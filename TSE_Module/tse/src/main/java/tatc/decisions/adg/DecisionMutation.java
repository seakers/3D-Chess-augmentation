package tatc.decisions.adg;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
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

    private Object extractEncoded(Solution sol, int offset, int length) {
        int[] encoding = new int[length];
        for (int i = 0; i < length; i++) {
            RealVariable var = (RealVariable) sol.getVariable(offset + i);
            encoding[i] = (int) Math.round(var.getValue()); // Convert double to int
        }
        return encoding;
    }
    
    private void injectEncoded(Solution sol, int offset, Object encoded) {
        int[] arr = (int[]) encoded;
        double epsilon = 1e-9; // small epsilon to ensure we stay within bounds
    
        for (int i = 0; i < arr.length; i++) {
            RealVariable var = (RealVariable) sol.getVariable(offset + i);
    
            double maxVal = var.getUpperBound();
            double newVal = (double) arr[i];
    
            // If newVal is equal or very close to maxVal, ensure we don't surpass it due to floating-point issues
            if (newVal > maxVal) {
                newVal = maxVal - epsilon;
            }
    
            var.setValue(newVal);
        }
    }
    
    

}
