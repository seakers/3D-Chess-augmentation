package tatc.architecture.variable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class that contains information about a decision in the tradespace
 */
public class Decision<T> {
    /**
     * The type of decision such as Integer, Binary, etc.
     */
    private final String type;
    /**
     * List of possible values this decision can take
     */
    private final List<T> allowedValues;

    /**
     * Constructs a decision object
     * @param type the type of decision
     * @param allowedValues the list of possible values this decision can take
     */
    public Decision(String type, List<T> allowedValues) {
        this.type = type;
        this.allowedValues = allowedValues;
    }

    /**
     * Gets a random possible value
     * @return a random possible value
     */
    public T getRandomValue(){
        int random=ThreadLocalRandom.current().nextInt(0, this.allowedValues.size());
        return this.allowedValues.get(random);
    }

    /**
     * Gets the type of decision
     * @return the type of decision
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the list of possible values for this decision
     * @return the list of possible values for this decision
     */
    public List<T> getAllowedValues() {
        return allowedValues;
    }
}