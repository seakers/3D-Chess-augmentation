package tatc.decisions.adg;

import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.Variation;
import org.moeaframework.core.variable.EncodingUtils;

import tatc.decisions.Decision;
import java.util.List;
import java.util.Random;

public class DecisionVariation implements Variation {

    private List<Decision> decisions;
    private Random rand = new Random();

    public DecisionVariation(List<Decision> decisions) {
        this.decisions = decisions;
    }

    @Override
    public int getArity() {
        // For crossover operators, typically 2. 
        // For mutation-only, 1. If you plan to handle both in one class, define accordingly.
        return 2; // Assuming crossover here
    }

    @Override
    public Solution[] evolve(Solution[] parents) {
        // Example: apply crossover to produce one child
        // 1) Decode each parent's architecture for each decision
        // 2) Apply decision crossover
        // 3) Re-encode into child's solution

        if (parents.length != 2) {
            throw new IllegalArgumentException("This operator requires two parents.");
        }

        Solution p1 = parents[0];
        Solution p2 = parents[1];

        // Create child solution (copy structure from p1)
        Solution child = p1.copy();

        int offset = 0;
        for (Decision d : decisions) {
            int vars = d.getNumberOfVariables();
            // Extract parent's encoded representation
            Object encodedP1 = extractEncoded(p1, offset, vars);
            Object encodedP2 = extractEncoded(p2, offset, vars);

            // Apply crossover using the decision's method
            Object encodedChild = d.crossover(encodedP1, encodedP2);

            // Put child's encoding back into solution variables
            injectEncoded(child, offset, encodedChild);
            offset += vars;
        }

        return new Solution[] { child };
    }

    /**
     * Extract the encoded representation for a given decision from a solution's variables.
     */
    private Object extractEncoded(Solution sol, int offset, int length) {
        int[] encoding = new int[length];
        for (int i = 0; i < length; i++) {
            Variable var = sol.getVariable(offset + i);
            // Now use EncodingUtils on the variable
            encoding[i] = EncodingUtils.getInt(var);
        }
        return encoding;
    }
    
    private void injectEncoded(Solution sol, int offset, Object encoded) {
        int[] arr = (int[]) encoded;
        for (int i = 0; i < arr.length; i++) {
            Variable var = sol.getVariable(offset + i);
            EncodingUtils.setInt(var, arr[i]);
        }
    }
    
    
}
