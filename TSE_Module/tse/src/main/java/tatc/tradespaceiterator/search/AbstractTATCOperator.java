/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.tradespaceiterator.search;

import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import tatc.architecture.StandardFormArchitecture;

/**
 *
 * @author Prachi
 */
public abstract class AbstractTATCOperator implements Variation {

    @Override
    public Solution[] evolve(Solution[] sltns) {
        if (!(sltns[0] instanceof StandardFormArchitecture)) {
            throw new IllegalArgumentException("Expected StandardFormArchitecture instance. Found " + sltns[0].getClass());
        }
        StandardFormArchitecture child = (StandardFormArchitecture) sltns[0].copy();
        return new Solution[]{evolve(child)};
    }

    protected abstract StandardFormArchitecture evolve(StandardFormArchitecture child);

}
