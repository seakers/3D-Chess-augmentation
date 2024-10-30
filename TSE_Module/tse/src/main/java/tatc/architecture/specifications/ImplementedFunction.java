package tatc.architecture.specifications;

import java.io.Serializable;
import java.util.Map;

/**
 * Details of an implemented function within a workflow step, including dependencies and level.
 */
public class ImplementedFunction implements Serializable {

    /**
     * Map of dependencies for the function
     */
    private final Map<String, String> dependencies;

    /**
     * The level of the function
     */
    private final int level;

    /**
     * Constructs an ImplementedFunction object
     *
     * @param dependencies the dependencies for the function
     * @param level        the level of the function
     */
    public ImplementedFunction(Map<String, String> dependencies, int level) {
        this.dependencies = dependencies;
        this.level = level;
    }

    /**
     * Gets the dependencies for the function
     *
     * @return the dependencies for the function
     */
    public Map<String, String> getDependencies() {
        return dependencies;
    }

    /**
     * Gets the level of the function
     *
     * @return the level of the function
     */
    public int getLevel() {
        return level;
    }
}
