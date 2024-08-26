/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.tradespaceiterator.search;

import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;

/**
 * Labels all the solutions on the front as 1 and all other as 0
 * @author Prachi
 */
public class PopulationLabeler extends AbstractPopulationLabeler{
    
    private NondominatedPopulation paretoFront;

    @Override
    protected void process(Population population) {
        super.process(population);
        paretoFront = new NondominatedPopulation(population);
        for (Solution individual : paretoFront) {
            individual.setAttribute(LABELATTRIB, 1);
        }
    }

    @Override
    protected int label(Solution individual) {
        //since all nondominated solutions will have been labeled already, only look for the unlabeled ones
        if (!individual.hasAttribute(LABELATTRIB)) {
            return 0;
        }else{
            return 1;
        }
    }
}
