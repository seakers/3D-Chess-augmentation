package tatc.tradespaceiterator;
import tatc.PythonServerManager;
import tatc.ResultIO;
import tatc.TSEPublisher;
import tatc.TSESubscriber;
import tatc.architecture.specifications.Architecture;
import tatc.architecture.specifications.TradespaceSearch;
import tatc.util.JSONIO;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import tatc.TSESubscriber;
import tatc.TSESubscriber;
import tatc.tradespaceiterator.TSERequestParser;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
/**
 * TradespaceSearchExecutive class which reads TradespaceSearchRequest.json, creates the problem properties,
 * and calls a search strategy (e.g. Full Factorial or Genetic Algorithm).
 */
public class TradespaceSearchExecutive {

    /**
     * The input path where the tradespace search request JSON file is located
     */
    private  String iPath;
    /**
     * The output path where the output results will be stored
     */
    private  String oPath;

    private Map<String, List<String>> costEvaluators;
    private Map<String, List<String>> scienceEvaluators;
    private Map<String, List<String>> evaluators;

    /**
     * Constructs the tradespace search executive
     * @param iPath the input path
     * @param oPath the output path
     */
    public TradespaceSearchExecutive(String iPath, String oPath){
        this.iPath=iPath;
        this.oPath=oPath;
    }

