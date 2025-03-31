package tatc.tradespaceiterator;
import org.moeaframework.core.*;
import org.moeaframework.core.variable.RealVariable;  // We'll use RealVariables for integer encoding
import org.moeaframework.problem.AbstractProblem;
import tatc.decisions.adg.AdgSolution;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

import tatc.decisions.Combining;
import tatc.decisions.ConstructionNode;
import tatc.decisions.Decision;
import tatc.decisions.Partitioning;
import tatc.decisions.adg.AdgSolution;
import tatc.decisions.adg.Graph;
import tatc.architecture.specifications.GroundNetwork;
import tatc.tradespaceiterator.ProblemProperties;
import tatc.architecture.ArchitectureCreatorNew;
import tatc.tradespaceiterator.TradespaceSearchExecutive;
import tatc.util.Summary;
import java.lang.InterruptedException;
public class GAnew extends AbstractProblem {

    private ProblemProperties properties;
    private List<Decision> decisions;
    private int totalVariables;
    private int totalObjectives;
    private int counter;
    private int solutionCounter;
    private Graph graph;
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
        ArchitectureCreatorNew creator = new ArchitectureCreatorNew();
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
            File architectureJsonFile = creator.toJSON(this.counter);
            this.counter++;
            try{
                HashMap<String, Double> objectivesResults = evaluateArchitecture(architectureJsonFile, properties);
                Summary.writeSummaryFileGA(objectivesResults, solution, this.counter, decisions);
                // Set solution objectives
                // Suppose the problem defines how many objectives and their order
                // Just iterate over them from objectivesResults
                int objIndex = 0;
                for (Map.Entry<String, Double> obj : objectivesResults.entrySet()){
                    String type = properties.getObjectives().get(objIndex).getParent().getType();
                    Double objective;
                    if (type.equals("MAX")){
                        objective = -obj.getValue();
                    }else{
                        objective = obj.getValue();
                    }
                    solution.setObjective(objIndex++, objective);
                    if (objIndex >= solution.getNumberOfObjectives()) break;
                }

            }catch (IOException e) {
                System.out.println("Error reading the JSON file: " + e.getMessage());
                e.printStackTrace();
            }


        } else {
            // No architecture created
            for(int i=0; i<solution.getNumberOfObjectives(); i++){
                solution.setObjective(i, Double.POSITIVE_INFINITY);
            }
        }
    }

    private List<Map<String, Object>> decodeSolution(Solution solution) {
        int offset = 0;
    
        // 1) Decode ALL decisions in topological order, but do not merge
        for (Decision d : decisions) {
            // (a) Set inputs from parent decisions/TSERequest
            graph.setInputs(d);
    
            // (b) Extract encoding & apply
            Object encoded = d.extractEncodingFromSolution(solution, offset);
            d.applyEncoding((int[]) encoded);
            offset += d.getNumberOfVariables();
    
            // (c) Decode
            List<Map<String, Object>> partialDecoded = d.decodeArchitecture(encoded, solution, graph);
    
            // (d) Store the result inside the decision for later reference
        }
    
        // 2) Find the ConstructionNode, call its decodeArchitecture again.
        //    This time, the node can gather all partial results from each parent's getResult().
        for (Decision d : decisions) {
            if (d instanceof ConstructionNode) {
                // ConstructionNode typically ignores 'encoded',
                // but we can pass null or an empty array if needed.
                return d.decodeArchitecture(null, solution, graph);
            }
        }
    
        // 3) If no construction node was found, return an empty list
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

    @Override
    public Solution newSolution() {
        // Step 1: Generate encodings, but don't store them in the solution yet
        List<int[]> allEncodings = new ArrayList<>();
        for (Decision d : decisions) {
            if(d instanceof ConstructionNode){
                break;
            }
            // Ensure inputs are set first (this might update entity sets, etc.)
            this.graph.setInputs(d);
    
            // Generate a random encoding for this decision
            Object encoded = d.randomEncoding();
            int[] arr = (int[]) encoded;
            allEncodings.add(arr);
        }
    
        // Step 2: Compute total number of variables from concatenated encodings
        int totalVars = 0;
        for (int[] enc : allEncodings) {
            totalVars += enc.length;
        }
    
        // Now we can create the Solution object with the correct number of variables
        // Assuming we already know the number of objectives (e.g., totalObjectives)
        AdgSolution solution = new AdgSolution(graph, properties, totalObjectives, totalVars);    
        // Step 3: Fill the solution with each decision's encoded representation
        int offset = 0;
        for (int i = 0; i < decisions.size(); i++) {
            Decision d = decisions.get(i);
            // If construction node, stop
            if (d instanceof ConstructionNode) {
                break;
            }
        
            // Retrieve the integer array (already encoded) for this decision
            int[] arr = allEncodings.get(i);
        
            // For each integer in arr, create a RealVariable
            for (int j = 0; j < arr.length; j++) {
                int maxOption = d.getMaxOptionForVariable(j);
                // Ensure maxOption is at least 1 to avoid negative or zero range
                if (maxOption < 1) {
                    maxOption = 1;
                }
        
                // The valid range is [0, (maxOption - 1)]
                double lowerBound = 0.0;
                double upperBound = maxOption - 1;
        
                // Clamp the encoded value into [0, maxOption - 1]
                double clampedValue = arr[j];
                if (clampedValue < lowerBound) {
                    clampedValue = lowerBound;
                } else if (clampedValue > upperBound) {
                    clampedValue = upperBound;
                }
        
                // Create the variable with safe bounds
                RealVariable var = new RealVariable(lowerBound, upperBound);
                var.setValue(clampedValue);
        
                // Place the variable in the MOEA Framework solution
                solution.setVariable(offset + j, var);
            }
        
            // Optionally track this encoding in the decision's map
            d.addEncodingById(solutionCounter, arr);
        
            // Advance the offset
            offset += arr.length;
        }
        
        // Mark the ID for the solution
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
