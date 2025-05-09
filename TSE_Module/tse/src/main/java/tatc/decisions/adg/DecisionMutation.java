package tatc.decisions.adg;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.Variation;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.RealVariable;
import tatc.decisions.ConstructionNode;
import tatc.decisions.Decision;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
public class DecisionMutation implements Variation {

    private List<Decision> decisions;
    private Random rand = new Random();
    private Map<Decision, Object> childFragments;  // <--- store child fragment for each decision node


    public DecisionMutation(List<Decision> decisions) {
        this.decisions = decisions;
        this.childFragments = new HashMap<>();
    }

    @Override
    public int getArity() {
        return 1; // mutation operator works on one solution
    }

    @Override
    public Solution[] evolve(Solution[] parents) {
        if (parents.length != 1) {
            throw new IllegalArgumentException("This operator requires one parent.");
        }
    
        Solution parent = parents[0];
        // Create a new child solution
        Solution child = parent.copy();
    
        int offset = 0;
        int childId = 0;
        // We assume 'decisions' is in topological order (parents before children)
        for (int nodeIndex = 0; nodeIndex < decisions.size(); nodeIndex++) {
            Decision d = decisions.get(nodeIndex);
            if(d instanceof ConstructionNode){
                continue;
            }
            int maxId = d.getHighestId();
            childId = maxId+1;            
            // 1) Extract the child's encoding
            Object encodedChild = d.getEncodingById(((AdgSolution)child).getId());
            // 2) Mutate child encoding
            d.mutate(encodedChild);
            
            // 4) If this decision depends on a parent, do the repair with the parent's partial encoding
            if (!d.getParentDecisions().isEmpty()) {
                // Suppose d depends on exactly one decision "parentNode"
                // The parent's new encoding is needed for feasibility checks
                Decision parentNode = d.getParentDecisions().get(0);
    
                // We figure out offset / stored child fragment for parentNode from earlier
                // OR we maintain a dictionary: decision -> childEncoded to look up by reference
                Object childParentEncoded = childFragments.get(parentNode);
    
                // Then call a specialized "repairWithDependency"
                encodedChild = d.repairWithDependency(encodedChild, childParentEncoded);
            }
            int vars = ((int[])encodedChild).length;
            // --- 5) Inject child's final fragment into the solution
            injectEncoded(child, offset, encodedChild);
    
            // Store childEncoded so future decisions can reference it 
            childFragments.put(d, encodedChild);
            d.addEncodingById(childId, (int[]) encodedChild);
    
            offset += vars;
        }
        ((AdgSolution)child).setId(childId);
    
        return new Solution[] { child };
    }
    

    private Object extractEncoded(Solution sol, int offset, int requestedLength) {
    // The final returned array must be exactly requestedLength in size
    int[] encoding = new int[requestedLength];

    // But if solution doesn't have enough variables, we can't read them all
    // We clamp the read length to the actual number of variables from offset.
    int maxAvailable = sol.getNumberOfVariables() - offset;
    int safeLength = Math.min(requestedLength, maxAvailable);

    for (int i = 0; i < safeLength; i++) {
        Variable var = sol.getVariable(offset + i);
        // Safely convert var to an int (presumably via RealVariable or direct Casting)
        encoding[i] = EncodingUtils.getInt(var);
    }

    // If the solution was shorter than requestedLength, the tail of encoding remains 0
    // (which might be the "default" meaning).
    return encoding;
}

/**
 * Safely writes the integer-encoded fragment into the solution, making sure
 * we do not step out of bounds. If the encoded array is longer than the
 * available variables, the extra portion is unused. If the solution region
 * is bigger, we set the "unused" variables to 0 or another sentinel.
 */
private void injectEncoded(Solution sol, int offset, Object encoded) {
    int[] arr = (int[]) encoded;

    // If offset is near the end of the solution, we clamp writing to not exceed
    // the solution's variable range.
    int maxAvailable = sol.getNumberOfVariables() - offset;
    int safeLength = Math.min(arr.length, maxAvailable);

    // Write each integer into the solution’s variables as a double
    // (with the "RealVariable" approach).
    for (int i = 0; i < safeLength; i++) {
        RealVariable var = (RealVariable) sol.getVariable(offset + i);

        double newVal = arr[i];
        // If newVal is out of bounds, clamp it (or subtract epsilon so we stay strictly in range)
        if (newVal > var.getUpperBound()) {
            newVal = var.getUpperBound();
        }
        if (newVal < var.getLowerBound()) {
            newVal = var.getLowerBound();
        }
        var.setValue(newVal);
    }

    // If the solution has more variables than arr.length, fill the remainder with 0.0
    // or another sentinel value. This ensures we don't leave stale data behind.
    for (int i = safeLength; i < maxAvailable; i++) {
        RealVariable var = (RealVariable) sol.getVariable(offset + i);
        var.setValue(0.0); // or another “do-nothing” sentinel
    }
}
    

}
