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
        Map<String, String> decisionVariables = this.getDecisionVariables();
        Map<String, List<Object>> variableValues = this.getDecisionVariableValues(decisionVariables);
        List<Map<String, Object>> fullArchitectures = new ArrayList<>();
        List<Map<String, Object>> combiningArchitectures;
        // Filter combining pattern variables
        Map<String, List<Object>> combiningVariableValues = new LinkedHashMap<>();
        Map<String, List<Object>> assigningVariableValues = new LinkedHashMap<>();
        List<Constellation> constellations = properties.getTradespaceSearch().getDesignSpace().getSpaceSegment();
        JSONArray constellationsJSON = tseRequestJson.getJSONObject("designSpace").getJSONArray("spaceSegment");
        HashMap<String, Decision<?>> decisions = properties.getDecisions();        
        Decision<GroundNetwork> decisionGroundNetwork = (Decision<GroundNetwork>)decisions.get("groundNetwork");
        for (Map.Entry<String, String> entry : decisionVariables.entrySet()) {
            if ("Combining".equalsIgnoreCase(entry.getValue())) {
                combiningVariableValues.put(entry.getKey(), variableValues.get(entry.getKey()));
            }
            else if ("Assigning".equalsIgnoreCase(entry.getValue())){
                assigningVariableValues.put(entry.getKey(),variableValues.get(entry.getKey()));
            }
        }
        if(!assigningVariableValues.isEmpty() && !combiningVariableValues.isEmpty()){
            combiningArchitectures = generateFullFactorialDesign(combiningVariableValues);
            for (Map.Entry<String, List<Object>> entry : assigningVariableValues.entrySet()){
                List<Object> architectures = new ArrayList<>(combiningArchitectures);
                List<Map<Object, Set<Object>>> allAssignments = assigning(architectures, entry.getValue());
                // Step 4: Combine architectures with assignments
                for (Map<Object, Set<Object>> assignment : allAssignments) {
                    Map<String, Object> architecture = new HashMap<>();
                    // Extract architecture parameters from assignment keys
                    for (Object archObj : assignment.keySet()) {
                        if (archObj instanceof Map) {
                            Map<String, Object> archMap = (Map<String, Object>) archObj;
                            architecture.putAll(archMap);
                        }
                        // Add instrument assignments
                        Set<Object> assignedVariables = assignment.get(archObj);
                        architecture.put(entry.getKey(), assignedVariables);
                    }
                    fullArchitectures.add(architecture);
                }
                System.out.println("Total number of architectures: " + fullArchitectures.size());
            }
        }else if (assigningVariableValues.isEmpty() && !combiningVariableValues.isEmpty()){
            fullArchitectures = generateFullFactorialDesign(combiningVariableValues);
        }
        Set<JSONObject> constellationsJson = new HashSet<JSONObject>();
        ArchitectureCreatorNew creator = new ArchitectureCreatorNew();
        // for (Map<String,Object> archParameters: fullArchitectures){
        //     int i = 0;
        //     for(Constellation constellation : constellations){
        //         if(constellation.getConstellationType().equals("DELTA_HOMOGENEOUS")){
        //            JSONObject constJson = constellationsJSON.getJSONObject(i);
        //            JSONObject finalConst = creator.addHomogeneousWalker(constJson, archParameters);
        //            constellationsJson.add(finalConst);
        //             i++;
        //         }                

        //     }
        // }
        List<JSONObject> architecturesJson = new ArrayList<>();
        // Loop over each set of architecture parameters
        int k = 0;
        for (GroundNetwork gn : decisionGroundNetwork.getAllowedValues()) {
            for (Map<String, Object> archParameters : fullArchitectures) {
                // Create a new architecture JSON object
                JSONObject architectureJson = new JSONObject();

                // Add necessary fields to match the structure of arch.json
                architectureJson.put("@type", "Architecture");
                architectureJson.put("@id", "arch-" + architecturesJson.size());

                // Initialize the spaceSegment JSONArray
                JSONArray spaceSegmentArray = new JSONArray();

                // Loop over the constellations
                for (int i = 0; i < constellations.size(); i++) {
                    Constellation constellation = constellations.get(i);

                    // Get the corresponding constellation JSON from constellationsJSON
                    JSONObject constJson = constellationsJSON.getJSONObject(i);

                    // Update the constellation JSON with the architecture parameters
                    //JSONObject finalConst = creator.addHomogeneousWalker(constJson, archParameters);
                    creator.addHomogeneousWalkerOld(constJson, archParameters);

                    // Add the updated constellation to the spaceSegment array
                }
                if(!creator.getConstellations().isEmpty()){
                    creator.addGroundNetwork(gn);
                    File architectureJsonFile = creator.toJSON(k);
                    k++;
                    try {
                            HashMap<String, Double> objectivesResults = TradespaceSearchExecutive.evaluateArchitecture(architectureJsonFile, properties);
                            writeSummaryFile(objectivesResults, archParameters, k);
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
    public void writeSummaryFile(Map<String, Double> objectives, Map<String, Object> archVariables, int archIndex) throws IOException {
        //String csvFile = "summary.csv";
        String csvFile = System.getProperty("tatc.output") + File.separator + "summary.csv";
        File file = new File(csvFile);
        boolean fileExists = file.exists();
    
        // Collect headers from archVariables and objectives
        Set<String> variableNames = new LinkedHashSet<>(archVariables.keySet());
        Set<String> objectiveNames = new LinkedHashSet<>(objectives.keySet());
    
        // Prepare to write to CSV
        try (FileWriter csvWriter = new FileWriter(file, true)) { // 'true' enables appending
            // If the file is new, write the header
            if (!fileExists) {
                List<String> header = new ArrayList<>();
                header.add("archIndex"); // Include archIndex in header
                header.addAll(variableNames);
                header.addAll(objectiveNames);
                csvWriter.append(String.join(",", header));
                csvWriter.append("\n");
            }
    
            // Prepare row values
            List<String> rowValues = new ArrayList<>();
            rowValues.add(Integer.toString(archIndex)); // Add archIndex to row
    
            // Add decision variable values
            for (String varName : variableNames) {
                Object value = archVariables.get(varName);
                String valueStr = (value != null) ? value.toString() : "";
                // Escape quotes and handle special characters
                valueStr = valueStr.replace("\"", "\"\"");
                if (valueStr.contains(",") || valueStr.contains("\"") || valueStr.contains("\n")) {
                    valueStr = "\"" + valueStr + "\"";
                }
                rowValues.add(valueStr);
            }
    
            // Add objective values
            for (String objName : objectiveNames) {
                Double value = objectives.get(objName);
                String valueStr = (value != null) ? value.toString() : "";
                // Escape quotes and handle special characters
                valueStr = valueStr.replace("\"", "\"\"");
                if (valueStr.contains(",") || valueStr.contains("\"") || valueStr.contains("\n")) {
                    valueStr = "\"" + valueStr + "\"";
                }
                rowValues.add(valueStr);
            }
    
            // Write the row to the CSV file
            csvWriter.append(String.join(",", rowValues));
            csvWriter.append("\n");
        }
    }

public Map<String, String> getDecisionVariables() {
    JSONObject designSpace = tseRequestJson.getJSONObject("designSpace");
    JSONObject decisionVariablesObject = designSpace.getJSONObject("decisionVariables");
    Map<String, String> decisionVariables = new LinkedHashMap<>();
    for (String key : decisionVariablesObject.keySet()) {
        String decisionType = decisionVariablesObject.getString(key);
        decisionVariables.put(key, decisionType);
    }
    return decisionVariables;
}


public Map<String, List<Object>> getDecisionVariableValues(Map<String, String> decisionVariables) {
    Map<String, List<Object>> variableValues = new LinkedHashMap<>();
    for (String variable : decisionVariables.keySet()) {
        List<Object> values = findValuesForVariable(variable, tseRequestJson);
        if (values != null && !values.isEmpty()) {
            // Remove duplicates while preserving order
            Set<Object> uniqueValues = new LinkedHashSet<>(values);
            variableValues.put(variable, new ArrayList<>(uniqueValues));
        } else {
            System.err.println("Warning: No values found for variable " + variable);
        }
    }
    return variableValues;
}

private List<Object> findValuesForVariable(String variable, Object element) {
    List<Object> values = new ArrayList<>();
    if (element instanceof JSONObject) {
        JSONObject obj = (JSONObject) element;
        for (String key : obj.keySet()) {
            if (!key.equals("decisionVariables")){
                Object value = obj.get(key);
                if (key.equals(variable)) {
                    if (value instanceof JSONArray) {
                        JSONArray array = (JSONArray) value;
                        for (int i = 0; i < array.length(); i++) {
                            values.add(array.get(i));
                        }
                    } else {
                        values.add(value);
                    }
                } else {
                    // Recursively search in the value
                    values.addAll(findValuesForVariable(variable, value));
                }
            }
        }
    } else if (element instanceof JSONArray) {
        JSONArray array = (JSONArray) element;
        for (int i = 0; i < array.length(); i++) {
            values.addAll(findValuesForVariable(variable, array.get(i)));
        }
    }
    return values;
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
