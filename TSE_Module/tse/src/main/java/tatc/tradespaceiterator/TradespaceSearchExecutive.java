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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tatc.TSESubscriber;
import tatc.TSESubscriber;
import tatc.tradespaceiterator.TSERequestParser;

import org.eclipse.paho.client.mqttv3.MqttException;
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
        String jsonFilePath = "TSERequestExample.json";
        PythonServerManager serverManager = new PythonServerManager();
        try{
            String evaluatorModulePath;
            String serverScriptPath;
            String content = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
            JSONObject tseRequest = new JSONObject(content);
            String tatcRoot = System.getProperty("tatc.root");
            Map<String, List<String>> costEvaluators = parser.getCostEvaluators(tseRequest);
            Map<String, List<String>> scienceEvaluators = parser.getScienceEvaluators(tseRequest);
            // Store the parsed evaluators and metrics
            this.costEvaluators = costEvaluators;
            this.scienceEvaluators = scienceEvaluators;
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
    Map<String, List<String>> costEvaluators = properties.getCostEvaluators();
    Map<String, List<String>> scienceEvaluators = properties.getScienceEvaluators();
    // Prepare the request JSON with architecture and folder path
    JSONObject architectureJson = new JSONObject(jsonContent);
    JSONObject requestJson = new JSONObject();
    requestJson.put("architecture", architectureJson);
    requestJson.put("folderPath", architectureJsonFile.getParent());
    requestJson.put("workflow_id", UUID.randomUUID().toString()); // Unique ID for the workflow

    String brokerUrl = "tcp://localhost:1883"; 
    String clientId = "TSE_Client";
    int qos = 1;

    // Initialize Publisher and Subscriber
    TSEPublisher publisher = new TSEPublisher(brokerUrl, clientId + "_Publisher");
    TSESubscriber subscriber = new TSESubscriber(brokerUrl, clientId + "_Subscriber");

    // Create a CountDownLatch to wait for both responses
    CountDownLatch latch = new CountDownLatch(2);
    // Calculate the total number of evaluators (cost + science)
    // int totalEvaluators = costEvaluators.size() + scienceEvaluators.size();
    // CountDownLatch latch = new CountDownLatch(totalEvaluators);

    // Maps to store results from evaluators
    Map<String, Double> costResult = new HashMap<>();
    Map<String, Object> coverageMetrics = new HashMap<>();

    try {
        // Connect to the MQTT broker
        publisher.connect();
        subscriber.connect();

        // Subscribe to evaluation results
        String resultTopic = "evaluation/results/#"; // Subscribe to all results
        subscriber.subscribe(resultTopic, qos, (topic, payload) -> {
            try {
                JSONObject responseJson = new JSONObject(payload);
                String evaluator = responseJson.getString("evaluator");
                String workflowId = responseJson.getString("workflow_id");
        
                // Check if the response corresponds to our request
                if (!workflowId.equals(requestJson.getString("workflow_id"))) {
                    return; // Ignore messages not related to our request
                }
        
                // Dynamic handling of evaluators based on their metric definitions
                if (costEvaluators.containsKey(evaluator)) {
                    JSONObject results = responseJson.getJSONObject("results");
                    
                    // Iterate over metrics related to the evaluator in cost objectives
                    for (String metric : costEvaluators.get(evaluator)) {
                        double value = results.getDouble(metric);
                        synchronized (costResult) {
                            costResult.put(metric, value); // Store each metric dynamically
                        }
                    }
                    latch.countDown();  // Signal that this evaluator's results have been processed
                } else if (scienceEvaluators.containsKey(evaluator)) {
                    JSONObject results = responseJson.getJSONObject("results");
        
                    // Iterate over metrics related to the evaluator in science objectives
                    for (String metric : scienceEvaluators.get(evaluator)) {
                        double value = results.getDouble(metric);
                        synchronized (coverageMetrics) {
                            coverageMetrics.put(metric, value); // Store each metric dynamically
                        }
                    }
                    latch.countDown();  // Signal that this evaluator's results have been processed
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        

        // Publish the evaluation request
        String requestTopic = "evaluation/requests";
        publisher.publish(requestTopic, requestJson.toString(), qos);

        // Wait for responses from both evaluators or timeout after a certain period
        boolean allResponsesReceived = latch.await(300, TimeUnit.SECONDS);
        if (!allResponsesReceived) {
            throw new IOException("Did not receive responses from all evaluators within the timeout period.");
        }

        // Process the results
        String folderPath = architectureJsonFile.getParent();

        // Update lifecycle cost
        if (costResult.containsKey("cost")) {
            modifyLifecycleCost(folderPath, costResult.get("cost"));
        } else {
            System.err.println("Cost result not received.");
        }

        // Update coverage metrics
        if (!coverageMetrics.isEmpty()) {
            double harmonicMeanRevisitTime = (double) coverageMetrics.get("harmonicMeanRevisitTime");
            double coverageFraction = (double) coverageMetrics.get("coverageFraction");
            // For demonstration, use harmonicMeanRevisitTime for avg, max, and min
            double[] revisitTime = { harmonicMeanRevisitTime, harmonicMeanRevisitTime, harmonicMeanRevisitTime };
            double[] responseTime = { harmonicMeanRevisitTime, harmonicMeanRevisitTime, harmonicMeanRevisitTime };
            double coverage = coverageFraction;
            modifyCoverageMetrics(folderPath, revisitTime, responseTime, coverage);
        } else {
            System.err.println("Coverage metrics not received.");
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
