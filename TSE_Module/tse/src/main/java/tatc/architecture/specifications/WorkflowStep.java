package tatc.architecture.specifications;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * A workflow step in the evaluation, specifying an evaluator and associated metrics.
 */
public class WorkflowStep implements Serializable {

    /**
     * The name of the evaluator
     */
    private final String evaluator;

    /**
     * Map of metrics associated with the evaluator
     */
    private final Map<String, String> metrics;

    /**
     * Map of implemented functions and their details
     */
    private final Map<String, ImplementedFunction> implementedFunctions;

    /**
     * List of subscriptions for this workflow step
     */
    private final List<String> subscribe;

    /**
     * The target to which metrics are published
     */
    private final String publish_metrics;

    /**
     * Constructs a WorkflowStep object
     *
     * @param evaluator            the name of the evaluator
     * @param metrics              the metrics associated with the evaluator
     * @param implementedFunctions the implemented functions and their details
     * @param subscribe            the list of subscriptions
     * @param publish_metrics      the target to which metrics are published
     */
    public WorkflowStep(String evaluator, Map<String, String> metrics, Map<String, ImplementedFunction> implementedFunctions, List<String> subscribe, String publish_metrics) {
        this.evaluator = evaluator;
        this.metrics = metrics;
        this.implementedFunctions = implementedFunctions;
        this.subscribe = subscribe;
        this.publish_metrics = publish_metrics;
    }

    /**
     * Gets the name of the evaluator
     *
     * @return the name of the evaluator
     */
    public String getEvaluator() {
        return evaluator;
    }

    /**
     * Gets the metrics associated with the evaluator
     *
     * @return the metrics associated with the evaluator
     */
    public Map<String, String> getMetrics() {
        return metrics;
    }

    /**
     * Gets the implemented functions and their details
     *
     * @return the implemented functions and their details
     */
    public Map<String, ImplementedFunction> getImplementedFunctions() {
        return implementedFunctions;
    }

    /**
     * Gets the list of subscriptions
     *
     * @return the list of subscriptions
     */
    public List<String> getSubscribe() {
        return subscribe;
    }

    /**
     * Gets the target to which metrics are published
     *
     * @return the target to which metrics are published
     */
    public String getPublishMetrics() {
        return publish_metrics;
    }
}
