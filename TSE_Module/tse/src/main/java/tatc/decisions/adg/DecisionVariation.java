package tatc.decisions.adg;

import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.Variation;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.RealVariable;

import tatc.decisions.Decision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DecisionVariation implements Variation {

    private List<Decision> decisions;
    private Random rand = new Random();
    private Map<Decision, Object> childFragments;  // <--- store child fragment for each decision node


    public DecisionVariation(List<Decision> decisions) {
        this.decisions = decisions;
        this.childFragments = new HashMap<>();
        
    }

    @Override
    public int getArity() {
        // For crossover operators, typically 2. 
        // For mutation-only, 1. If you plan to handle both in one class, define accordingly.
        return 2; // Assuming crossover here
    }

    // @Override
    // public Solution[] evolve(Solution[] parents) {
    //     // Example: apply crossover to produce one child
    //     // 1) Decode each parent's architecture for each decision
    //     // 2) Apply decision crossover
    //     // 3) Re-encode into child's solution

    //     if (parents.length != 2) {
    //         throw new IllegalArgumentException("This operator requires two parents.");
    //     }

    //     Solution p1 = parents[0];
    //     Solution p2 = parents[1];

    //     // Create child solution (copy structure from p1)
    //     Solution child = p1.copy();

    //     int offset = 0;
    //     for (Decision d : decisions) {
    //         int vars = d.getNumberOfVariables();
    //         // Extract parent's encoded representation
    //         Object encodedP1 = extractEncoded(p1, offset, vars);
    //         Object encodedP2 = extractEncoded(p2, offset, vars);

    //         // Apply crossover using the decision's method
    //         Object encodedChild = d.crossover(encodedP1, encodedP2);

    //         // Put child's encoding back into solution variables
    //         injectEncoded(child, offset, encodedChild);
    //         offset += vars;
    //     }

    //     return new Solution[] { child };
    // }
    @Override
    public Solution[] evolve(Solution[] parents) {
        if (parents.length != 2) {
            throw new IllegalArgumentException("This operator requires two parents.");
        }
    
        // child solution structure initially copied from parent0
        Solution p1 = parents[0];
        Solution p2 = parents[1];
        Solution child = p1.copy(); 
    
        // We'll keep track of partial encodings for each node (DCi)
        int offset = 0;
        int childId = 0;
        for (int nodeIndex = 0; nodeIndex < decisions.size(); nodeIndex++) {
            Decision d = decisions.get(nodeIndex);
            int vars = d.getNumberOfVariables();
            int maxId = d.getHighestId();
            childId = maxId+1;
    
            // --- 1) Extract each parent's fragment
            Object p1Encoded = extractEncoded(p1, offset, vars);
            Object p2Encoded = extractEncoded(p2, offset, vars);
    
            // --- 2) Cross them to get child's encoded fragment
            Object childEncoded = d.crossover(p1Encoded, p2Encoded);
    
            // --- 3) (Optional) If this decision depends on previous decisions, 
            //         pass them to a repair operator for feasibility checks
            if (!d.getParentDecisions().isEmpty()) {
                // Suppose d depends on exactly one decision "parentNode"
                // The parent's new encoding is needed for feasibility checks
                Decision parentNode = d.getParentDecisions().get(0);
    
                // We figure out offset / stored child fragment for parentNode from earlier
                // OR we maintain a dictionary: decision -> childEncoded to look up by reference
                Object childParentEncoded = childFragments.get(parentNode);
    
                // Then call a specialized "repairWithDependency"
                childEncoded = d.repairWithDependency(childEncoded, childParentEncoded);
            }
    
            // --- 4) Inject child's final fragment into the solution
            injectEncoded(child, offset, childEncoded);
    
            // Store childEncoded so future decisions can reference it 
            childFragments.put(d, childEncoded);
            d.addEncodingById(childId, (int[]) childEncoded);
    
            offset += vars;
           
           
        }
        ((AdgSolution)child).setId(childId);

    
        return new Solution[]{ child };
    }


   /**
 * Safely reads an integer-encoded fragment from the solution, stopping if
 * the solution doesn’t have enough variables or the fragment is smaller than
 * the solution’s allocated region.
 */
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
    double epsilon = 1e-9;

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
            newVal = var.getUpperBound() - epsilon;
        }
        if (newVal < var.getLowerBound()) {
            newVal = var.getLowerBound() + epsilon;
        }
        var.setValue(newVal);
    }
    

    // If the solution has more variables than arr.length, fill the remainder with 0.0
    // or another sentinel value. This ensures we don't leave stale data behind.
    createReducedSolution(sol, 0, safeLength);
}


public Solution createReducedSolution(Solution original, int offset, int length) {
    // Create a new solution with reduced size
    Solution reduced = new Solution(length, original.getNumberOfObjectives(), original.getNumberOfConstraints());

    // Copy the necessary variables
    for (int i = 0; i < length; i++) {
        RealVariable originalVar = (RealVariable) original.getVariable(offset + i);
        RealVariable newVar = new RealVariable(originalVar.getLowerBound(), originalVar.getUpperBound());
        newVar.setValue(originalVar.getValue());
        reduced.setVariable(i, newVar);
    }

    // Copy objectives and constraints
    for (int i = 0; i < original.getNumberOfObjectives(); i++) {
        reduced.setObjective(i, original.getObjective(i));
    }
    for (int i = 0; i < original.getNumberOfConstraints(); i++) {
        reduced.setConstraint(i, original.getConstraint(i));
    }

    return reduced;
}


    
}
