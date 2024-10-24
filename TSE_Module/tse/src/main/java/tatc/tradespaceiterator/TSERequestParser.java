package tatc.tradespaceiterator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.json.JSONArray;

public class TSERequestParser {

    /**
     * Extracts a mapping of metrics to their respective evaluators from the TSE request.
     *
     * @param tseRequest The JSONObject representing the TSE request.
     * @return A Map where the key is the metric name and the value is the evaluator name.
     */
    public Map<String, String> getEvaluatorsForObjectives(JSONObject tseRequest) {
        Map<String, String> objectivesAndEvaluators = new HashMap<>();

        // Extract the publish_metric_requests that maps metrics to evaluators
        JSONObject publish = tseRequest.getJSONObject("evaluation")
                .getJSONObject("TSE")
                .getJSONObject("publish_metric_requests");

        // Loop through the metrics and get the respective evaluators
        for (String metric : publish.keySet()) {
            String evaluatorPath = publish.getString(metric);
            // Extract the evaluator name from the full path (e.g., "evaluators/InstruPy")
            String evaluatorName = extractEvaluatorName(evaluatorPath);
            objectivesAndEvaluators.put(metric, evaluatorName);
        }

        return objectivesAndEvaluators;
    }

    /**
     * Extracts the evaluator name from a given evaluator path.
     *
     * @param evaluatorPath The evaluator path string (e.g., "evaluators/InstruPy").
     * @return The evaluator name (e.g., "InstruPy").
     */
    private String extractEvaluatorName(String evaluatorPath) {
        if (evaluatorPath.contains("/")) {
            return evaluatorPath.substring(evaluatorPath.lastIndexOf('/') + 1);
        }
        return evaluatorPath; // Return as-is if no '/' found
    }

    /**
     * Retrieves a mapping of evaluators to the functions they implement.
     *
     * @param tseRequest The JSONObject representing the TSE request.
     * @return A Map where the key is the evaluator name and the value is a list of function names.
     */
    public Map<String, List<String>> getWorkflow(JSONObject tseRequest) {
        Map<String, List<String>> evaluatorFunctions = new HashMap<>();

        // Get the workflow array
        JSONArray workflow = tseRequest.getJSONObject("evaluation").getJSONArray("workflow");

        // Loop through each evaluator's workflow
        for (int i = 0; i < workflow.length(); i++) {
            JSONObject evaluatorObject = workflow.getJSONObject(i);
            String evaluatorName = evaluatorObject.getString("evaluator");

            // Get the functions implemented by this evaluator
            JSONObject implementedFunctions = evaluatorObject.getJSONObject("implementedFunctions");

            // Collect function names from the workflow
            List<String> functions = new ArrayList<>(implementedFunctions.keySet());

            // Add to the map of evaluators and the functions they are responsible for
            evaluatorFunctions.put(evaluatorName, functions);
        }

        return evaluatorFunctions;
    }
    public Map<String, String> getMetricRequestsTopics(JSONObject tseRequest) {
        Map<String, String> metricTopicsMap = new HashMap<>();

        // Get the publish_metric_requests object from the TSE request
        JSONObject publishMetricRequests = tseRequest.getJSONObject("evaluation")
                .getJSONObject("TSE")
                .getJSONObject("publish_metric_requests");

        // Loop through each metric in the publish_metric_requests object
        for (String metric : publishMetricRequests.keySet()) {
            // Get the topic for each metric
            String topic = publishMetricRequests.getString(metric);
            
            // Add the metric and its corresponding topic to the map (using Arrays.asList for Java 8 compatibility)
            metricTopicsMap.put(metric, topic);
        }

        return metricTopicsMap;
    }

    /**
     * Prepares and prints requests to send to evaluators based on the parsed TSERequest.
     *
     * @param tseRequest The JSONObject representing the TSE request.
     */
    public void createRequestsForEvaluators(JSONObject tseRequest) {
        // Step 1: Get the objectives and their evaluators
        Map<String, String> objectivesEvaluators = getEvaluatorsForObjectives(tseRequest);

        // Step 2: Get the workflow and functions implemented by each evaluator
        Map<String, List<String>> evaluatorFunctions = getWorkflow(tseRequest);

        // Step 3: Group metrics by evaluator
        Map<String, List<String>> evaluatorMetrics = new HashMap<>();
        for (Map.Entry<String, String> entry : objectivesEvaluators.entrySet()) {
            String metric = entry.getKey();
            String evaluator = entry.getValue();

            evaluatorMetrics.computeIfAbsent(evaluator, k -> new ArrayList<>()).add(metric);
        }

        // Iterate through each evaluator to build and print requests
        for (String evaluator : evaluatorFunctions.keySet()) {
            List<String> functionsToCall = evaluatorFunctions.get(evaluator);
            List<String> metricsToCalculate = evaluatorMetrics.getOrDefault(evaluator, Collections.emptyList());

            // Build and print a request to the evaluator
            System.out.println("Sending request to evaluator: " + evaluator);
            System.out.println("Metrics to calculate: " + metricsToCalculate);
            System.out.println("Functions to call: " + functionsToCall);
            System.out.println("-----------------------------------------");

            // TODO: Create and send actual requests to the evaluators
            // Example:
            // JSONObject evaluatorRequest = new JSONObject();
            // evaluatorRequest.put("metrics", metricsToCalculate);
            // evaluatorRequest.put("functions", functionsToCall);
            // sendRequestToEvaluator(evaluator, evaluatorRequest);
        }
    }

    /**
     * Loads the TSERequest JSON file from the specified file path.
     *
     * @param filePath The path to the TSERequest JSON file.
     * @return A JSONObject representing the TSE request.
     * @throws IOException If an I/O error occurs reading from the file.
     */
    public static JSONObject loadTSERequest(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        return new JSONObject(content);
    }

    /**
     * Main method to test the TSERequestParser.
     *
     * @param args Command-line arguments (not used).
     * @throws IOException If an I/O error occurs reading the TSE request file.
     */
    public static void main(String[] args) throws IOException {
        // Load the TSERequest JSON file
        JSONObject tseRequest = loadTSERequest("workflow_output.json");

        // Create the parser object
        TSERequestParser parser = new TSERequestParser();

        // Parse and process the request
        parser.createRequestsForEvaluators(tseRequest);

        // Optionally, you can perform additional operations here
    }

    /**
     * Example method to send a request to an evaluator.
     * Implement the actual communication logic as needed.
     *
     * @param evaluatorName    The name of the evaluator.
     * @param evaluatorRequest The JSON object containing the request data.
     */
    private void sendRequestToEvaluator(String evaluatorName, JSONObject evaluatorRequest) {
        // Implement the logic to send the request to the evaluator
        // This could be via HTTP, message queues, etc.
        // Example (pseudo-code):
        // HttpClient client = HttpClient.newHttpClient();
        // HttpRequest request = HttpRequest.newBuilder()
        //         .uri(URI.create("http://evaluator-service/" + evaluatorName + "/execute"))
        //         .header("Content-Type", "application/json")
        //         .POST(HttpRequest.BodyPublishers.ofString(evaluatorRequest.toString()))
        //         .build();
        // client.sendAsync(request, BodyHandlers.ofString())
        //         .thenApply(HttpResponse::body)
        //         .thenAccept(System.out::println);
    }
}
