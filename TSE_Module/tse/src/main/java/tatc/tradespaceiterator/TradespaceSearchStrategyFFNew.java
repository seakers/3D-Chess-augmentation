package tatc.tradespaceiterator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import tatc.architecture.ArchitectureCreator;
import tatc.architecture.constellations.HomogeneousWalkerParametersNew;
import tatc.architecture.*;
import tatc.architecture.specifications.Constellation;
import tatc.architecture.specifications.GroundNetwork;
import tatc.architecture.specifications.Satellite;
import tatc.architecture.variable.Decision;
import tatc.util.JSONIO;
import tatc.util.Summary;
import tatc.architecture.ArchitectureCreatorNew;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class TradespaceSearchStrategyFFNew implements TradespaceSearchStrategy {

    private JSONObject tseRequestJson;
    private ProblemProperties properties;

    public TradespaceSearchStrategyFFNew(ProblemProperties searchProperties) {
        // Read and parse the JSON file using JSONObject
        this.tseRequestJson = searchProperties.getTsrObject();
        this.properties = searchProperties;
        
    }

    public void start() throws IllegalArgumentException {
        // Step 1: Get combining pattern variables and generate architectures (L)
        Map<String, String> decisionVariables = properties.getDecisionVariables();
        Map<String, List<Object>> variableValues = properties.getDecisionVariableValues(decisionVariables);
        // Filter combining pattern variables
        Map<String, List<Object>> combiningVariableValues = new LinkedHashMap<>();
        Map<String, List<Object>> assigningVariableValues = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : decisionVariables.entrySet()) {
            if ("Combining".equalsIgnoreCase(entry.getValue())) {
                combiningVariableValues.put(entry.getKey(), variableValues.get(entry.getKey()));
            }
            else if ("Assigning".equalsIgnoreCase(entry.getValue())){
                assigningVariableValues.put(entry.getKey(),variableValues.get(entry.getKey()));
            }
        }
        if(!assigningVariableValues.isEmpty() && !combiningVariableValues.isEmpty()){
            fullFactorialCombiningAndAssigning(combiningVariableValues, assigningVariableValues);
        }
        else{
            fullFactorialCombining(combiningVariableValues);
        }
    }
    private void fullFactorialCombiningAndAssigning(Map<String, List<Object>> combiningVariableValues,Map<String, List<Object>> assigningVariableValues ){
        List<Map<String, Object>> combiningArchitectures = generateFullFactorialDesign(combiningVariableValues);
        List<Map<Object, Set<Object>>> fullArchitectures = new ArrayList<>();;
        HashMap<String, Decision<?>> decisions = properties.getDecisions();        
        Decision<GroundNetwork> decisionGroundNetwork = (Decision<GroundNetwork>)decisions.get("groundNetwork");
        for (Map.Entry<String, List<Object>> entry : assigningVariableValues.entrySet()){
            List<Object> architectures = new ArrayList<>(combiningArchitectures);
            List<Map<Object, Set<Object>>> allAssignments = assigning(architectures, entry.getValue());
            // Step 4: Combine architectures with assignments
            for (Map<Object, Set<Object>> assignment : allAssignments) {
                fullArchitectures.add(assignment);
            }
            System.out.println("Total number of architectures: " + fullArchitectures.size());
            JSONArray constellationsJSON = tseRequestJson.getJSONObject("designSpace").getJSONArray("spaceSegment");
            List<JSONObject> architecturesJson = new ArrayList<>();
            // Loop over each set of architecture parameters
            Collections.shuffle(fullArchitectures);
            int k = 0;
            for (GroundNetwork gn : decisionGroundNetwork.getAllowedValues()) {
                for (Map<Object, Set<Object>> archParameters : fullArchitectures) {
                    // Create a new architecture JSON object
                    ArchitectureCreatorNew creator = new ArchitectureCreatorNew(properties);
                    // Initialize the spaceSegment JSONArray
                    Map<String, Object> architecture = new HashMap<>();
                    // Extract architecture parameters from assignment keys
                    int i = 0;
                    JSONObject constJson = constellationsJSON.getJSONObject(i);
                    for (Object archObj : archParameters.keySet()) {
                        if (archObj instanceof Map) {
                            Map<String, Object> archMap = (Map<String, Object>) archObj;
                            architecture.putAll(archMap);
                        }
                        // Add instrument assignments
                        Set<Object> assignedVariables = archParameters.get(archObj);
                        architecture.put(entry.getKey(), assignedVariables);
                        //JSONObject finalConst = creator.addHomogeneousWalker(constJson, archParameters);
                        creator.addHomogeneousWalkerOld(constJson, architecture);
                        i++;
                    }
                        // Add the updated constellation to the spaceSegment array
                    if(!creator.getConstellations().isEmpty()){
                        creator.addGroundNetwork(gn);
                        File architectureJsonFile = creator.toJSON(k);
                        k++;
                        try {
                                HashMap<String, Double> objectivesResults = TradespaceSearchExecutive.evaluateArchitecture(architectureJsonFile, properties);
                                Summary.writeSummaryFile(objectivesResults,architecture,k);
                        } catch (InterruptedException | IOException e) {
                            System.out.println("Error reading the JSON file: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
    
                }       
                   
            }
    
            for (int i = 0; i < architecturesJson.size(); i++) {
                JSONObject arch = architecturesJson.get(i);
                File mainPath = new File(System.getProperty("tatc.output"));
                File archPath = new File(mainPath, "arch-" + i);
                archPath.mkdirs();
                File file = new File(archPath, "arch.json");
    
                // Use FileWriter and write the JSONObject directly
                try (FileWriter fileWriter = new FileWriter(file)) {
                    fileWriter.write(arch.toString(4)); // Pretty print with indentation
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    
    
            System.out.println("Hello");

        } 
    }
    private void fullFactorialCombining(Map<String, List<Object>> combiningVariableValues){
        List<Map<String, Object>> fullArchitectures = generateFullFactorialDesign(combiningVariableValues);
        JSONArray constellationsJSON = tseRequestJson.getJSONObject("designSpace").getJSONArray("spaceSegment");
        HashMap<String, Decision<?>> decisions = properties.getDecisions();
        Decision<GroundNetwork> decisionGroundNetwork = (Decision<GroundNetwork>)decisions.get("groundNetwork");
        Collections.shuffle(fullArchitectures);
        for (GroundNetwork gn : decisionGroundNetwork.getAllowedValues()) {
            int k= 0;
            for (Map<String,Object> archParameters: fullArchitectures){
                ArchitectureCreatorNew creator = new ArchitectureCreatorNew(properties);
                for(int i=0; i<constellationsJSON.length(); i++){
                    JSONObject constJson = constellationsJSON.getJSONObject(i);
                    creator.addHomogeneousWalkerOld(constJson, archParameters);
                }
                if(!creator.getConstellations().isEmpty()){
                    creator.addGroundNetwork(gn);
                    File architectureJsonFile = creator.toJSON(k);
                    k++;
                    try {
                            HashMap<String, Double> objectivesResults = TradespaceSearchExecutive.evaluateArchitecture(architectureJsonFile, properties);
                            Summary.writeSummaryFile(objectivesResults,archParameters,k);
                    } catch (InterruptedException | IOException e) {
                        System.out.println("Error reading the JSON file: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

        }
    }
    


    public List<Map<String, Object>> generateFullFactorialDesign(Map<String, List<Object>> variableValues) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<String> variables = new ArrayList<>(variableValues.keySet());
        generateCombinations(variableValues, variables, 0, new HashMap<>(), result);
        return result;
    }

    private void generateCombinations(Map<String, List<Object>> variableValues, List<String> variables, int index, Map<String, Object> current, List<Map<String, Object>> result) {
        if (index == variables.size()) {
            // Add a copy of the current combination to the result
            result.add(new HashMap<>(current));
            return;
        }
        String variable = variables.get(index);
        List<Object> values = variableValues.get(variable);
        if (values == null || values.isEmpty()) {
            // Skip variables with no values
            generateCombinations(variableValues, variables, index + 1, current, result);
            return;
        }
        for (Object value : values) {
            current.put(variable, value);
            generateCombinations(variableValues, variables, index + 1, current, result);
            current.remove(variable);
        }
    }
    public List<Map<String, Object>> generateFullFactorialDesign(
        Map<String, List<Object>> variableValues,
        Map<String, String> decisionVariables
    ) {
        // Filter out combining pattern variables
        Map<String, List<Object>> combiningVariableValues = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : decisionVariables.entrySet()) {
            String variable = entry.getKey();
            String pattern = entry.getValue();
            if ("Combining".equalsIgnoreCase(pattern)) {
                combiningVariableValues.put(variable, variableValues.get(variable));
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        List<String> variables = new ArrayList<>(combiningVariableValues.keySet());
        generateCombiningCombinations(combiningVariableValues, variables, 0, new HashMap<>(), result);
        return result;
    }
    
    private void generateCombiningCombinations(
        Map<String, List<Object>> variableValues,
        List<String> variables,
        int index,
        Map<String, Object> current,
        List<Map<String, Object>> result
    ) {
        if (index == variables.size()) {
            // Add a copy of the current combination to the result
            result.add(new HashMap<>(current));
            return;
        }
        String variable = variables.get(index);
        List<Object> values = variableValues.get(variable);
        if (values == null || values.isEmpty()) {
            // Skip variables with no values
            generateCombiningCombinations(variableValues, variables, index + 1, current, result);
            return;
        }
        for (Object value : values) {
            current.put(variable, value);
            generateCombiningCombinations(variableValues, variables, index + 1, current, result);
            current.remove(variable);
        }
    }
    
    public List<Map<Object, Set<Object>>> assigning(List<Object> L, List<Object> R) {
        List<Map<Object, Set<Object>>> result = new ArrayList<>();
        generateAssignments(L, R, 0, new HashMap<>(), result);
        return result;
    }

    // Recursive helper method
    private void generateAssignments(
        List<Object> L,
        List<Object> R,
        int index,
        Map<Object, Set<Object>> currentAssignment,
        List<Map<Object, Set<Object>>> result
    ) {
        if (index == L.size()) {
            // Add a copy of the current assignment to the result
            Map<Object, Set<Object>> assignmentCopy = new HashMap<>();
            for (Map.Entry<Object, Set<Object>> entry : currentAssignment.entrySet()) {
                assignmentCopy.put(entry.getKey(), new HashSet<>(entry.getValue()));
            }
            result.add(assignmentCopy);
            return;
        }

        Object element = L.get(index);
        List<Set<Object>> subsets = getAllSubsets(R);

        for (Set<Object> subset : subsets) {
            currentAssignment.put(element, subset);
            generateAssignments(L, R, index + 1, currentAssignment, result);
            currentAssignment.remove(element); // Backtrack
        }
    }

    // Method to generate all subsets (power set) of a given set
    private List<Set<Object>> getAllSubsets(List<Object> set) {
        List<Set<Object>> subsets = new ArrayList<>();
        int n = set.size();
        int totalSubsets = 1 << n; // 2^n subsets

        for (int i = 0; i < totalSubsets; i++) {
            Set<Object> subset = new HashSet<>();
            for (int j = 0; j < n; j++) {
                // Check if jth element of set is included in the ith subset
                if ((i & (1 << j)) != 0) {
                    subset.add(set.get(j));
                }
            }
            subsets.add(subset);
        }
        return subsets;
    }

    



}
