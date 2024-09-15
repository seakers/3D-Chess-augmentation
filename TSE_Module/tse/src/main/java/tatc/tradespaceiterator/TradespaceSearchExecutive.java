package tatc.tradespaceiterator;

import tatc.ResultIO;
import tatc.architecture.specifications.Architecture;
import tatc.architecture.specifications.TradespaceSearch;
import tatc.util.JSONIO;
import java.io.*;
import java.nio.charset.StandardCharsets;

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
    public static void evaluateArchitecture(File architectureJSONFile, ProblemProperties properties) {
        Architecture arch = JSONIO.readJSON( architectureJSONFile, Architecture.class);

        ProcessBuilder builder = new ProcessBuilder();

        String inputPath = System.getProperty("tatc.input");
        String outputPath = System.getProperty("tatc.output")+ File.separator + arch.get_id();
        //String pathArchEvaluator = System.getProperty("tatc.archevalPath");
        String pathArchEvaluator = System.getProperty("tatc.archevalPath");
        String costEvalPath = System.getProperty("tatc.costEvalPath");
        // builder.command("python", pathArchEvaluator, inputPath, outputPath);


        // builder.directory(new File(System.getProperty("tatc.root")+ File.separator + "TSE Module"+File.separator + "demo"));
        
        builder.command("python3.10", costEvalPath, inputPath);

        builder.directory(new File(System.getProperty("tatc.root")+ File.separator + "Evaluator Module"+File.separator + "SpaDes"));

        try {

            Process process = builder.start();

            StringBuilder output = new StringBuilder();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                //System.out.println(output);
            } else {
                InputStream error = process.getErrorStream();
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = error.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
                System.out.println(result.toString(StandardCharsets.UTF_8.name()));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(!new File(outputPath + File.separator + "gbl.json").isFile()){
            evaluateArchitecture(architectureJSONFile, properties);
        }

        boolean keepLowLevelData = properties.getTradespaceSearch().getSettings().getOutputs().isKeepLowLevelData();
        if (!keepLowLevelData){
            // Directory containing files to delete.
            String directory = outputPath;

            // Extension.
            String extension1 = "accessInfo.csv";
            String extension2 = "accessInfo.json";
            String extension3 = "level0_data_metrics.csv";
            String starting1 = "obs";

            try {
                ResultIO.deleteFileWithExtension(directory, extension1);
                ResultIO.deleteFileWithExtension(directory, extension2);
                ResultIO.deleteFileWithExtension(directory, extension3);
                ResultIO.deleteFileWithStarting(directory, starting1);
            } catch (IOException e) {
                System.out.println("Problem occurs when deleting files");
                e.printStackTrace();
            }

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
    private void setDirectories() {
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
        System.setProperty("tatc.archevalPath", System.getProperty("tatc.root")+File.separator + "TSE Module"+File.separator + "demo" + File.separator + "bin" + File.separator + "arch_eval.py");
        System.setProperty("tatc.costEvalPath", System.getProperty("tatc.root")+File.separator + "Evaluator Module" +File.separator + "SpaDes" + File.separator + "ConstellationDesignMain.py");
        System.setProperty("tatc.numThreads", "16");
    }
}
