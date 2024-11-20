package tatc.tradespaceiterator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import tatc.architecture.ArchitectureCreator;
import tatc.architecture.constellations.HomogeneousWalkerParametersNew;
import tatc.architecture.specifications.Constellation;
import tatc.architecture.specifications.Satellite;
import tatc.architecture.ArchitectureCreatorNew;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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
    List<String> decisionVariables = this.getDecisionVariables();
    System.out.println("Decision Variables: " + decisionVariables);

    // Get possible values for each decision variable
    Map<String, List<Object>> variableValues = this.getDecisionVariableValues(decisionVariables);
    System.out.println("Variable Values:");
    for (String var : variableValues.keySet()) {
        System.out.println(var + ": " + variableValues.get(var));
    }
    ArchitectureCreatorNew ac = ArchitectureCreatorNew();

    // Generate full factorial design
    List<Map<String, Object>> fullFactorialDesign = this.generateFullFactorialDesign(variableValues);
    System.out.println("Number of architectures: " + fullFactorialDesign.size());

    for (Map<String, Object> design : fullFactorialDesign) {
        // Add additional required parameters to design map
        design.put("epoch", properties.getTradespaceSearch().getMission().getStart());
        design.put("eccentricity", properties.getTradespaceSearch().getDesignSpace().getSpaceSegment().get(0).getOrbit().get(0).getEccentricity());
        design.put("secondaryPayload", false); // Adjust as needed

        // Include the satellite and payload structure
        Satellite baseSatellite = properties.getTradespaceSearch().getDesignSpace().getSpaceSegment().get(0).getSatellites().get(0);
        design.put("satellite", baseSatellite);

        // Include the payload and instruments
        Map<String, Object> payloadParams = new HashMap<>();
        List<Map<String, Object>> instrumentsList = new ArrayList<>();

        // Collect instrument parameters from design
        Map<String, Object> instrumentParams = new HashMap<>();
        // Assume that design contains instrument parameters
        for (String key : design.keySet()) {
            if (isInstrumentParameter(key)) {
                instrumentParams.put(key, design.get(key));
            }
        }
        instrumentsList.add(instrumentParams);
        payloadParams.put("instruments", instrumentsList);
        design.put("payload", payloadParams);
        // Now call the addHomogeneousWalker method with the design map
        try {
            addHomogeneousWalkerNew(design);
        } catch (IllegalArgumentException e) {
            System.err.println("Error creating constellation: " + e.getMessage());
            // Handle the error as needed, e.g., skip this design
        }
    }

    // Proceed to evaluate the constellations
    // For example:
    for (Constellation constellation : constellations) {
        // Build the architecture using constellation
        ArchitectureCreator architecture = new ArchitectureCreator();
        architecture.addConstellation(constellation);

        // Evaluate the architecture
        // Collect results
        // (Implementation depends on your existing architecture and evaluation methods)
    }
}

// Helper method to determine if a parameter is related to the instrument
private boolean isInstrumentParameter(String key) {
    // Define a list or set of instrument parameter keys
    Set<String> instrumentKeys = new HashSet<>(Arrays.asList(
        "focalLength",
        "apertureDia",
        "numberOfDetectorsRowsAlongTrack",
        "bitsPerPixel"
        // Add other instrument parameter keys as needed
    ));
    return instrumentKeys.contains(key);
}


    public List<String> getDecisionVariables() {
        JSONObject designSpace = tseRequestJson.getJSONObject("designSpace");
        JSONArray decisionVariablesArray = designSpace.getJSONArray("decisionVariables");
        List<String> decisionVariables = new ArrayList<>();
        for (int i = 0; i < decisionVariablesArray.length(); i++) {
            decisionVariables.add(decisionVariablesArray.getString(i));
        }
        return decisionVariables;
    }

    public Map<String, List<Object>> getDecisionVariableValues(List<String> decisionVariables) {
        Map<String, List<Object>> variableValues = new HashMap<>();
        for (String variable : decisionVariables) {
            List<Object> values = findValuesForVariable(variable, tseRequestJson);
            if (values != null && !values.isEmpty()) {
                // Remove duplicates
                Set<Object> uniqueValues = new HashSet<>(values);
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
}
