/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.architecture;

import org.moeaframework.core.Solution;

/**
 * Solution with standard form decisions
 */
public class StandardFormArchitecture extends Solution {

    private static final long serialVersionUID = -453938510114132596L;

    /**
     * Constructs a standard form architecture solution
     * @param numberOfVariables the number of decisions or variables
     * @param numberOfObjectives the number of objectives
     * @param numberOfConstraints the number of constraints
     */
    public StandardFormArchitecture(int numberOfVariables, int numberOfObjectives, int numberOfConstraints) {
        super(numberOfVariables, numberOfObjectives, numberOfConstraints);
    }

    /**
     * Private constructor used for copying solution
     * @param solution 
     */
    private StandardFormArchitecture(Solution solution) {
        super(solution);
    }

    @Override
    public Solution copy() {
        return new StandardFormArchitecture(this);
    }
    
}
