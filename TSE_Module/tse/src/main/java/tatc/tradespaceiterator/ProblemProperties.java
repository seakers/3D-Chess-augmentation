package tatc.tradespaceiterator;

import tatc.architecture.specifications.CompoundObjective;
import tatc.architecture.specifications.TradespaceSearch;
import tatc.architecture.variable.Decision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import tatc.tradespaceiterator.TSERequestParser;
/**
 * Class that contains the properties of the constellation design problem.
 */
public class ProblemProperties {
    /**
     * The tradespace search request
     */
    private final TradespaceSearch tradespaceSearch;
    /**
     * Map of decision keys and decision objects (which contain the allowed values for a specific decision)
     */
    private final HashMap<String,Decision<?>> decisions;
    /**
     * List of objectives of the search (empty list for FF)
     */
    private final List<CompoundObjective> objectives;

    private static ProblemProperties instance;
    private Map<String, List<String>> costEvaluators;
    private Map<String, List<String>> scienceEvaluators;
    private Map<String, JSONObject> evaluators;
    Map<String, String> metricTopics;
    private JSONObject tsrJson;

    /**
     * Constructs the problem properties
     * @param tsr the tradespace search request
     */
    public ProblemProperties(TradespaceSearch tsr, JSONObject tsrJson) {
        this.tradespaceSearch = tsr;
        decisions = this.tradespaceSearch.TradespaceSearch2Decisions();
        objectives = this.tradespaceSearch.processObjectives();
        instance = this;
        this.tsrJson = tsrJson;
        TSERequestParser parser = new TSERequestParser();
        evaluators = parser.getWorkflowFromTse(tsrJson);
        metricTopics = parser.getMetricRequestsTopics(tsrJson);
    }

    /**
     * Gets the tradespace search request
     * @return the tradespace search request
     */
    public Map<String, List<String>> getCostEvaluators() {
        return costEvaluators;
    }
    public Map<String, List<String>> getScienceEvaluators() {
        return scienceEvaluators;
    }
    public Map<String, JSONObject> getEvaluators() {
        return evaluators;
    }
    public Map<String, String> getMetricTopics() {
        return metricTopics;
    }
    public JSONObject getTsrObject(){
        return tsrJson;
    }
    public TradespaceSearch getTradespaceSearch() {
        return tradespaceSearch;
    }

    /**
     * Gets the map of decision keys and decision objects
     * @return the map of decision keys and decision objects
     */
    public HashMap<String, Decision<?>> getDecisions() {
        return decisions;
    }

