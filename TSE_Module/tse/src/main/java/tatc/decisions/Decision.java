package tatc.decisions;

import tatc.decisions.adg.Graph;
import tatc.tradespaceiterator.ProblemProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.moeaframework.core.Solution;

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
    protected Map<Integer,int[]> encodingMap;
    /**
     * A unique identifier or name for this decision, allowing easy reference.
     */
    protected String decisionName;
    protected List<Decision> parentDecisions;
    protected int[] lastEncoding;
    protected List<Object> result;
    protected String resultType;

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
        this.encodingMap = new HashMap<Integer, int[]>();
        this.parentDecisions = new ArrayList<>();
    }
    @Override
    public Decision clone() {
        try {
            Decision cloned = (Decision) super.clone();
            cloned.parentDecisions = new ArrayList<>(this.parentDecisions);
            cloned.encodingMap = new HashMap<>();
            for (Map.Entry<Integer, int[]> entry : this.encodingMap.entrySet()) {
                cloned.encodingMap.put(entry.getKey(), Arrays.copyOf(entry.getValue(), entry.getValue().length));
            }
            cloned.lastEncoding = (this.lastEncoding != null) ? Arrays.copyOf(this.lastEncoding, this.lastEncoding.length) : null;
            cloned.result = (this.result != null) ? new ArrayList<>(this.result) : null;
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // Should never happen since we're Cloneable
        }
    }


    /**
     * Retrieves the decision name.
     * 
     * @return the decision name string.
     */
    public String getDecisionName() {
        return this.decisionName;
    }
    public int[] getEncodingById(int id){
        return this.encodingMap.get(id);
    }
    public void addEncodingById(int id, int[] encoding){
        this.encodingMap.put(id, encoding);
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
    public abstract List<Map<String,Object>> decodeArchitecture(Object encoded, Solution sol, Graph graph);

    /**
     * Applies mutation operators to the given encoded architecture representation.
     * This method ensures variability and exploration of the design space by flipping bits,
     * swapping elements, or other pattern-specific mutations.
     * 
     * @param encoded The encoded architecture representation to be mutated.
     */
    public abstract void mutate(Object encoded);
    /**
 * Returns the highest key (solution ID) in the encodingMap.
 * Assumes encodingMap uses String keys and is not empty.
 * 
 * @return The highest key in the encodingMap based on lexicographical order.
 * @throws IllegalStateException if encodingMap is empty.
 */
    public Integer getHighestId() {
        if (encodingMap.isEmpty()) {
            throw new IllegalStateException("The encodingMap is empty. No highest ID can be determined.");
        }
        
        // Retrieve the highest key in lexicographical order
        return encodingMap.keySet().stream()
                        .max(Integer::compareTo)
                        .orElseThrow(() -> new IllegalStateException("Failed to find the highest ID."));
    }

    public List<Object> getResult() {
        return result;
    }

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
    public abstract Object extractEncodingFromSolution(Solution solution, int offset);
    public abstract void addParentDecision(Decision decision);
    public abstract int[] getLastEncoding();
    public abstract Object repairWithDependency(Object childEnc, Object parentEnc);
    public List<Decision> getParentDecisions(){
        return this.parentDecisions;
    }
    public String getResultType(){
        return this.resultType;
    }
    public void setResultType(String resultType){
        this.resultType = resultType;
    }
    public abstract void applyEncoding(int[] encoding);

    /**
     * Gets the list of variable names for this decision.
     * For combining decisions, these are the sub-decision names.
     * For assigning decisions, these are the source-target pairs.
     * For other decisions, these are generic names based on the decision type.
     */
    public List<String> getVariableNames() {
        List<String> names = new ArrayList<>();
        if (this instanceof Combining) {
            Combining comb = (Combining) this;
            names.addAll(comb.getSubDecisions());
        } else if (this instanceof tatc.decisions.Assigning) {
            tatc.decisions.Assigning assign = (tatc.decisions.Assigning) this;
            List<String> sources = assign.getSourceEntities();
            List<String> targets = assign.getTargetEntities();
            
            // For assigning decisions, we need n*m variables where n=|sources| and m=|targets|
            int n = sources.size();
            int m = targets.size();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    names.add(sources.get(i) + "-" + targets.get(j));
                }
            }
        } else {
            // Generic names for other decision types
            for (int i = 0; i < getNumberOfVariables(); i++) {
                names.add(decisionName + "_var" + i);
            }
        }
        return names;
    }

    /**
     * Gets the source entities for assigning decisions.
     * Returns empty list for non-assigning decisions.
     */
    public List<String> getSourceEntities() {
        // Base implementation returns empty list
        return new ArrayList<>();
    }

    /**
     * Gets the target entities for assigning decisions.
     * Returns empty list for non-assigning decisions.
     */
    public List<String> getTargetEntities() {
        // Base implementation returns empty list
        return new ArrayList<>();
    }
}
