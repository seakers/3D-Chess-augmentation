package tatc.tradespaceiterator.search;

/**
 * This class implements mutation for chromosomes
 * that have two different variable types - Integer and Real variable.
 *
 * @author Prachi
 */

import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.Variation;
import tatc.architecture.variable.IntegerVariable;
import tatc.architecture.variable.RealVariable;


public class VariableMutation implements Variation {
    private final double probability;

    public VariableMutation(double probability) {
        this.probability = probability;
    }

    public static void evolve(IntegerVariable variable) {
        variable.setValue(PRNG.nextInt(variable.getLowerBound(), variable.getUpperBound()));
    }

    public static void evolve(RealVariable variable) {
        variable.setValue(PRNG.nextFloat());
    }

    public int getArity() {
        return 1;
    }

    public Solution[] evolve(Solution[] parents) {
        Solution result = parents[0].copy();

        for (int i = 0; i < result.getNumberOfVariables(); ++i) {
            Variable variable = result.getVariable(i);
            if (this.probability <= 1 && this.probability > 0) {
                if (variable instanceof IntegerVariable) {
                    variable.randomize();
                    evolve((IntegerVariable) variable);
                } else {
                    evolve((RealVariable) variable);
                }
            }
        }
            return new Solution[]{result};
        }
}
