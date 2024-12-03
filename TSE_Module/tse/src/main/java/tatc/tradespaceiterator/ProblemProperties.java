package tatc.tradespaceiterator;

import tatc.architecture.specifications.CompoundObjective;
import tatc.architecture.specifications.TradespaceSearch;
import tatc.architecture.variable.Decision;

import java.util.ArrayList;
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
        evaluators = parser.getWorkflow(tsrJson);
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
public Map<String, String> getDecisionVariables() {
    JSONObject designSpace = this.tsrJson.getJSONObject("designSpace");
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
        List<Object> values = findValuesForVariable(variable, tsrJson);
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

    /**
     * Returns the instance of the problem
     *
     * @return
     */
    public static ProblemProperties getInstance() {
        return instance;
    }
}
