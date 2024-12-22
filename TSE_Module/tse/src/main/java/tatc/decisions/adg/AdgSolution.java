package tatc.decisions.adg;

import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.variable.RealVariable;
import tatc.decisions.Decision;
import tatc.decisions.Combining;
import tatc.decisions.Assigning;
// import tatc.decisions.Partitioning; // just an example, if needed
// import tatc.decisions.DownSelecting; // if needed
// import tatc.decisions.StandardForm; // if needed
import tatc.tradespaceiterator.ProblemProperties;
import java.util.*;

/**
 * AdgSolution represents an architecture solution composed of multiple decision fragments.
 * Each decision contributes a set of variables (encoded as integers but stored in RealVariables).
 */
public class AdgSolution extends Solution {

    private List<Decision> decisions;
    private ProblemProperties properties;
    private int id;
    /**
     * Constructs a new AdgSolution by randomly generating encodings for all decisions from the given graph.
     * @param graph The graph containing decisions in topological order
     * @param properties The problem properties (objectives, etc.)
     * @param totalObjectives the number of objectives for the optimization problem
     */
    public AdgSolution(tatc.decisions.adg.Graph graph, ProblemProperties properties, int totalObjectives, int totalVariables) {

        super(totalVariables, totalObjectives);
        this.properties = properties;
        this.decisions = graph.getTopoOrderedDecisions();
        // Create a random encoding for each decision and store as RealVariables
    }

    /**
     * Copy constructor
     */
    protected AdgSolution(AdgSolution original) {
        super(original);
        this.decisions = original.decisions;
        this.properties = original.properties;
    }
    public void randomizeSolution(){
        int offset = 0;
        for (Decision d : decisions) {
            Object encoded = d.randomEncoding(); 
            int[] arr = (int[]) encoded;
            for (int i = 0; i < arr.length; i++) {
                int maxOption = d.getMaxOptionForVariable(i);
                // We'll assume the decision's indices are [0, maxOption-1]
                double lowerBound = 0.0;
                double upperBound = (double) (maxOption - 1);

                RealVariable var = new RealVariable(lowerBound, upperBound);
                var.setValue(arr[i]); // direct integer assignment
                setVariable(offset + i, var);
            }
            offset += arr.length;
            d.addEncodingById(this.id, arr);
        }
    }
    public int getId(){
        return this.id;
    }
    public void setId(int id){
        this.id = id;
    }

    @Override
    public Solution copy() {
        return new AdgSolution(this);
    }

    /**
     * Decodes the entire solution into a map of architecture parameters by delegating to each decision.
     * 
     * @return a map representing the entire architecture's parameters
     */
    public Map<String,Object> getDesign() {
        // Start with an initial architecture set: a single empty architecture
        List<Map<String, Object>> archSet = new ArrayList<>();
        archSet.add(new HashMap<>());
    
        int offset = 0;
    
        for (Decision d : decisions) {
            // Extract encoded representation for this decision from the solution
            Object encoded = d.extractEncodingFromSolution(this, offset);
            offset += d.getNumberOfVariables();
    
            // Decode using the decision, which transforms or expands the architecture set
            archSet = d.decodeArchitecture(encoded, this);
        }
    
        if (archSet.isEmpty()) {
            throw new IllegalStateException("No architectures produced after decoding all decisions.");
        }
    
        // If multiple architectures are generated, return the first one
        // Adjust if you want different behavior when multiple architectures are produced
        return archSet.get(0);
    }
    @Override
    public int getNumberOfVariables() {
        int nVar = 0;
    
        // Loop through variables using their indices if direct access is restricted
        for (int i = 0; i < super.getNumberOfVariables(); i++) {
            Variable var = this.getVariable(i);
    
            // Example condition: Include variables that are RealVariables or meet other criteria
            if (var instanceof RealVariable) {
                nVar++;
            }
        }
    
        return nVar;
    }
    
    
    /**
     * Utility method to decode a single decision from the solution.
     * If you need to inspect just one decision's portion, this method can help.
     * 
     * @param decisionName the name of the decision
     * @return a map of architecture parameters for that decision
     */
    public Map<String,Object> getDesignDecision(String decisionName) {
        int offset = 0;
        Decision target = null;
    
        // Find the target decision and compute the offset in the solution variables
        for (Decision d : decisions) {
            if (d.getDecisionName().equals(decisionName)) {
                target = d;
                break;
            } else {
                offset += d.getNumberOfVariables();
            }
        }
    
        if (target == null) {
            throw new IllegalArgumentException("Decision " + decisionName + " not found in solution.");
        }
    
        // Extract the encoded representation for this decision
        Object encoded = target.extractEncodingFromSolution(this, offset);
    
        // Initially, we have no fully defined architectures, so start with a single empty architecture
        List<Map<String, Object>> initialArchSet = new ArrayList<>();
        initialArchSet.add(new HashMap<>());
    
        // Decode architecture using the updated method
        List<Map<String, Object>> resultArchSet = target.decodeArchitecture(encoded, this);
    
        // Check the result
        if (resultArchSet.isEmpty()) {
            throw new IllegalArgumentException("Decoded architecture list is empty after decoding decision " + decisionName);
        }
    
        // If multiple architectures are produced, we just return the first one
        // (Adjust this if you want a different behavior)
        return resultArchSet.get(0);
    }
    
    

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof AdgSolution)) {
            return false;
        }
        AdgSolution other = (AdgSolution) obj;
        // Check variable values
        if (this.getNumberOfVariables() != other.getNumberOfVariables()) {
            return false;
        }
        for (int i=0; i<this.getNumberOfVariables(); i++) {
            double v1 = ((RealVariable)this.getVariable(i)).getValue();
            double v2 = ((RealVariable)other.getVariable(i)).getValue();
            if (Math.round(v1) != Math.round(v2)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        // Create a hash based on the encoded variables
        int hash = 7;
        for (int i=0; i<this.getNumberOfVariables(); i++) {
            double val = ((RealVariable)this.getVariable(i)).getValue();
            hash = 31 * hash + (int)Math.round(val);
        }
        return hash;
    }

    @Override
    public String toString() {
        // Print the encoded representation
        StringBuilder sb = new StringBuilder();
        sb.append("AdgSolution: ");
        for (int i=0; i<this.getNumberOfVariables(); i++) {
            double val = ((RealVariable)this.getVariable(i)).getValue();
            sb.append((int)Math.round(val)).append(" ");
        }
        return sb.toString().trim();
    }

}
