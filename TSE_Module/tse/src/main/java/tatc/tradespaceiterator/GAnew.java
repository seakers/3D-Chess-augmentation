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
import tatc.decisions.Decision;
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
                    solution.setObjective(objIndex++, obj.getValue());
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

    /**
     * Decode solution variables into architecture parameters by querying each decision.
     */
    // private Map<String,Object> decodeSolution(Solution solution) {
    //     Map<String,Object> archParams = new HashMap<>();
    //     int offset = 0;
    //     for (Decision d : decisions) {
    //         int numVars = d.getNumberOfVariables();
    //         // Extract relevant slice of solution for this decision
    //         int[] encoding = new int[numVars];
    //         for (int i=0; i<numVars; i++){
    //             double val = ((RealVariable)solution.getVariable(offset+i)).getValue();
    //             // Convert val to int
    //             encoding[i] = (int)Math.round(val);
    //         }
    //         offset += numVars;

    //         Map<String,Object> partialArch = d.decodeArchitecture(encoding);
    //         archParams.putAll(partialArch);
    //     }
    //     return archParams;
    // }
    private List<Map<String, Object>> decodeSolution(Solution solution) {
        // Start with a single empty architecture
        List<Map<String, Object>> archSet = new ArrayList<>();
        archSet.add(new HashMap<>());
    
        int offset = 0;
        for (Decision d : decisions) {
            if(!(d instanceof Combining)){
                            // Extract the encoding for this decision from the solution
            Object encoded = d.extractEncodingFromSolution(solution, offset);
            offset += d.getNumberOfVariables();
            // Use the decision's decode method to transform archSet
            archSet = d.decodeArchitecture(encoded, archSet);
            }
        }
    
        return archSet;
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
        // First, determine total number of variables by summing over all decisions
        int totalVars = 0;
        for (Decision d : decisions) {
            totalVars += d.getNumberOfVariables();
        }
        return new AdgSolution(graph, properties, totalObjectives, totalVariables) ;
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
