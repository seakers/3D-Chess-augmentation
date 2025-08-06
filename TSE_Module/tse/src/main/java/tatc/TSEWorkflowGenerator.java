package tatc;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A Java implementation of the TSEWorkflowGenerator class.
 * Connects to a Neo4j database, parses user requests, and generates workflows in JSON format.
 * This class manages the generation of evaluation workflows based on desired metrics and tool constraints.
 * 
 * @author TSE Development Team
 */
public class TSEWorkflowGenerator implements AutoCloseable {

    private Driver driver;
    private final Set<String> requiredFunctions;        // Stores all functions required to calculate desired metrics
    private final Map<String, String> functionToolMap;  // Maps functions to the tools that implement them
    private final Map<String, List<String>> dependencyGraph; // Dependencies between functions
    private final Map<String, List<String>> metricFunctionMap; // Maps metric -> list of functions
    private Map<String, Integer> functionLevels;        // Stores the level of each function

    private Map<String, List<String>> toolDependencyGraph;    // For evaluating tool-level dependencies
    private Map<String, Integer> toolLevels;                 // The levels of each tool

    /**
     * Constructs a TSEWorkflowGenerator with the given Neo4j connection parameters.
     * 
     * @param neo4jUri The Neo4j database URI
     * @param neo4jUser The Neo4j username
     * @param neo4jPassword The Neo4j password
     * @throws RuntimeException if connection to Neo4j fails
     */
    public TSEWorkflowGenerator(String neo4jUri, String neo4jUser, String neo4jPassword) {
        System.out.println("Connecting to Neo4j:");
        System.out.println("URI: " + neo4jUri);
        System.out.println("USER: " + neo4jUser);
        System.out.println("PASS: [REDACTED]");

        try {
            driver = GraphDatabase.driver(neo4jUri, AuthTokens.basic(neo4jUser, neo4jPassword));
            System.out.println("Neo4j driver created successfully.");
        } catch (Exception e) {
            System.err.println("Failed to initialize Neo4j driver: " + e.getMessage());
            throw new RuntimeException("Neo4j connection failed", e);
        }
        
        this.requiredFunctions = new HashSet<>();
        this.functionToolMap = new HashMap<>();
        this.dependencyGraph = new HashMap<>();
        this.metricFunctionMap = new HashMap<>();
        this.functionLevels = new HashMap<>();
    }

    /**
     * Closes the Neo4j driver connection.
     */
    @Override
    public void close() {
        if (driver != null) {
            driver.close();
        }
    }

