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
import java.util.List;
import tatc.tradespaceiterator.TSERequestParser;
import org.json.JSONObject;
import java.util.Random;
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
        try{
            String content = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
            JSONObject tseRequest = new JSONObject(content);
            List<String> costEvaluators = parser.getCostEvaluators(tseRequest);
            if (costEvaluators.contains("SpaDes")) {
                System.out.println("SpaDes is in the list of cost evaluators.");
                // You can start the Python server here if needed
                PythonServerManager.startServer();
            } else {
                System.out.println("SpaDes is not in the list of cost evaluators.");
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

    /**
     * Method that evaluates an arch.json file using the architecture evaluator (arch_eval.py) located in
     * the demo folder. In this method we are calling python from java.
     * @param architectureJSONFile the architecture file that needs to be evaluated
     */
    // public static void evaluateArchitecture(File architectureJSONFile, ProblemProperties properties) throws IOException{
    //     Architecture arch = JSONIO.readJSON( architectureJSONFile, Architecture.class);
    //     try{
    //     URL url = new URL("http://localhost:5000/evaluate");
    //     HttpURLConnection conn = (HttpURLConnection) url.openConnection();

    //     // Set request properties
    //     conn.setRequestMethod("POST");
    //     conn.setRequestProperty("Content-Type", "application/json; utf-8");
    //     conn.setDoOutput(true);

    //     // Write JSON data to request body
    //     try (OutputStream os = conn.getOutputStream()) {
    //         byte[] input = architectureJSONFile.toString().getBytes("utf-8");
    //         os.write(input, 0, input.length);
    //     }

    //     // Check response code
    //     int responseCode = conn.getResponseCode();
    //     if (responseCode != HttpURLConnection.HTTP_OK) {
    //         throw new IOException("HTTP error code: " + responseCode);
    //     }

    //     // Read the response
    //     try (BufferedReader br = new BufferedReader(
    //             new InputStreamReader(conn.getInputStream(), "utf-8"))) {

    //         StringBuilder response = new StringBuilder();
    //         String responseLine;
    //         while ((responseLine = br.readLine()) != null) {
    //             response.append(responseLine.trim());
    //         }

    //         // Parse the response JSON
    //         JSONObject responseJson = new JSONObject(response.toString());
    //         double cost = responseJson.getDouble("cost");
    //     ProcessBuilder builder = new ProcessBuilder();

    //     String inputPath = System.getProperty("tatc.input");
    //     String outputPath = System.getProperty("tatc.output")+ File.separator + arch.get_id();
    //     //String pathArchEvaluator = System.getProperty("tatc.archevalPath");
    //     String pathArchEvaluator = System.getProperty("tatc.archevalPath");
    //     String costEvalPath = System.getProperty("tatc.costEvalPath");
    //     // builder.command("python", pathArchEvaluator, inputPath, outputPath);
    //     String costFilePath = System.getProperty("tatc.costFilePath");

    //     // builder.directory(new File(System.getProperty("tatc.root")+ File.separator + "TSE_Module"+File.separator + "demo"));
        
    //     builder.command("python", costEvalPath);

    //     builder.directory(new File(System.getProperty("tatc.root")+ File.separator + "Evaluator_Module"+File.separator + "SpaDes"));

    //     try {

    //         Process process = builder.start();

    //         StringBuilder output = new StringBuilder();

    //         BufferedReader reader = new BufferedReader(
    //                 new InputStreamReader(process.getInputStream()));

    //         String line;
    //         while ((line = reader.readLine()) != null) {
    //             output.append(line + "\n");
    //         }

    //         int exitVal = process.waitFor();
    //         if (exitVal == 0) {
    //             //System.out.println(output);
    //         } else {
    //             InputStream error = process.getErrorStream();
    //             ByteArrayOutputStream result = new ByteArrayOutputStream();
    //             byte[] buffer = new byte[1024];
    //             int length;
    //             while ((length = error.read(buffer)) != -1) {
    //                 result.write(buffer, 0, length);
    //             }
    //             System.out.println(result.toString(StandardCharsets.UTF_8.name()));
    //         }

    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     } catch (InterruptedException e) {
    //         e.printStackTrace();
    //     }

    //     if(!new File(outputPath + File.separator + "gbl.json").isFile()){
    //         evaluateArchitecture(architectureJSONFile, properties);
    //     }

    //     boolean keepLowLevelData = properties.getTradespaceSearch().getSettings().getOutputs().isKeepLowLevelData();
    //     if (!keepLowLevelData){
    //         // Directory containing files to delete.
    //         String directory = outputPath;

    //         // Extension.
    //         String extension1 = "accessInfo.csv";
    //         String extension2 = "accessInfo.json";
    //         String extension3 = "level0_data_metrics.csv";
    //         String starting1 = "obs";

    //         try {
    //             ResultIO.deleteFileWithExtension(directory, extension1);
    //             ResultIO.deleteFileWithExtension(directory, extension2);
    //             ResultIO.deleteFileWithExtension(directory, extension3);
    //             ResultIO.deleteFileWithStarting(directory, starting1);
    //         } catch (IOException e) {
    //             System.out.println("Problem occurs when deleting files");
    //             e.printStackTrace();
    //         }
    //     }

    // }
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
    
        // Send HTTP POST request to the Python server
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
            System.out.println("Sent JSON data to server.");
        } catch (IOException e) {
            System.err.println("Error writing to the output stream: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    
        // Check response code
        int responseCode = conn.getResponseCode();
        System.out.println("Received HTTP response code: " + responseCode);
    
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
                System.err.println("Error response: " + errorResponse);
            } catch (IOException e) {
                // Ignore, as we already have the response code
            }
            throw new IOException("HTTP error code: " + responseCode + ". Error response: " + errorResponse);
        }
    
        // Read the response
        double cost;
        Random random = new Random();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
    
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
    
            // Parse the response JSON
            System.out.println("Received response: " + response.toString());
            JSONObject responseJson = new JSONObject(response.toString());
            cost = responseJson.getDouble("cost");
            System.out.println("Extracted cost: " + cost);
            String folder_path = architectureJsonFile.getParent();
            modifyLifecycleCost(folder_path,cost);
            double[] revisitTime = new double[3];
            double[] responseTime = new double[3];
            for (int i = 0; i < 3; i++) {
                revisitTime[i] = 10 + (90 * random.nextDouble()); // Random between 10 and 100
                responseTime[i] = 10 + (90 * random.nextDouble()); // Random between 10 and 100
            }
    
            double coverage = random.nextDouble()*100;
            modifyCoverageMetrics(folder_path, revisitTime, responseTime, coverage);
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
