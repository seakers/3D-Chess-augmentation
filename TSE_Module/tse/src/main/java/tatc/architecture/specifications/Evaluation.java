package tatc.architecture.specifications;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Specification of the evaluation process for the TSERequest, including objectives and workflow steps.
 */
public class Evaluation implements Serializable {

    /**
     * Type tag for JSON serialization
     */
    @SerializedName("@type")
    private final String _type = "Evaluation";

    /**
     * TSE configuration containing objectives and other settings
     */
    private final TSEObject TSE;

    /**
     * List of workflow steps defining the evaluation process
     */
    private final List<WorkflowStep> workflow;

    /**
     * Constructs an Evaluation object
     *
     * @param TSE      the TSE configuration
     * @param workflow the list of workflow steps
     */
    public Evaluation(TSEObject TSE, List<WorkflowStep> workflow) {
        this.TSE = TSE;
        this.workflow = workflow;
    }

    /**
     * Gets the TSE configuration
     *
     * @return the TSE configuration
     */
    public TSEObject getTSE() {
        return TSE;
    }

    /**
     * Gets the list of workflow steps
     *
     * @return the list of workflow steps
     */
    public List<WorkflowStep> getWorkflow() {
        return workflow;
    }
}