    /**
     * Executes a Cypher query with optional parameters and returns a list of map records.
     * 
     * @param query The Cypher query to execute
     * @param parameters Optional parameters for the query
     * @return List of map records containing the query results
     */
    private List<Map<String, Object>> executeQuery(String query, Map<String, Object> parameters) {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                Result result;
                if (parameters == null) {
                    result = tx.run(query);
                } else {
                    result = tx.run(query, parameters);
                }

                List<Map<String, Object>> output = new ArrayList<>();
                while (result.hasNext()) {
                    Record record = result.next();
                    // Convert each record to a Map<String,Object>
                    Map<String, Object> row = new HashMap<>();
                    for (String key : record.keys()) {
                        row.put(key, record.get(key).asObject());
                    }
                    output.add(row);
                }
                return output;
            });
        }
    }

    /**
     * Retrieves the httpAddress property of a given tool from Neo4j.
     * 
     * @param toolName The name of the tool
     * @return The HTTP address of the tool
     * @throws IllegalArgumentException if tool is not found or has no httpAddress
     */
    private String getToolHttpAddress(String toolName) {
        String query = 
            "MATCH (t:Tool {name: $tool_name}) " +
            "RETURN t.httpAddress AS httpAddress";

        Map<String, Object> params = Collections.singletonMap("tool_name", toolName);
        List<Map<String, Object>> result = executeQuery(query, params);

        if (!result.isEmpty() && result.get(0).get("httpAddress") != null) {
            return result.get(0).get("httpAddress").toString();
        } else {
            throw new IllegalArgumentException(
                "No httpAddress found for tool '" + toolName + "' or tool does not exist."
            );
        }
    }

    /**
     * Retrieves the list of functions that can calculate the given metric.
     * Also updates metricFunctionMap for future reference.
     * 
     * @param metricName The name of the metric
     * @return List of function names that can calculate the metric
     */
    private List<String> getFunctionsCalculatingMetric(String metricName) {
        String query = 
            "MATCH (f:Function)-[:CALCULATES]->(m:Metric {name: $metric_name}) " +
            "RETURN f.name AS function_name";

        Map<String, Object> params = Collections.singletonMap("metric_name", metricName);
        List<Map<String, Object>> results = executeQuery(query, params);

        List<String> functions = results.stream()
                .map(r -> r.get("function_name").toString())
                .collect(Collectors.toList());
        metricFunctionMap.put(metricName, functions);
        return functions;
    }

    /**
     * Recursively resolves dependencies for a function.
     * Updates the dependencyGraph and requiredFunctions collections.
     * 
     * @param functionName The name of the function to resolve dependencies for
     */
    private void resolveDependencies(String functionName) {
        String query = 
            "MATCH (f:Function {name: $function_name})-[:REQUIRES]->(dep:Function) " +
            "RETURN dep.name AS dependency_name";

        Map<String, Object> params = Collections.singletonMap("function_name", functionName);
        List<Map<String, Object>> results = executeQuery(query, params);

        List<String> dependencies = new ArrayList<>();
        for (Map<String, Object> record : results) {
            dependencies.add(record.get("dependency_name").toString());
        }
        dependencyGraph.put(functionName, dependencies);

        for (String depFunction : dependencies) {
            if (!requiredFunctions.contains(depFunction)) {
                requiredFunctions.add(depFunction);
                resolveDependencies(depFunction);
            }
        }
    }

    /**
     * Assigns a tool to a function, optionally checking user-specified constraints.
     * 
     * @param functionName The name of the function
     * @param userSpecifiedTool The user-specified tool (can be null)
     * @throws IllegalArgumentException if the specified tool doesn't implement the function
     */
    private void assignToolToFunction(String functionName, String userSpecifiedTool) {
        if (userSpecifiedTool != null) {
            // Check if that tool implements the function
            String query = 
                "MATCH (t:Tool {name: $tool_name})-[:IMPLEMENTS]->(f:Function {name: $function_name}) " +
                "RETURN t.name AS tool_name";

            Map<String, Object> params = new HashMap<>();
            params.put("tool_name", userSpecifiedTool);
            params.put("function_name", functionName);

            List<Map<String, Object>> results = executeQuery(query, params);
            if (!results.isEmpty()) {
                // Accept the user-specified tool
                functionToolMap.put(functionName, userSpecifiedTool);
            } else {
                throw new IllegalArgumentException(
                    "Error: Tool '" + userSpecifiedTool + "' does not implement function '" + functionName + "'."
                );
            }
        } else {
            // Find available tools for this function
            String query = 
                "MATCH (t:Tool)-[:IMPLEMENTS]->(f:Function {name: $function_name}) " +
                "RETURN t.name AS tool_name";

            Map<String, Object> params = Collections.singletonMap("function_name", functionName);
            List<Map<String, Object>> results = executeQuery(query, params);
            if (!results.isEmpty()) {
                // For now, pick the first tool
                String selectedTool = results.get(0).get("tool_name").toString();
                functionToolMap.put(functionName, selectedTool);
            } else {
                throw new IllegalArgumentException(
                    "Error: No available tools implement function '" + functionName + "'."
                );
            }
        }
    }

    /**
     * Computes levels of each function based on dependencyGraph.
     * Functions with no dependencies are level 1, others are level of max dependency + 1.
     */
    private void computeLevels() {
        functionLevels = new HashMap<>();
        Set<String> visited = new HashSet<>();

        for (String function : requiredFunctions) {
            assignLevel(function, visited);
        }
    }

    /**
     * Recursively assigns a level to each function based on its dependencies.
     * 
     * @param functionName The name of the function
     * @param visited Set of already visited functions to prevent cycles
     * @return The assigned level for the function
     */
    private int assignLevel(String functionName, Set<String> visited) {
        if (visited.contains(functionName)) {
            // Already computed
            return functionLevels.getOrDefault(functionName, 1);
        }
        visited.add(functionName);
        List<String> deps = dependencyGraph.getOrDefault(functionName, new ArrayList<>());
        if (deps.isEmpty()) {
            functionLevels.put(functionName, 1);
        } else {
            int maxDepLevel = 0;
            for (String dep : deps) {
                int depLevel = assignLevel(dep, visited);
                if (depLevel > maxDepLevel) {
                    maxDepLevel = depLevel;
                }
            }
            functionLevels.put(functionName, maxDepLevel + 1);
        }
        return functionLevels.get(functionName);
    }

    /**
     * Infers dependencies among tools from the given workflow.
     * 
     * @param workflow The workflow containing evaluator information
     * @return Map of tool dependencies
     */
    private Map<String, List<String>> inferToolDependenciesFromWorkflow(List<Map<String, Object>> workflow) {
        Map<String, List<String>> tDepGraph = new HashMap<>();
        // Each evaluator block has: "evaluator", "implementedFunctions", etc.
        for (Map<String, Object> evaluator : workflow) {
            String toolName = evaluator.get("evaluator").toString();
            tDepGraph.put(toolName, new ArrayList<>());

            // implementedFunctions => (functionName -> { dependencies: {...}, level: ... })
            Object implObj = evaluator.get("implementedFunctions");
            if (implObj instanceof Map<?, ?>) {
                Map<?, ?> implementedFuncs = (Map<?, ?>) implObj;
                for (Map.Entry<?, ?> entry : implementedFuncs.entrySet()) {
                    String functionName = entry.getKey().toString();
                    Object functionDataObj = entry.getValue();
                    if (functionDataObj instanceof Map<?, ?>) {
                        Map<?, ?> fnData = (Map<?, ?>) functionDataObj;
                        Object depsObj = fnData.get("dependencies");
                        if (depsObj instanceof Map<?, ?>) {
                            Map<?, ?> depMap = (Map<?, ?>) depsObj;
                            for (Map.Entry<?, ?> dmEntry : depMap.entrySet()) {
                                String depToolUrl = dmEntry.getValue().toString();
                                if (!"self".equalsIgnoreCase(depToolUrl)) {
                                    String[] parts = depToolUrl.split("/");
                                    if (parts.length >= 2) {
                                        String depToolName = parts[1]; // the second part
                                        tDepGraph.get(toolName).add(depToolName);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return tDepGraph;
    }

    /**
     * Computes levels of each tool based on the inferred dependencies from the workflow.
     * 
     * @param workflow The workflow to analyze
     * @return Map of tool levels
     */
    private Map<String, Integer> computeToolLevels(List<Map<String, Object>> workflow) {
        // 1) Infer tool dependencies from the workflow
        toolDependencyGraph = inferToolDependenciesFromWorkflow(workflow);
        toolLevels = new HashMap<>();

        // 2) Recursively assign levels
        Set<String> visited = new HashSet<>();
        // Collect all tools
        for (Map<String, Object> evaluator : workflow) {
            String toolName = evaluator.get("evaluator").toString();
            assignToolLevel(toolName, visited);
        }
        return toolLevels;
    }

    /**
     * Assigns a level to a given tool, based on its dependent tools.
     * 
     * @param toolName The name of the tool
     * @param visited Set of already visited tools to prevent cycles
     * @return The assigned level for the tool
     */
    private int assignToolLevel(String toolName, Set<String> visited) {
        if (visited.contains(toolName)) {
            return toolLevels.getOrDefault(toolName, 1);
        }
        visited.add(toolName);

        List<String> deps = toolDependencyGraph.getOrDefault(toolName, new ArrayList<>());
        if (deps.isEmpty()) {
            toolLevels.put(toolName, 1);
        } else {
            int maxDepLevel = 0;
            for (String depTool : deps) {
                int depLevel = assignToolLevel(depTool, visited);
                if (depLevel > maxDepLevel) {
                    maxDepLevel = depLevel;
                }
            }
            toolLevels.put(toolName, maxDepLevel + 1);
        }
        return toolLevels.get(toolName);
    }

    /**
     * Main method to generate the workflow given the user_request.
     * This method orchestrates the entire workflow generation process.
     * 
     * @param userRequest The JSON object containing the user's request
     * @return A map representing the generated workflow
     * @throws IllegalArgumentException if the request is invalid or cannot be processed
     */
    public Map<String, Object> generateWorkflow(JSONObject userRequest) {
        // 1) Parse the user request for desired metrics and tool constraints
        JSONObject evaluationObj = userRequest.optJSONObject("evaluation");
        JSONObject desiredMetricsObj = evaluationObj.optJSONObject("metrics");
        Map<String, String> desiredMetrics = new HashMap<>();
        if (desiredMetricsObj != null) {
            for (String key : desiredMetricsObj.keySet()) {
                desiredMetrics.put(key, desiredMetricsObj.getString(key));
            }
        }
        JSONObject toolConstraintsObj = evaluationObj.optJSONObject("tool_constraints");
        Map<String, String> toolConstraints = new HashMap<>();
        if (toolConstraintsObj != null) {
            for (String key : toolConstraintsObj.keySet()) {
                toolConstraints.put(key, toolConstraintsObj.getString(key));
            }
        }

        // 2) Gather functions for each metric, and their dependencies
        for (String metric : desiredMetrics.keySet()) {
            List<String> functions = getFunctionsCalculatingMetric(metric);
            if (functions.isEmpty()) {
                throw new IllegalArgumentException(
                    "Error: No functions calculate the metric '" + metric + "'."
                );
            }
            for (String fn : functions) {
                requiredFunctions.add(fn);
                resolveDependencies(fn);
            }
        }

        // 3) Assign tools to required functions
        for (String fn : requiredFunctions) {
            String userTool = toolConstraints.get(fn);
            assignToolToFunction(fn, userTool);
        }

        // 4) Compute levels among functions
        computeLevels();

        // 5) Prepare an "evaluator map": tool -> block with "metrics", "implementedFunctions", etc.
        Map<String, Map<String, Object>> evaluatorMap = new LinkedHashMap<>();

        // Each function belongs to one tool
        for (String fn : requiredFunctions) {
            String tool = functionToolMap.get(fn);
            if (!evaluatorMap.containsKey(tool)) {
                Map<String, Object> block = new HashMap<>();
                block.put("evaluator", tool);
                block.put("metrics", new HashMap<String, String>());  
                block.put("implementedFunctions", new HashMap<String, Map<String, Object>>());
                block.put("subscribe", new ArrayList<String>());
                block.put("publish_metrics", new ArrayList<String>());
                evaluatorMap.put(tool, block);
            }
        }

        // 6) Map metrics -> evaluators
        for (Map.Entry<String, List<String>> entry : metricFunctionMap.entrySet()) {
            String metric = entry.getKey();
            List<String> fns = entry.getValue();
            // Possibly multiple functions for one metric
            for (String fn : fns) {
                String tool = functionToolMap.get(fn);
                @SuppressWarnings("unchecked")
                Map<String, String> metricMap =
                        (Map<String, String>) evaluatorMap.get(tool).get("metrics");
                metricMap.put(metric, fn);
            }
        }

        // 7) Assign implementedFunctions + levels + dependencies
        for (String function : functionToolMap.keySet()) {
            String tool = functionToolMap.get(function);
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> implementedFunctions =
                    (Map<String, Map<String, Object>>) evaluatorMap.get(tool).get("implementedFunctions");

            if (!implementedFunctions.containsKey(function)) {
                implementedFunctions.put(function, new HashMap<String, Object>());
            }
            Map<String, Object> fnData = implementedFunctions.get(function);
            if (!fnData.containsKey("dependencies")) {
                fnData.put("dependencies", new HashMap<String, String>());
            }
            @SuppressWarnings("unchecked")
            Map<String, String> depMap = (Map<String, String>) fnData.get("dependencies");
            List<String> deps = dependencyGraph.getOrDefault(function, new ArrayList<>());

            // Add each dependency as function_name -> "http://tool/function" or "self"
            for (String depFn : deps) {
                String depTool = functionToolMap.get(depFn);
                if (!depTool.equals(tool)) {
                    depMap.put(depFn, getToolHttpAddress(depTool) + "/" + depFn);
                } else {
                    depMap.put(depFn, "self");
                }
            }
            // Set the function level
            fnData.put("level", functionLevels.getOrDefault(function, 1));

            @SuppressWarnings("unchecked")
            List<String> subscribeList = (List<String>) evaluatorMap.get(tool).get("subscribe");
            subscribeList.add("evaluators/" + tool);
        }

        // 8) Deduplicate "subscribe" arrays, set "publish_metrics" to "TSE"
        for (Map.Entry<String, Map<String, Object>> e : evaluatorMap.entrySet()) {
            Map<String, Object> block = e.getValue();
            @SuppressWarnings("unchecked")
            List<String> subList = (List<String>) block.get("subscribe");
            // Deduplicate
            Set<String> uniqueSubs = new HashSet<>(subList);
            block.put("subscribe", new ArrayList<>(uniqueSubs));

            // publish_metrics => "TSE" as in the original code
            block.put("publish_metrics", "TSE");
        }

        // Convert evaluatorMap to a list
        List<Map<String, Object>> workflow = new ArrayList<>(evaluatorMap.values());

        // 9) Compute tool levels
        Map<String, Integer> toolLvls = computeToolLevels(workflow);

        // 10) Publish map for each metric => "evaluators/{tool}/{function}"
        Map<String, String> publishMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : metricFunctionMap.entrySet()) {
            String metric = entry.getKey();
            List<String> fns = entry.getValue();
            if (!fns.isEmpty()) {
                String fn = fns.get(0);
                String tool = functionToolMap.get(fn);
                publishMap.put(metric, "evaluators/" + tool + "/" + fn);
            }
        }

        // 11) Prepare objectives
        List<Map<String, Object>> objectives = new ArrayList<>();
        for (String metric : desiredMetrics.keySet()) {
            String objectiveType = desiredMetrics.get(metric);
            Map<String, Object> obj = new HashMap<>();
            obj.put("objectiveName", metric);
            obj.put("objectiveType", objectiveType);
            objectives.add(obj);
        }

        // 12) Build final JSON object
        Map<String, Object> root = new LinkedHashMap<>();
        Map<String, Object> evaluation = new LinkedHashMap<>();

        // TSE block
        Map<String, Object> tseBlock = new LinkedHashMap<>();
        tseBlock.put("objectives", objectives);
        tseBlock.put("subscribe", "TSE");
        tseBlock.put("publish_metric_requests", publishMap);
        tseBlock.put("tool_levels", toolLvls);

        evaluation.put("TSE", tseBlock);
        evaluation.put("workflow", workflow);
        root.put("evaluation", evaluation);

        return root;
    }

    //------------------------------------------------------------------------------------------
    // UTILITY: read a user_request.json, build workflow, and write output to workflow_output.json
    //------------------------------------------------------------------------------------------

    /**
     * Main method for standalone execution of the workflow generator.
     * Reads a user request file and generates a workflow output file.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Configuration: File paths
        String userRequestFile = "user_request.json";
        String outputFile = "workflow_output.json";

        // Get Neo4j credentials from environment variables or use defaults
        String neo4jUri = System.getenv("NEO4J_URI");
        String neo4jUser = System.getenv("NEO4J_USER");
        String neo4jPassword = System.getenv("NEO4J_PASSWORD");
        
        if (neo4jUri == null || neo4jUser == null || neo4jPassword == null) {
            System.err.println("Error: Neo4j credentials not found in environment variables.");
            System.err.println("Please set NEO4J_URI, NEO4J_USER, and NEO4J_PASSWORD environment variables.");
            return;
        }

        // Create the generator
        try (TSEWorkflowGenerator generator = new TSEWorkflowGenerator(neo4jUri, neo4jUser, neo4jPassword)) {
            // Parse user_request.json
            JSONObject userRequest;
            try (FileReader fr = new FileReader(userRequestFile)) {
                userRequest = new JSONObject(new JSONTokener(fr));
            } catch (IOException e) {
                System.err.println("Error reading user_request.json: " + e.getMessage());
                return;
            }

            // Generate workflow
            Map<String, Object> outputMap;
            try {
                outputMap = generator.generateWorkflow(userRequest);
            } catch (Exception e) {
                // If an error occurs, produce an error JSON
                JSONObject errorOutput = new JSONObject();
                errorOutput.put("error", e.getMessage());
                try (FileWriter fw = new FileWriter(outputFile)) {
                    fw.write(errorOutput.toString(4));
                } catch (IOException ioEx) {
                    System.err.println("Failed to write error to output: " + ioEx.getMessage());
                }
                System.err.println("An error occurred: " + e.getMessage());
                return;
            }

            // Write the final output to workflow_output.json
            try (FileWriter fw = new FileWriter(outputFile)) {
                JSONObject finalJson = new JSONObject(outputMap);
                fw.write(finalJson.toString(4)); // pretty-print with 4 spaces
            } catch (IOException e) {
                System.err.println("Error writing workflow_output.json: " + e.getMessage());
            }

            System.out.println("Workflow successfully generated and saved to '" + outputFile + "'.");

        } catch (Exception e) {
            // If generator initialization or close() fails
            System.err.println("Fatal error with TSEWorkflowGenerator: " + e.getMessage());
        }
    }
}
