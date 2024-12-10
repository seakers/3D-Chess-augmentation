package tatc.decisions;

import tatc.tradespaceiterator.ProblemProperties;
import java.util.Map;

/**
 * The Decision class represents a generic decision node that encodes how certain 
 * parts of the architecture design space are enumerated and manipulated. 
 * 
 * In this framework, a "Decision" abstracts the encoding and operations 
 * (such as crossover, mutation, enumeration) for a particular portion of 
 * the design space defined by certain variables or parameters.
 * 
 * Concrete subclasses will implement different decision patterns, e.g., 
 * Combining, Assigning, Partitioning, etc. Each pattern manages how 
 * decision variables are represented (encoded) and how operators are applied.
 */
public abstract class Decision {

    /**
     * Reference to the problem's properties. This contains information 
     * parsed from the TSERequest, including the design space definition, 
     * objectives, and potentially other parameters needed to build and 
     * evaluate architectures.
     */
    protected ProblemProperties properties;

    /**
     * A unique identifier or name for this decision, allowing easy reference.
     */
    protected String decisionName;

    /**
     * Constructs a Decision object given the problem properties and a decision name.
     * 
     * @param properties    The problem properties object containing the design space definition 
     *                      and other relevant information from the TSERequest.
     * @param decisionName  A user-defined name or identifier for this decision node.
     */
    public Decision(ProblemProperties properties, String decisionName) {
        this.properties = properties;
        this.decisionName = decisionName;
    }

    /**
     * Retrieves the decision name.
     * 
     * @return the decision name string.
     */
    public String getDecisionName() {
        return this.decisionName;
    }

    /**
     * In general, decision variables will be extracted from the ProblemProperties object.
     * Concrete subclasses will parse the relevant parts of the design space and store 
     * them in their own internal structures (e.g., lists, arrays, etc.).
     */
    public abstract void initializeDecisionVariables();

    /**
     * Generates the full-factorial enumeration or initial random population of architectures 
     * defined by the decision variables. This may return a list of encodings that represent 
     * different architectures for exploration.
     * 
     * @return A list or set of encoded architectures (e.g., a list of Maps representing assignments).
     */
    public abstract Iterable<Map<String, Object>> enumerateArchitectures();

    /**
     * Encodes a given architecture (represented as a map or structure) into the decision's 
     * internal representation (e.g., a chromosome or integer array). This encoding will 
     * depend on the particular decision pattern.
     * 
     * @param architecture A map or object representing the selected design variables of this architecture.
     * @return An encoded representation suitable for evolutionary operations.
     */
    public abstract Object encodeArchitecture(Map<String, Object> architecture);

    /**
     * Decodes an encoded representation (e.g., a chromosome) back into a 
     * user-friendly architecture parameter map. This allows evaluation 
     * against problem objectives.
     * 
     * @param encoded The encoded representation of the architecture.
     * @return A map of architecture parameters corresponding to the decision's encoding.
     */
    public abstract Map<String, Object> decodeArchitecture(Object encoded);

    /**
     * Applies mutation operators to the given encoded architecture representation.
     * This method ensures variability and exploration of the design space by flipping bits,
     * swapping elements, or other pattern-specific mutations.
     * 
     * @param encoded The encoded architecture representation to be mutated.
     */
    public abstract void mutate(Object encoded);

    /**
     * Applies crossover operators to generate a new offspring architecture from two parent 
     * encodings. The crossover logic will vary depending on the pattern and the nature 
     * of the encoded solution.
     * 
     * @param parent1 The first parent encoding.
     * @param parent2 The second parent encoding.
     * @return The new offspring's encoded representation.
     */
    public abstract Object crossover(Object parent1, Object parent2);

    public abstract int getNumberOfVariables();

    public abstract Object randomEncoding();

    public abstract int getMaxOptionForVariable(int i);
    
}
