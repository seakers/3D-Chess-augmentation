package tatc.tradespaceiterator;
import tatc.PythonServerManager;
import tatc.ResultIO;
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

import tatc.tradespaceiterator.TSERequestParser;
import org.json.JSONObject;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
            List<String> costEvaluators = parser.getCostEvaluators(tseRequest);
            List<String> scienceEvaluators = parser.getScienceEvaluators(tseRequest);
            if (costEvaluators.contains("SpaDes")) {
                evaluatorModulePath = tatcRoot + File.separator + "Evaluators_Module" + File.separator + "SpaDes";
                serverScriptPath = evaluatorModulePath + File.separator + "server.py";
                System.out.println("SpaDes is in the list of cost evaluators.");
                // You can start the Python server here if needed
                serverManager.startServer(5000, serverScriptPath);
            
            } else {
                System.out.println("SpaDes is not in the list of cost evaluators.");
            }

            if (scienceEvaluators.contains("TAT-C")) {
                evaluatorModulePath = tatcRoot + File.separator + "Evaluators_Module" + File.separator + "TAT-C";
                serverScriptPath = evaluatorModulePath + File.separator + "tatc_server.py";
                System.out.println("TAT-C is in the list of science evaluators.");
                evaluatorModulePath = tatcRoot + File.separator + "Evaluators_Module" + File.separator + "TAT-C";
                serverScriptPath = evaluatorModulePath + File.separator + "tatc_server.py";
                // You can start the Python server here if needed
                serverManager.startServer(5001, serverScriptPath);
            
            } else {
                System.out.println("TAT-C is not in the list of cost evaluators.");
            }
        } catch (IOException e) {
            System.out.println("Error reading the JSON file: " + e.getMessage());
            e.printStackTrace();
        }


        TradespaceSearch tsr = JSONIO.readJSON( new File(System.getProperty("tatc.input")),
                TradespaceSearch.class);
        
        ProblemProperties searchProperties = this.createProblemProperties(tsr);

        TradespaceSearchStrategy problem = this.createTradespaceSearchtrategy(tsr, searchProperties);

        problem.start();

        //Delete cache directory after tat-c run
        String cacheDirectory = System.getProperty("tatc.output")+ File.separator + "cache";
        if(!ResultIO.deleteDirectory(new File(cacheDirectory))){
            System.out.println("Problem occurs when deleting the cache directory");
        }
    }
    // public static void evaluateArchitecture(File architectureJsonFile, ProblemProperties properties) throws IOException {
    //     // Read the JSON content from the architecture file
    //     String jsonContent;
    //     try {
    //         jsonContent = new String(Files.readAllBytes(architectureJsonFile.toPath()), StandardCharsets.UTF_8);
    //         //System.out.println("Read JSON content: " + jsonContent);
    //     } catch (IOException e) {
    //         System.err.println("Error reading the JSON file: " + e.getMessage());
    //         e.printStackTrace();
    //         throw e;
    //     }
    
    //     // Prepare the request JSON with architecture and folder path
    //     JSONObject architectureJson = new JSONObject(jsonContent);
    //     JSONObject requestJson = new JSONObject();
    //     requestJson.put("architecture", architectureJson);
    //     requestJson.put("folderPath", architectureJsonFile.getParent()); // Add folder path
    //     //System.out.println("Prepared request JSON: " + requestJson.toString());
    
    //     // Send HTTP POST request to the Python server
    //     URL url = new URL("http://localhost:5000/evaluate");
    //     HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    
    //     // Set request properties
    //     conn.setRequestMethod("POST");
    //     conn.setRequestProperty("Content-Type", "application/json; utf-8");
    //     conn.setDoOutput(true);
    
    //     // Write JSON data to request body
    //     try (OutputStream os = conn.getOutputStream()) {
    //         byte[] input = requestJson.toString().getBytes(StandardCharsets.UTF_8);
    //         os.write(input, 0, input.length);
    //         System.out.println("Sent JSON data to server.");
    //     } catch (IOException e) {
    //         System.err.println("Error writing to the output stream: " + e.getMessage());
    //         e.printStackTrace();
    //         throw e;
    //     }
    
    //     // Check response code
    //     int responseCode = conn.getResponseCode();
    //     System.out.println("Received HTTP response code: " + responseCode);
    
    //     if (responseCode != HttpURLConnection.HTTP_OK) {
    //         // Read error stream for more details
    //         String errorResponse = "";
    //         try (BufferedReader br = new BufferedReader(
    //                 new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
    //             StringBuilder response = new StringBuilder();
    //             String responseLine;
    //             while ((responseLine = br.readLine()) != null) {
    //                 response.append(responseLine.trim());
    //             }
    //             errorResponse = response.toString();
    //             System.err.println("Error response: " + errorResponse);
    //         } catch (IOException e) {
    //             // Ignore, as we already have the response code
    //         }
    //         throw new IOException("HTTP error code: " + responseCode + ". Error response: " + errorResponse);
    //     }
    
    //     // Read the response
    //     double cost;
    //     Random random = new Random();
    //     try (BufferedReader br = new BufferedReader(
    //             new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
    
    //         StringBuilder response = new StringBuilder();
    //         String responseLine;
    //         while ((responseLine = br.readLine()) != null) {
    //             response.append(responseLine.trim());
    //         }
    
    //         // Parse the response JSON
    //         System.out.println("Received response: " + response.toString());
    //         JSONObject responseJson = new JSONObject(response.toString());
    //         cost = responseJson.getDouble("cost");
    //         System.out.println("Extracted cost: " + cost);
    //         String folder_path = architectureJsonFile.getParent();
    //         modifyLifecycleCost(folder_path,cost);
    //         double[] revisitTime = new double[3];
    //         double[] responseTime = new double[3];
    //         for (int i = 0; i < 3; i++) {
    //             revisitTime[i] = 10 + (90 * random.nextDouble()); // Random between 10 and 100
    //             responseTime[i] = 10 + (90 * random.nextDouble()); // Random between 10 and 100
    //         }
    
    //         double coverage = random.nextDouble()*100;
    //         modifyCoverageMetrics(folder_path, revisitTime, responseTime, coverage);
    //     }
    // }
    /**
     * Method that evaluates an arch.json file using the architecture evaluator (arch_eval.py) located in
     * the demo folder. In this method we are calling python from java.
     * @param architectureJSONFile the architecture file that needs to be evaluated
     */
    public static void evaluateArchitecture(File architectureJsonFile, ProblemProperties properties) throws IOException {
        // Read the JSON content from the architecture file
        String jsonContent;
        try {
            jsonContent = new String(Files.readAllBytes(architectureJsonFile.toPath()), StandardCharsets.UTF_8);
            //System.out.println("Read JSON content: " + jsonContent);
        } catch (IOException e) {
            System.err.println("Error reading the JSON file: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        // Prepare the request JSON with architecture and folder path
        JSONObject architectureJson = new JSONObject(jsonContent);
        JSONObject requestJson = new JSONObject();
        requestJson.put("architecture", architectureJson);
        requestJson.put("folderPath", architectureJsonFile.getParent()); // Add folder path
        //System.out.println("Prepared request JSON: " + requestJson.toString());

        // Create an ExecutorService to run tasks in parallel
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Task for SpaDes server (cost evaluation)
        Callable<Double> costTask = () -> {
            // Send HTTP POST request to the SpaDes server
            URL url = new URL("http://localhost:5000/evaluate");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Set request properties
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            // Write JSON data to request body
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestJson.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
                System.out.println("Sent JSON data to SpaDes server.");
            } catch (IOException e) {
                System.err.println("Error writing to the SpaDes server output stream: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }

            // Check response code
            int responseCode = conn.getResponseCode();
            System.out.println("Received HTTP response code from SpaDes server: " + responseCode);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                // Read error stream for more details
                String errorResponse = "";
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    errorResponse = response.toString();
                    System.err.println("Error response from SpaDes server: " + errorResponse);
                } catch (IOException e) {
                    // Ignore, as we already have the response code
                }
                throw new IOException("HTTP error code from SpaDes server: " + responseCode + ". Error response: " + errorResponse);
            }

            // Read the response
            double cost;
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                // Parse the response JSON
                System.out.println("Received response from SpaDes server: " + response.toString());
                JSONObject responseJson = new JSONObject(response.toString());
                cost = responseJson.getDouble("cost");
                System.out.println("Extracted cost: " + cost);
                return cost;
            }
        };

        // Task for TAT-C server (coverage evaluation)
        Callable<Map<String, Object>> coverageTask = () -> {
            // Send HTTP POST request to the TAT-C server
            URL url = new URL("http://localhost:5001/evaluate");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Set request properties
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            // Write JSON data to request body
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestJson.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
                System.out.println("Sent JSON data to TAT-C server.");
            } catch (IOException e) {
                System.err.println("Error writing to the TAT-C server output stream: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }

            // Check response code
            int responseCode = conn.getResponseCode();
            System.out.println("Received HTTP response code from TAT-C server: " + responseCode);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                // Read error stream for more details
                String errorResponse = "";
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    errorResponse = response.toString();
                    System.err.println("Error response from TAT-C server: " + errorResponse);
                } catch (IOException e) {
                    // Ignore, as we already have the response code
                }
                throw new IOException("HTTP error code from TAT-C server: " + responseCode + ". Error response: " + errorResponse);
            }

            // Read the response
            Map<String, Object> coverageMetrics = new HashMap<>();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                // Parse the response JSON
                System.out.println("Received response from TAT-C server: " + response.toString());
                JSONObject responseJson = new JSONObject(response.toString());

                double harmonicMeanRevisitTime = responseJson.getDouble("harmonicMeanRevisitTime");
                double coverageFraction = responseJson.getDouble("coverageFraction");
                // If responseTime is provided, extract it here
                // double responseTime = responseJson.getDouble("responseTime");

                coverageMetrics.put("harmonicMeanRevisitTime", harmonicMeanRevisitTime);
                coverageMetrics.put("coverageFraction", coverageFraction);
                // coverageMetrics.put("responseTime", responseTime);

                System.out.println("Extracted coverage metrics: " + coverageMetrics);
                return coverageMetrics;
            }
        };

        try {
            // Submit both tasks to the executor
            Future<Double> costFuture = executor.submit(costTask);
            Future<Map<String, Object>> coverageFuture = executor.submit(coverageTask);

            // Wait for both tasks to complete and get the results
            double cost = costFuture.get();
            Map<String, Object> coverageMetrics = coverageFuture.get();

            // Now process the results
            String folder_path = architectureJsonFile.getParent();
            modifyLifecycleCost(folder_path, cost);

            // Extract revisitTime, responseTime, and coverage from coverageMetrics
            double harmonicMeanRevisitTime = (double) coverageMetrics.get("harmonicMeanRevisitTime");
            double coverageFraction = (double) coverageMetrics.get("coverageFraction");

            // For demonstration, we'll use the harmonic mean revisit time for avg, max, and min
            double[] revisitTime = {harmonicMeanRevisitTime, harmonicMeanRevisitTime, harmonicMeanRevisitTime};
            double[] responseTime = {harmonicMeanRevisitTime, harmonicMeanRevisitTime, harmonicMeanRevisitTime};
            double coverage = coverageFraction; // Assuming coverageFraction is in percentage

            modifyCoverageMetrics(folder_path, revisitTime, responseTime, coverage);

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Error during evaluation", e);
        } finally {
            executor.shutdown();
        }
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
    private ProblemProperties createProblemProperties(TradespaceSearch tsr) throws IllegalArgumentException {
        return new ProblemProperties(tsr);
    }

    /**
     * Sets some system properties for easy access of root, input, output and arch_eval.py paths
     */
    public void setDirectories() {
        //TODO: Here is where the system variables are created
        File mainPath = new File(System.getProperty("user.dir"));
        while(!mainPath.getName().equals("3D-CHESS augmentation")){
            mainPath=mainPath.getParentFile();
        }

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
