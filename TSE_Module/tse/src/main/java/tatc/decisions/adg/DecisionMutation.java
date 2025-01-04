package tatc.decisions.adg;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.Variation;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.RealVariable;

import tatc.architecture.variable.IntegerVariable;
import tatc.decisions.Decision;
import java.util.List;
import java.util.Random;
public class DecisionMutation implements Variation {

    private List<Decision> decisions;
    private Random rand = new Random();

    public DecisionMutation(List<Decision> decisions) {
        this.decisions = decisions;
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
        Solution child = parent.copy();

        int offset = 0;
        for (Decision d : decisions) {
            int vars = d.getNumberOfVariables();
            Object encodedChild = extractEncoded(child, offset, vars);
            d.mutate(encodedChild);
            injectEncoded(child, offset, encodedChild);
            offset += vars;
        }

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
    for (int i = safeLength; i < maxAvailable; i++) {
        RealVariable var = (RealVariable) sol.getVariable(offset + i);
        var.setValue(0.0); // or another “do-nothing” sentinel
    }
}
    

}
