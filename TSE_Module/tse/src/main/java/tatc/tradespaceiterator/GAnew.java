package tatc.tradespaceiterator;
import org.moeaframework.core.*;
import org.moeaframework.core.variable.RealVariable;  // We'll use RealVariables for integer encoding
import org.moeaframework.problem.AbstractProblem;
import tatc.decisions.adg.AdgSolution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URI;

import tatc.decisions.Combining;
import tatc.decisions.ConstructionNode;
import tatc.decisions.Decision;
import tatc.decisions.Partitioning;
import tatc.decisions.adg.AdgSolution;
import tatc.decisions.adg.Graph;
import tatc.architecture.specifications.CompoundObjective;
import tatc.architecture.specifications.GroundNetwork;
import tatc.tradespaceiterator.ProblemProperties;
import tatc.architecture.ArchitectureCreatorNew;
import tatc.tradespaceiterator.TradespaceSearchExecutive;
import tatc.util.Summary;
import java.lang.InterruptedException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class GAnew extends AbstractProblem {

    private ProblemProperties properties;
    private List<Decision> decisions;
    private int totalVariables;
    private int totalObjectives;
    private int counter;
    private int solutionCounter;
    private Graph graph;
    private String callbackUrl;
    private boolean loadSolutions = false; // or false
    private List<int[]> loadedChromosomes = new ArrayList<>();
    private List<double[]> loadedObjectives = new ArrayList<>();
    private int loadPointer = 0;
    /**
     * Constructs a GA problem from the given properties and a list of decisions.
     * 
     * @param properties     Problem properties.
     * @param decisions      A list of Decision objects (Combining, Assigning, etc.).
     * @param totalObjectives Number of objectives (from problem definition).
     */
    public GAnew(ProblemProperties properties, Graph graph, int totalObjectives) {
        super(getTotalNumberOfVariables(graph.getTopoOrderedDecisions(), properties), totalObjectives);
        this.decisions = graph.getTopoOrderedDecisions();
        this.properties = properties;
        this.totalObjectives = totalObjectives;
        this.totalVariables = getTotalNumberOfVariables(decisions, properties);
        this.counter = 0;
        this.graph = graph;
        this.solutionCounter = 0;
       
        // Get callback URL from properties
        this.callbackUrl = properties.getTsrObject().optString("callbackUrl", null);
        System.out.println("GAnew initialized with callback URL: " + this.callbackUrl);
    }

    // Utility to sum up variables from each decision
    private static int getTotalNumberOfVariables(List<Decision> decisions, ProblemProperties properties) {
        int sum = 0;
        for (Decision d : decisions) {
            d.initializeDecisionVariables();
            sum += d.getNumberOfVariables();
        }
        return sum;
    }

    @Override
    public void evaluate(Solution solution) {
        // Decode the solution into architecture parameters
        List<Map<String, Object>> archParams = decodeSolution(solution);
        ArchitectureCreatorNew creator = new ArchitectureCreatorNew(properties);
        JSONObject tseRequestJson = properties.getTsrObject();
        JSONArray constellationsJSON = tseRequestJson.getJSONObject("designSpace").getJSONArray("spaceSegment");
        JSONObject constJson = constellationsJSON.getJSONObject(0);

        if (archParams instanceof List){

            for (Map<String, Object> conste : ( List<Map<String, Object>>) archParams){
                creator.addHomogeneousWalkerOld(constJson, conste);

            }

        }else if(archParams instanceof Map){
            // Build architecture JSON using the archParams
            int k = 0;
            Map<String, Object> architecture = new HashMap<>();
            //JSONObject finalConst = creator.addHomogeneousWalker(constJson, archParameters);
            creator.addHomogeneousWalkerOld(constJson, (HashMap)archParams);
            k++;

        }
        
    

        // Add ground network if available
        HashMap<String, tatc.architecture.variable.Decision<?>> allDecisions = properties.getDecisions();
        tatc.architecture.variable.Decision<GroundNetwork> decisionGroundNetwork = (tatc.architecture.variable.Decision<GroundNetwork>) allDecisions.get("groundNetwork");

        // Assume we picked a groundNetwork from archParams, or from decisionGroundNetwork
        // If the groundNetwork decision was one of the combining or assigning decisions,
        // archParams should contain a chosen GroundNetwork
        GroundNetwork chosenGN = (GroundNetwork) properties.getTradespaceSearch().getDesignSpace().getGroundSegment().get(0);
        if (chosenGN == null) {
            // fallback: pick first allowedValue, or handle error
            chosenGN = decisionGroundNetwork.getAllowedValues().get(0);
        }
        creator.addGroundNetwork(chosenGN);

        if (!creator.getConstellations().isEmpty()) {
            // Write architecture JSON and evaluate
             // Convert current solution into a chromosome int[]
            int numVars = solution.getNumberOfVariables();
            int[] chromosome = new int[numVars];
            for (int i = 0; i < numVars; i++) {
                chromosome[i] = (int) Math.round(((RealVariable) solution.getVariable(i)).getValue());
            }

            boolean matched = false;

            if (loadSolutions && loadedChromosomes != null && loadedObjectives != null) {
                for (int i = 0; i < loadedChromosomes.size(); i++) {
                    int[] known = loadedChromosomes.get(i);
                    if (Arrays.equals(known, chromosome)) {
                        double[] objectives = loadedObjectives.get(i);
                        for (int j = 0; j < objectives.length; j++) {
                            String type = properties.getObjectives().get(j).getParent().getType();
                            double value = type.equals("MAX") ? -objectives[j] : objectives[j];
                            solution.setObjective(j, value);
                        }
                        System.out.println("Solution #" + (i + 1) + " already evaluated. Objectives: " + Arrays.toString(objectives));
                        matched = true;
                        
                        // Write summary for loaded solution
                        HashMap<String, Double> objectivesResults = new HashMap<>();
                        properties.getObjectives();
                        int j=0;
                        for (CompoundObjective objective : properties.getObjectives()) {
                            String objectiveName = objective.getParent().getName();
                            objectivesResults.put(objectiveName, objectives[j]);
                            j++;
                        }
                        try {
                            File architectureJsonFile = creator.toJSON(this.counter);
                            Summary.writeSummaryFileGA(objectivesResults, solution, this.counter, decisions);
                        } catch (IOException e) {
                            System.err.println("Error writing summary file: " + e.getMessage());
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }

            if (!matched) {
                // Evaluate normally if no match found
                File architectureJsonFile = creator.toJSON(this.counter);
                this.counter++;
                try {
                    HashMap<String, Double> objectivesResults = evaluateArchitecture(architectureJsonFile, properties);
                    Summary.writeSummaryFileGA(objectivesResults, solution, this.counter, decisions);

                    int objIndex = 0;
                    for (Map.Entry<String, Double> obj : objectivesResults.entrySet()) {
                        String type = properties.getObjectives().get(objIndex).getParent().getType();
                        Double objective = type.equals("MAX") ? -obj.getValue() : obj.getValue();
                        solution.setObjective(objIndex++, objective);
                        if (objIndex >= solution.getNumberOfObjectives()) break;
                    }

                    if (callbackUrl != null) {
                        sendSolution(solution, objectivesResults);
                    }

                } catch (IOException e) {
                    System.out.println("Error reading the JSON file: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
                // If no constellations were created, set objectives to infinity
                System.out.println("No constellations created.");
                for (int i = 0; i < solution.getNumberOfObjectives(); i++) {
                    solution.setObjective(i, Double.POSITIVE_INFINITY);
                }
        }
    }

    private List<Map<String, Object>> decodeSolution(Solution solution) {
        int offset = 0;
        Set<String> leafDecision = graph.getLeafDecisions();
    
        // 1) Decode ALL decisions in topological order, but do not merge
        for (Decision d : decisions) {
            // (a) Set inputs from parent decisions/TSERequest
            graph.setInputs(d);
    
            // (b) Extract encoding & apply
            Object encoded = d.extractEncodingFromSolution(solution, offset);
            d.applyEncoding((int[]) encoded);
            offset += d.getNumberOfVariables();

            // (c) Decode
            
            if (d instanceof ConstructionNode){
                return d.decodeArchitecture(null, solution, graph);
            }else if(!(decisions.get(decisions.size()-1) instanceof ConstructionNode) && leafDecision.contains(d.getDecisionName())){
                List<Map<String, Object>> partialDecoded = d.decodeArchitecture(encoded, solution, graph);
                return partialDecoded;
            }
            // (d) Store the result inside the decision for later reference
        }

        return new ArrayList<>();
    }
    
    

    

    

    /**
     * Evaluate architecture by calling TradespaceSearchExecutive.
     */
    private HashMap<String,Double> evaluateArchitecture(File architectureJSONFile, ProblemProperties problemProperties) {
        HashMap<String, Double> objectives = new HashMap<>();
        try {
            objectives = TradespaceSearchExecutive.evaluateArchitecture(architectureJSONFile, problemProperties);
        } catch(InterruptedException | IOException e) {
            System.out.println("Error reading JSON file: " + e.getMessage());
            e.printStackTrace();
            // If error, set large objective values
            for(int i=0; i<totalObjectives; i++){
                objectives.put("obj"+i, Double.POSITIVE_INFINITY);
            }
        }
        return objectives;
    }

    private void sendSolution(Solution solution, HashMap<String, Double> objectivesResults) {
        try {
            // Create JSON payload
            JSONObject payload = new JSONObject();
            
            // Add design variables with meaningful names
            JSONObject designVariables = new JSONObject();
            int varOffset = 0;
            
            for (Decision d : decisions) {
                // Skip construction nodes as they don't have direct variables
                if (d instanceof ConstructionNode) {
                    continue;
                }
                
                // Get the encoded values for this decision
                int[] encoded = (int[]) d.extractEncodingFromSolution(solution, varOffset);
                
                // Get the variable names for this decision
                List<String> varNames = d.getVariableNames();
                
                // Map encoded values to their corresponding names
                for (int i = 0; i < encoded.length; i++) {
                    String varName;
                    RealVariable var = (RealVariable) solution.getVariable(varOffset + i);
                    
                    // For assigning decisions, create a more descriptive name
                    if (d instanceof tatc.decisions.Assigning) {
                        tatc.decisions.Assigning assign = (tatc.decisions.Assigning) d;
                        List<String> sources = assign.getSourceEntities();
                        List<String> targets = assign.getTargetEntities();
                        
                        if (i < sources.size() && i < targets.size()) {
                            varName = sources.get(i) + "-" + targets.get(i);
                        } else {
                            varName = varNames.get(i);
                        }
                    } else {
                        varName = varNames.get(i);
                    }
                    
                    designVariables.put(varName, var.getValue());
                }
                
                varOffset += encoded.length;
            }
            
            payload.put("designVariables", designVariables);
            
            // Add objectives
            JSONObject objectives = new JSONObject();
            for (Map.Entry<String, Double> entry : objectivesResults.entrySet()) {
                objectives.put(entry.getKey(), entry.getValue());
            }
            payload.put("objectives", objectives);
            
            // Add solution ID
            payload.put("solutionId", solutionCounter++);

            System.out.println("Sending solution #" + (solutionCounter-1) + " to callback URL: " + callbackUrl);
            System.out.println("Objectives: " + objectives.toString(2));
            System.out.println("Design Variables: " + designVariables.toString(2));

            // Send HTTP POST request using Apache HttpClient
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(callbackUrl);
                httpPost.setHeader("Content-Type", "application/json");
                httpPost.setEntity(new StringEntity(payload.toString(), "UTF-8"));

                // Execute the request
                try (CloseableHttpResponse response = client.execute(httpPost)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode != 200) {
                        System.err.println("Failed to send solution: " + statusCode);
                    } else {
                        System.out.println("Successfully sent solution #" + (solutionCounter-1));
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error preparing solution for HTTP: " + e.getMessage());
        }
    }

    private List<int[]> loadChromosomesFromCSV(String filename, int totalObjectives) {
        List<int[]> chromosomes = new ArrayList<>();
        loadedObjectives = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            // Skip header row
            reader.readLine();
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.trim().split(",");
                // Skip first column, adjust length calculation
                int len = tokens.length - totalObjectives - 1;
                int[] chrom = new int[len];
                double[] objectives = new double[totalObjectives];
                
                // Load chromosome (starting from index 1 to skip first column)
                for (int i = 0; i < len; i++) {
                    chrom[i] = (int) Double.parseDouble(tokens[i + 1].trim());
                }
                
                // Load objectives from last totalObjectives columns
                for (int i = 0; i < totalObjectives; i++) {
                    objectives[i] = Double.parseDouble(tokens[tokens.length - totalObjectives + i].trim());
                }
                
                chromosomes.add(chrom);
                loadedObjectives.add(objectives);
            }
        } catch (IOException e) {
            System.err.println("Error reading chromosome file: " + filename);
            e.printStackTrace();
        }
        return chromosomes;
    }

    
    @Override
    public Solution newSolution() {
        // Check if we are loading solutions from a CSV file
        loadedChromosomes = loadChromosomesFromCSV("C:/Users/dfornos/OneDrive - Texas A&M University/Desktop/3D-CHESS-aumentation-MQTT/3D-Chess-augmentation/summary_files/summary.csv", totalObjectives);

        if (loadSolutions && loadPointer < loadedChromosomes.size()) {
            for (Decision d : decisions) {
                if(d instanceof ConstructionNode){
                    break;
                }

                int[] chromosome = loadedChromosomes.get(loadPointer);
                loadPointer++;

                // Create solution with the appropriate number of variables
                AdgSolution solution = new AdgSolution(graph, properties, totalObjectives, chromosome.length);
                for (int i = 0; i < chromosome.length; i++) {
                    RealVariable var = new RealVariable(0.0, 100.0);
                    var.setValue(chromosome[i]);
                    solution.setVariable(i, var);
                }
                d.addEncodingById(solutionCounter, chromosome);
                solution.setId(solutionCounter++);
                return solution;
            }
        }
        
        // If not loading solutions or no more solutions to load, generate new random solution
        List<int[]> allEncodings = new ArrayList<>();
        for (Decision d : decisions) {
            if(d instanceof ConstructionNode){
                break;
            }
            this.graph.setInputs(d);
            Object encoded = d.randomEncoding();
            int[] arr = (int[]) encoded;
            allEncodings.add(arr);
        }
    
        int totalVars = 0;
        for (int[] enc : allEncodings) {
            totalVars += enc.length;
        }
    
        AdgSolution solution = new AdgSolution(graph, properties, totalObjectives, totalVars);    
        int offset = 0;
        for (int i = 0; i < decisions.size(); i++) {
            Decision d = decisions.get(i);
            if (d instanceof ConstructionNode) {
                break;
            }
        
            int[] arr = allEncodings.get(i);
        
            for (int j = 0; j < arr.length; j++) {
                int maxOption = d.getMaxOptionForVariable(j);
                if (maxOption < 1) {
                    maxOption = 1;
                }
        
                double lowerBound = 0.0;
                double upperBound = maxOption - 1;
        
                double clampedValue = arr[j];
                if (clampedValue < lowerBound) {
                    clampedValue = lowerBound;
                } else if (clampedValue > upperBound) {
                    clampedValue = upperBound;
                }
        
                RealVariable var = new RealVariable(lowerBound, upperBound);
                var.setValue(clampedValue);
                solution.setVariable(offset + j, var);
            }
        
            d.addEncodingById(solutionCounter, arr);
            offset += arr.length;
        }
        
        solution.setId(solutionCounter);
        solutionCounter++;
        return solution;
    }
    
    


    @Override
    public int getNumberOfObjectives() {
        return totalObjectives;
    }

    @Override
    public int getNumberOfConstraints() {
        return 0;
    }

    public ProblemProperties getProperties() {
        return properties;
    }

    public void setProperties(ProblemProperties properties) {
        this.properties = properties;
    }

    public List<Decision> getDecisions() {
        return decisions;
    }

    public void setDecisions(List<Decision> decisions) {
        this.decisions = decisions;
    }

    public int getTotalVariables() {
        return totalVariables;
    }

    public void setTotalVariables(int totalVariables) {
        this.totalVariables = totalVariables;
    }

    public int getTotalObjectives() {
        return totalObjectives;
    }

    public void setTotalObjectives(int totalObjectives) {
        this.totalObjectives = totalObjectives;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

}