    /**
     * Runs the TSE.
     * 1. It configures the input, output and demo paths used during execution of the tse
     * 2. Reads the tradespace search request JSON file
     * 3. Creates the problem properties
     * 4. Creates the search strategy (FF, MOEA, AOS or KDO)
     * 5. Runs the selected search strategy
     * @throws IllegalArgumentException
     */
    public void run() throws IllegalArgumentException {
        this.setDirectories();
        TSERequestParser parser = new TSERequestParser();
        String jsonFilePath = iPath;
        PythonServerManager serverManager = new PythonServerManager();
        try{
            String evaluatorModulePath;
            String serverScriptPath;
            String content = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
            JSONObject tseRequest = new JSONObject(content);
            String tatcRoot = System.getProperty("tatc.root");
            Map<String, List<String>> evaluators = parser.getWorkflow(tseRequest);
            // Store the parsed evaluators and metrics
            this.costEvaluators = evaluators;
            this.scienceEvaluators = evaluators;
            this.evaluators = evaluators;
            if (costEvaluators.containsKey("SpaDes")) {
                evaluatorModulePath = tatcRoot + File.separator + "Evaluators_Module" + File.separator + "SpaDes";
                serverScriptPath = evaluatorModulePath + File.separator + "mqtt_manager.py";
                System.out.println("SpaDes is in the list of cost evaluators.");
                // You can start the Python server here if needed
                //serverManager.startServer(5000, serverScriptPath);
            
            } else {
                System.out.println("SpaDes is not in the list of cost evaluators.");
            }

            if (scienceEvaluators.containsKey("TAT-C")) {
                evaluatorModulePath = tatcRoot + File.separator + "Evaluators_Module" + File.separator + "TAT-C";
                serverScriptPath = evaluatorModulePath + File.separator + "tatc_server.py";
                System.out.println("TAT-C is in the list of science evaluators.");
                evaluatorModulePath = tatcRoot + File.separator + "Evaluators_Module" + File.separator + "TAT-C";
                serverScriptPath = evaluatorModulePath + File.separator + "mqtt_manager.py";
                // You can start the Python server here if needed
                //serverManager.startServer(5001, serverScriptPath);
            
            } else {
                System.out.println("TAT-C is not in the list of cost evaluators.");
            }
            TradespaceSearch tsr = JSONIO.readJSON( new File(System.getProperty("tatc.input")),
            TradespaceSearch.class);
    
            ProblemProperties searchProperties = this.createProblemProperties(tsr,tseRequest);

            TradespaceSearchStrategy problem = this.createTradespaceSearchtrategy(tsr, searchProperties);
            String brokerUrl = "tcp://localhost:1883"; 
            String clientId = "TSE_Client";
            int qos = 1;
        
            // Initialize Publisher and Subscriber
            TSEPublisher publisher = new TSEPublisher(brokerUrl, clientId + "_Publisher");
            TSESubscriber subscriber = new TSESubscriber(brokerUrl, clientId + "_Subscriber");
            

            problem.start();

            //Delete cache directory after tat-c run
            String cacheDirectory = System.getProperty("tatc.output")+ File.separator + "cache";
            if(!ResultIO.deleteDirectory(new File(cacheDirectory))){
                System.out.println("Problem occurs when deleting the cache directory");
            }
        } catch (IOException e) {
            System.out.println("Error reading the JSON file: " + e.getMessage());
            e.printStackTrace();
        }



    }
    /**
     * Method that evaluates an arch.json file using the architecture evaluator (arch_eval.py) located in
     * the demo folder. In this method we are calling python from java.
     * @param architectureJSONFile the architecture file that needs to be evaluated
     */
    public static void evaluateArchitecture(File architectureJsonFile, ProblemProperties properties) throws IOException, InterruptedException {
        // Read the JSON content from the architecture file
        String jsonContent;
        try {
            jsonContent = new String(Files.readAllBytes(architectureJsonFile.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error reading the JSON file: " + e.getMessage());
            throw e;
        }

        // Retrieve metric topics from properties
        Map<String, String> metricTopics = properties.getMetricTopics();   // Map of metrics to their topics

        // Prepare the architecture JSON and unique workflow ID
        JSONObject architectureJson = new JSONObject(jsonContent);
        String workflowId = UUID.randomUUID().toString(); // Unique ID for the workflow

        // Prepare a set to keep track of all expected metrics
        HashSet<String> expectedMetrics = new HashSet<>(metricTopics.keySet());

        // Initialize MQTT Publisher and Subscriber
        String brokerUrl = "tcp://localhost:1883";
        String clientId = "TSE_Client_" + UUID.randomUUID();
        int qos = 1;

        TSEPublisher publisher = new TSEPublisher(brokerUrl, clientId + "_Publisher");
        TSESubscriber subscriber = new TSESubscriber(brokerUrl, clientId + "_Subscriber");

        // Map to store results from evaluators
        Map<String, Double> metricResults = new HashMap<>();

        // Create a CountDownLatch to wait for all metric results
        CountDownLatch latch = new CountDownLatch(expectedMetrics.size());

        try {
            // Connect to the MQTT broker
            publisher.connect();
            subscriber.connect();

            // Subscribe to the result topic
            String resultTopic = "TSE";
            subscriber.subscribe(resultTopic, qos, (topic, payload) -> {
                try {
                    JSONObject responseJson = new JSONObject(payload);
                    String responseWorkflowId = responseJson.getString("workflow_id");

                    // Check if the response corresponds to our request
                    if (!responseWorkflowId.equals(workflowId)) {
                        return; // Ignore messages not related to our request
                    }

                    // Extract results
                    JSONObject results = responseJson.getJSONObject("results");

                    synchronized (metricResults) {
                        // Store each metric received
                        for (String metric : results.keySet()) {
                            double value = results.getDouble(metric);
                            metricResults.put(metric, value);
                            latch.countDown(); // Decrement the latch for each metric received
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });

            // Generate and publish requests for each metric
            for (Map.Entry<String, String> entry : metricTopics.entrySet()) {
                String metric = entry.getKey();
                String topic = entry.getValue(); // e.g., "evaluators/TATC/CoverageAnalysis"

                // Parse the topic to get evaluator and function
                String[] topicParts = topic.split("/");
                if (topicParts.length != 3) {
                    System.err.println("Invalid topic format for metric " + metric + ": " + topic);
                    continue; // Skip invalid topic
                }
                String evaluatorName = topicParts[1]; // e.g., "TATC"
                String functionName = topicParts[2];  // e.g., "CoverageAnalysis"

                // Build the request JSON for this function
                JSONObject evaluatorRequestJson = new JSONObject();
                evaluatorRequestJson.put("architecture", architectureJson);
                evaluatorRequestJson.put("workflow_id", workflowId);
                evaluatorRequestJson.put("function", functionName);
                evaluatorRequestJson.put("metric", metric);
                evaluatorRequestJson.put("result_topic", "TSE"); // The topic to return results to

                // Publish the request to the topic
                publisher.publish(topic, evaluatorRequestJson.toString(), qos);

                System.out.println("Published request for metric '" + metric + "' to topic '" + topic + "'");
                System.out.println("Request JSON: " + evaluatorRequestJson.toString(2)); // Pretty-print with indentation
            }

            // Wait for responses from all evaluators or timeout after a certain period
            boolean allResponsesReceived = latch.await(300, TimeUnit.SECONDS);
            if (!allResponsesReceived) {
                throw new IOException("Did not receive responses for all metrics within the timeout period.");
            }

            // Process the results
            String folderPath = architectureJsonFile.getParent();

            // Example processing using modifyLifecycleCost and modifyCoverageMetrics functions
            if (!metricResults.isEmpty()) {
                // Process LifecycleCost metric
                if (metricResults.containsKey("LifecycleCost")) {
                    double cost = metricResults.get("LifecycleCost");
                    modifyLifecycleCost(folderPath, cost);
                } else {
                    System.err.println("LifecycleCost metric not received.");
                }

                // Process CoverageFraction and HarmonicMeanRevisitTime metrics
                if (metricResults.containsKey("CoverageFraction") || metricResults.containsKey("HarmonicMeanRevisitTime")) {
                    double coverageFraction = metricResults.getOrDefault("CoverageFraction", 0.0);
                    double revisitTime = metricResults.getOrDefault("HarmonicMeanRevisitTime", 0.0);

                    // For demonstration, use revisitTime for avg, max, and min
                    double[] revisitTimes = { revisitTime, revisitTime, revisitTime };
                    double[] responseTimes = { revisitTime, revisitTime, revisitTime };
                    double coverage = coverageFraction;

                    modifyCoverageMetrics(folderPath, revisitTimes, responseTimes, coverage);
                } else {
                    System.err.println("Coverage metrics not received.");
                }

                // Process other metrics as needed
                // You can add additional processing for other metrics here

            } else {
                System.err.println("No metrics received.");
            }

        } catch (MqttException e) {
            e.printStackTrace();
            throw new IOException("MQTT communication error", e);
        }
        // } finally {
        //     // Disconnect from the MQTT broker
        //     try {
        //         publisher.disconnect();
        //         subscriber.disconnect();
        //     } catch (MqttException e) {
        //         e.printStackTrace();
        //     }
        // }
    }
    public static void modifyLifecycleCost(String jsonFilePath, double totalMissionCosts) {
        String costRiskFilePath = jsonFilePath + File.separator + "CostRisk_output.json";
        JSONObject data;
        try {
            Path path = Paths.get(costRiskFilePath);
            if (Files.exists(path)) {
                // Read the JSON file content into a String
                String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
                // Parse the JSON content into a JSONObject
                data = new JSONObject(content);
            } else {
                // File does not exist, create a new JSONObject
                data = new JSONObject();
                // Initialize the lifecycleCost object
                data.put("lifecycleCost", new JSONObject());
                System.out.println("CostRisk_output.json does not exist. Created a new file.");
            }

            // Update the lifecycleCost estimate with the total mission cost
            JSONObject lifecycleCost = data.getJSONObject("lifecycleCost");
            lifecycleCost.put("estimate", totalMissionCosts);
            lifecycleCost.put("fiscalYear", 2024);

            // Save the updated JSON back to the file
            Files.write(path, data.toString(4).getBytes(StandardCharsets.UTF_8));

            System.out.println("Lifecycle cost updated with total mission costs: " + totalMissionCosts);

        } catch (IOException e) {
            System.err.println("Error while reading or writing JSON file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static void modifyCoverageMetrics(String jsonFilePath, double[] revisitTime, double[] responseTime, double coverage) {
        String costRiskFilePath = jsonFilePath + File.separator + "gbl.json";
        JSONObject data;
        try {
            Path path = Paths.get(costRiskFilePath);
            if (Files.exists(path)) {
                // Read the JSON file content into a String
                String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
                // Parse the JSON content into a JSONObject
                data = new JSONObject(content);
            } else {
                // File does not exist, create a new JSONObject
                data = new JSONObject();
                // Initialize the lifecycleCost object
                data.put("RevisitTime", new JSONObject());
                data.put("ResponseTime", new JSONObject());
                data.put("Coverage", new JSONObject());
                System.out.println("gbl.json does not exist. Created a new file.");
            }

            // Update the lifecycleCost estimate with the total mission cost
            JSONObject revTime = data.getJSONObject("RevisitTime");
            revTime.put("avg", revisitTime[0]);
            revTime.put("max", revisitTime[1]);
            revTime.put("min", revisitTime[2]);
            JSONObject resTime = data.getJSONObject("ResponseTime");
            resTime.put("avg", responseTime[0]);
            resTime.put("max", responseTime[1]);
            resTime.put("min", responseTime[2]);
            data.put("Coverage", coverage);
            // Save the updated JSON back to the file
            Files.write(path, data.toString(4).getBytes(StandardCharsets.UTF_8));

            System.out.println("Revisit Time updated with avg value: " + revisitTime[0]);
            System.out.println("Response Time updated with avg value: " + responseTime[0]);
            System.out.println("Coverage updated with avg value: " + coverage);

        } catch (IOException e) {
            System.err.println("Error while reading or writing JSON file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    /**
     * Creates the search strategy (FF, MOEA, AOS or KDO)
     * @param tsr the tradespace search request
     * @param searchProperties the problem properties
     * @return the search strategy
     * @throws IllegalArgumentException
     */
    private TradespaceSearchStrategy createTradespaceSearchtrategy(TradespaceSearch tsr, ProblemProperties searchProperties) throws IllegalArgumentException {
        if (tsr.getSettings().getSearchStrategy() == null){
            throw new IllegalArgumentException("Search Strategy cannot be null. It has to be either FF, MOEA, AOS or KDO.");
        }

        switch (tsr.getSettings().getSearchStrategy()) {
            case "FF":
                return new TradespaceSearchStrategyFF(searchProperties);
            case "GA":
            case "MOEA":
                return new TradespaceSearchStrategyMOEA(searchProperties);
            case "AOS":
                return new TradespaceSearchStrategyAOS(searchProperties);
            case "KDO":
            //                return new TradespaceSearchStrategyKDO(searchProperties);
            default:
                throw new IllegalArgumentException("Search Strategy has to be either FF, MOEA, AOS or KDO.");
        }
    }

    /**
     * Creates the problem properties given a tradespace search request
     * @param tsr the tradespace search request
     * @return
     * @throws IllegalArgumentException
     */
    private ProblemProperties createProblemProperties(TradespaceSearch tsr, JSONObject tseRequest) throws IllegalArgumentException {
        return new ProblemProperties(tsr, tseRequest);
    }
    public static File findProjectRoot(File currentDir, String markerName) {
        File dir = currentDir;
        while (dir != null) {
            // Check if the marker file or directory exists in the current directory
            File[] matchingFiles = dir.listFiles((d, name) -> name.equals(markerName));
            if (matchingFiles != null && matchingFiles.length > 0) {
                // Marker found, return the current directory
                return dir;
            }
            // Move up to the parent directory
            dir = dir.getParentFile();
        }
        // Return null if the root was not found
        return null;
    }
    /**
     * Sets some system properties for easy access of root, input, output and arch_eval.py paths
     */
    public void setDirectories() {
        //TODO: Here is where the system variables are created
        File mainPath = new File(System.getProperty("user.dir"));
        while(!mainPath.getName().equals("3D-Chess-augmentation")){
            mainPath=mainPath.getParentFile();
        }
        //File mainPath = findProjectRoot(new File(System.getProperty("user.dir")), ".project_marker");

        System.setProperty("tatc.root", mainPath.getAbsolutePath());
        File tempInputPath = new File(this.iPath);
        File tempOutputPath = new File(this.oPath);
        if (tempInputPath.isAbsolute()) {
            System.setProperty("tatc.input", this.iPath);
        }
        else { 
            System.setProperty("tatc.input", System.getProperty("tatc.root")+ File.separator + this.iPath);
        }
        if (tempOutputPath.isAbsolute()) {
            System.setProperty("tatc.output", this.oPath);
        }
        else {
            System.setProperty("tatc.output", System.getProperty("tatc.root")+ File.separator + this.oPath);
        }
        File file = new File(System.getProperty("tatc.output"));
        if (!file.exists()) {
            file.mkdirs();
        }
        System.setProperty("tatc.archevalPath", System.getProperty("tatc.root")+File.separator + "TSE_Module"+File.separator + "demo" + File.separator + "bin" + File.separator + "arch_eval.py");
        System.setProperty("tatc.costEvalPath", System.getProperty("tatc.root")+File.separator + "Evaluator_Module" +File.separator + "SpaDes" + File.separator + "server.py");
        System.setProperty("tatc.costFilePath", System.getProperty("tatc.root")+File.separator + "TSE_Module"+File.separator + "tse" + File.separator + "results" + File.separator+"arch-0" + File.separator + "CostRisk_output.json");
        System.setProperty("tatc.numThreads", "16");
    }

}
