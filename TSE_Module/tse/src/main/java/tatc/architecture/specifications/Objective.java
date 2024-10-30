package tatc.architecture.specifications;

import java.io.Serializable;

/**
 * An objective for the evaluation, including its name and optimization type.
 */
public class Objective implements Serializable {

    /**
     * The name of the objective
     */
    private final String objectiveName;

    /**
     * The optimization type of the objective (e.g., "MAX" or "MIN")
     */
    private final String objectiveType;

    /**
     * Constructs an Objective object
     *
     * @param objectiveName the name of the objective
     * @param objectiveType the optimization type of the objective
     */
    public Objective(String objectiveName, String objectiveType) {
        this.objectiveName = objectiveName;
        this.objectiveType = objectiveType;
    }

    /**
     * Gets the name of the objective
     *
     * @return the name of the objective
     */
    public String getObjectiveName() {
        return objectiveName;
    }

    /**
     * Gets the optimization type of the objective
     *
     * @return the optimization type of the objective
     */
    public String getObjectiveType() {
        return objectiveType;
    }
}
