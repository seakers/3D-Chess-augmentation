package tatc.tradespaceiterator;

import tatc.architecture.specifications.CompoundObjective;
import tatc.architecture.specifications.TradespaceSearch;
import tatc.architecture.variable.Decision;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    /**
     * Constructs the problem properties
     * @param tsr the tradespace search request
     */
    public ProblemProperties(TradespaceSearch tsr, JSONObject tsrJson) {
        this.tradespaceSearch = tsr;
        decisions = this.tradespaceSearch.TradespaceSearch2Decisions();
        objectives = this.tradespaceSearch.processObjectives();
        instance = this;
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

    /**
     * Returns the instance of the problem
     *
     * @return
     */
    public static ProblemProperties getInstance() {
        return instance;
    }
}
