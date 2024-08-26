package tatc.tradespaceiterator.search;


import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.Variation;
import tatc.architecture.variable.IntegerVariable;

/**
 * This operator is a uniform mutation over a set of integer valued variables
 */
public class VariableIntegerUM implements Variation {

    /**
     * Creates a new uniform mutation
     **/
    public VariableIntegerUM() {
    }


    @Override
    public int getArity() {
        return 1;
    }

    public Solution[] evolve(Solution[] parents, double probability) {
        Solution result = parents[0].copy();

        for (int i = 0; i < result.getNumberOfVariables(); i++) {
            Variable variable = result.getVariable(i);

            if ((PRNG.nextDouble() <= probability) && (variable instanceof IntegerVariable)) {
                evolve((IntegerVariable) variable);
            }
        }

        return new Solution[]{result};
    }


    @Override
    public Solution[] evolve(Solution[] parents) {
        Solution result = parents[0].copy();

        for (int i = 0; i < result.getNumberOfVariables(); i++) {
            Variable variable = result.getVariable(i);

            if ((PRNG.nextDouble() <= 1.0/result.getNumberOfVariables()) && (variable instanceof IntegerVariable)) {
                evolve((IntegerVariable) variable);
            }
        }

        return new Solution[]{result};
    }

    /**
     * Mutates the specified variable using uniform mutation.
     *
     * @param variable the variable to be mutated
     */
    public static void evolve(IntegerVariable variable) {
        variable.setValue(PRNG.nextInt(variable.getLowerBound(), variable
                .getUpperBound()));
    }
}