    /**
     * Gets the objectives of the search
     * @return the objectives of the search
     */
    public List<CompoundObjective> getObjectives() {
        return objectives;
    }
// public Map<String, String> getDecisionVariables() {
//     JSONObject designSpace = this.tsrJson.getJSONObject("designSpace");
//     JSONObject decisionVariablesObject = designSpace.getJSONObject("decisionVariables");
//     Map<String, String> decisionVariables = new LinkedHashMap<>();
//     for (String key : decisionVariablesObject.keySet()) {
//         String decisionType = decisionVariablesObject.getString(key);
//         decisionVariables.put(key, decisionType);
//     }
//     return decisionVariables;
// }


// public Map<String, List<Object>> getDecisionVariableValues(Map<String, String> decisionVariables) {
//     Map<String, List<Object>> variableValues = new LinkedHashMap<>();
//     for (String variable : decisionVariables.keySet()) {
//         List<Object> values = findValuesForVariable(variable, tsrJson);
//         if (values != null && !values.isEmpty()) {
//             // Remove duplicates while preserving order
//             Set<Object> uniqueValues = new LinkedHashSet<>(values);
//             variableValues.put(variable, new ArrayList<>(uniqueValues));
//         } else {
//             System.err.println("Warning: No values found for variable " + variable);
//         }
//     }
//     return variableValues;
// }
public List<Object> getDistinctValuesForVariable(String variable) {
    List<Object> vals = findValuesForVariable(variable, tsrJson);
    if (vals == null || vals.isEmpty()) {
        return new ArrayList<>();
    } else {
        Set<Object> unique = new LinkedHashSet<>(vals);
        return new ArrayList<>(unique);
    }
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

public Map<String, String> getDecisionVariables() {
    // Extract "designSpace" and "decisionVariables" objects from the JSON
    JSONObject designSpace = this.tsrJson.getJSONObject("designSpace");
    JSONObject decisionVariablesObject = designSpace.getJSONObject("decisionVariables");
    
    // This map will store the decision variable names and their types (e.g., "Combining", "Assigning")
    Map<String, String> decisionVariables = new LinkedHashMap<>();
    
    // Loop through each variable defined in the "decisionVariables" object
    for (String variableName : decisionVariablesObject.keySet()) {
        // Extract the JSON object that defines the details for this decision variable
        JSONObject variableDefinition = decisionVariablesObject.getJSONObject(variableName);
        
        // Extract the "type" field to identify the type of decision (e.g., "Combining", "Assigning")
        String decisionType = variableDefinition.getString("type");
        
        // Check if the variable is of type "Combining"
        if ("Combining".equalsIgnoreCase(decisionType)) {
            // Check if it has "combiningDecisions", and handle sub-decisions accordingly
            if (variableDefinition.has("combiningDecisions")) {
                JSONArray combiningDecisionsArray = variableDefinition.getJSONArray("combiningDecisions");
                List<String> subDecisions = new ArrayList<>();
                for (int i = 0; i < combiningDecisionsArray.length(); i++) {
                    subDecisions.add(combiningDecisionsArray.getString(i));
                }
                // Optionally, you might want to log or store sub-decisions
                System.out.println("Combining decision '" + variableName + "' has sub-decisions: " + subDecisions);
            } else {
                System.err.println("Warning: 'Combining' decision '" + variableName + "' is missing 'combiningDecisions' array.");
            }
        }
        
        // Add the variable name and type to the map
        decisionVariables.put(variableName, decisionType);
    }
    
    return decisionVariables;
}


public Map<String, List<Object>> getDecisionVariableValues(Map<String, String> decisionVariables) {
    Map<String, List<Object>> variableValues = new LinkedHashMap<>();

    JSONObject designSpace = this.tsrJson.getJSONObject("designSpace");
    JSONObject decisionVariablesObject = designSpace.getJSONObject("decisionVariables");

    for (String variable : decisionVariables.keySet()) {
        String type = decisionVariables.get(variable);
        JSONObject varObj = decisionVariablesObject.getJSONObject(variable);

        if (type.equalsIgnoreCase("Combining")) {
            // For a Combining decision, we have a "combiningDecisions" array
            JSONArray subDecisions = varObj.getJSONArray("combiningDecisions");

            // Collect values for each sub-decision variable
            List<List<Object>> subVariablesValues = new ArrayList<>();
            for (int i = 0; i < subDecisions.length(); i++) {
                String subVar = subDecisions.getString(i);
                List<Object> subVarValues = findValuesForVariable(subVar, tsrJson);

                if (subVarValues == null || subVarValues.isEmpty()) {
                    System.err.println("Warning: No values found for sub-variable " + subVar + " in combining decision " + variable);
                    subVarValues = Collections.emptyList();
                } else {
                    // Remove duplicates
                    Set<Object> uniqueValues = new LinkedHashSet<>(subVarValues);
                    subVarValues = new ArrayList<>(uniqueValues);
                }
                subVariablesValues.add(subVarValues);
            }

            // Compute the Cartesian product of all subVariablesValues to form a combined set of configurations
            List<List<Object>> cartesianProduct = cartesianProduct(subVariablesValues);

            // Convert cartesian product (List<List<Object>>) to List<Object> so it matches return type
            // Each element in final list is a tuple (List<Object>) representing one combination
            List<Object> combinedList = new ArrayList<>(cartesianProduct);
            variableValues.put(variable, combinedList);

        } else if (type.equalsIgnoreCase("Assigning")) {
            // For Assigning (and other patterns), we may need to consider parents.
            // In this simplified version, we assume we just find values for this variable as before.
            // If the pattern depends on parent's results, you'd fetch parent's variables first.
            // For now, let's just call findValuesForVariable directly on the variable name.
            
            // NOTE: The actual logic might differ based on your pattern definition.
            // If "Assigning" needs parent's architectures or combined sets, implement that logic.
            
            // Just find direct values (if any). If no direct values are defined (like for "payload"),
            // you might either skip or handle parent's results. Here we just do a direct attempt:
            List<Object> values = findValuesForVariable(variable, tsrJson);
            if (values == null || values.isEmpty()) {
                System.err.println("Warning: No values found for variable " + variable + " of type Assigning");
                values = Collections.emptyList();
            } else {
                Set<Object> uniqueValues = new LinkedHashSet<>(values);
                values = new ArrayList<>(uniqueValues);
            }
            variableValues.put(variable, values);

        } else {
            // For other patterns (like down-selecting, partitioning, permuting),
            // the logic would be similar. Find the relevant variables or elements
            // from tsrJson and form their values. For simplicity, do the same as original:
            List<Object> values = findValuesForVariable(variable, tsrJson);
            if (values != null && !values.isEmpty()) {
                // Remove duplicates while preserving order
                Set<Object> uniqueValues = new LinkedHashSet<>(values);
                variableValues.put(variable, new ArrayList<>(uniqueValues));
            } else {
                System.err.println("Warning: No values found for variable " + variable);
            }
        }
    }
    return variableValues;
}

/**
 * Helper method to compute the Cartesian product of a list of lists.
 * Each inner list is one dimension of the product.
 * For example, if subVariablesValues = [[A,B],[1,2]] then
 * cartesianProduct = [[A,1],[A,2],[B,1],[B,2]].
 */
private List<List<Object>> cartesianProduct(List<List<Object>> lists) {
    List<List<Object>> result = new ArrayList<>();
    if (lists.isEmpty()) {
        // No dimensions
        result.add(new ArrayList<>());
        return result;
    }
    // Start with the first list
    result.addAll(wrapEachAsList(lists.get(0)));

    // Iteratively combine with the next lists
    for (int i = 1; i < lists.size(); i++) {
        List<List<Object>> newResult = new ArrayList<>();
        for (List<Object> prefix : result) {
            for (Object elem : lists.get(i)) {
                List<Object> combo = new ArrayList<>(prefix);
                combo.add(elem);
                newResult.add(combo);
            }
        }
        result = newResult;
    }
    return result;
}

private List<List<Object>> wrapEachAsList(List<Object> singleDimension) {
    List<List<Object>> wrapped = new ArrayList<>();
    for (Object o : singleDimension) {
        List<Object> l = new ArrayList<>();
        l.add(o);
        wrapped.add(l);
    }
    return wrapped;
}


    /**
     * Returns the instance of the problem
     *
     * @return
     */
    public static ProblemProperties getInstance() {
        return instance;
    }
}
