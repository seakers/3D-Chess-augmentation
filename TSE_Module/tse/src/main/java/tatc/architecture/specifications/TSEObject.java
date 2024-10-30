package tatc.architecture.specifications;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * TSE configuration for the evaluation, including objectives and tool levels.
 */
public class TSEObject implements Serializable {

    /**
     * List of objectives for the evaluation
     */
    private final List<MissionObjective> objectives;

    /**
     * Subscribe field, indicating the subscription target
     */
    private final String subscribe;

    /**
     * Map of publish metric requests
     */
    private final Map<String, String> publish_metric_requests;

    /**
     * Map of tool levels
     */
    private final Map<String, Integer> tool_levels;

    /**
     * Constructs a TSE object
     *
     * @param objectives              the list of objectives
     * @param subscribe               the subscription target
     * @param publish_metric_requests the publish metric requests
     * @param tool_levels             the tool levels
     */
    public TSEObject(List<MissionObjective> objectives, String subscribe, Map<String, String> publish_metric_requests, Map<String, Integer> tool_levels) {
        this.objectives = objectives;
        this.subscribe = subscribe;
        this.publish_metric_requests = publish_metric_requests;
        this.tool_levels = tool_levels;
    }

    /**
     * Gets the list of objectives
     *
     * @return the list of objectives
     */
    public List<MissionObjective> getObjectives() {
        return objectives;
    }

    /**
     * Gets the subscription target
     *
     * @return the subscription target
     */
    public String getSubscribe() {
        return subscribe;
    }

    /**
     * Gets the publish metric requests
     *
     * @return the publish metric requests
     */
    public Map<String, String> getPublishMetricRequests() {
        return publish_metric_requests;
    }

    /**
     * Gets the tool levels
     *
     * @return the tool levels
     */
    public Map<String, Integer> getToolLevels() {
        return tool_levels;
    }
}
