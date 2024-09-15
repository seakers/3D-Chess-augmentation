package tatc.tradespaceiterator.search;

import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.operator.real.PM;
import org.moeaframework.core.variable.RealVariable;

public class VariablePM2 extends PM {
    public VariablePM2(double distributionIndex) {
        super(1.0D, distributionIndex);
    }

    public Solution[] evolve(Solution[] parents) {
        Solution result = parents[0].copy();

        for(int i = 0; i < result.getNumberOfVariables(); ++i) {
            Variable variable = result.getVariable(i);
            if (PRNG.nextDouble() <= 1.0D / (double)result.getNumberOfVariables() && variable instanceof RealVariable) {
                evolve((RealVariable)variable, this.getDistributionIndex());
            }
        }

        return new Solution[]{result};
    }

    public Solution[] evolve(Solution[] parents, double probability) {
        Solution result = parents[0].copy();

        for(int i = 0; i < result.getNumberOfVariables(); ++i) {
            Variable variable = result.getVariable(i);
            if (PRNG.nextDouble() <= probability && variable instanceof RealVariable) {
                evolve((RealVariable)variable, this.getDistributionIndex());
            }
        }

        return new Solution[]{result};
    }
}
