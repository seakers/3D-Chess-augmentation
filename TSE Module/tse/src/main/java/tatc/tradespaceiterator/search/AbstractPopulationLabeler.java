/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.tradespaceiterator.search;

import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;

/**
 * This interface is for classes that will label individuals in a population
 * with a group number. The labeled data can then be used with data mining or
 * machine learning methods.
 *
 * @author nozomihitomi
 */
public abstract class AbstractPopulationLabeler {

    /**
     * This is the attribute used to store the label values.
     */
    public static String LABELATTRIB = "label";

    /**
     * This method will label the solutions and store the label information in
     * the attributes of the solutions in the population. The attribute is
     * stored as "label"
     *
     * @param population
     * @return a population with the individuals labeled
     */
    public Population label(Population population) {
        process(population);
        for (Solution individual : population) {
            individual.setAttribute(LABELATTRIB, label(individual));
        }
        return population;
    }

    /**
     * This method should be overridden if the population must be processed
     * before its individuals are labeled. For example, if labeling the
     * non-dominated solutions, non-dominated filtering must be applied before
     * labels are given. The default method is to remove any old label
     * attributes from the solutions
     *
     * @param population the population to process
     */
    protected void process(Population population) {
        for (Solution individual : population) {
            individual.removeAttribute(LABELATTRIB);
        }
    }

    /**
     * This method will label a given individual with an integer value.
     *
     * @param individual
     * @return the label to
     */
    protected abstract int label(Solution individual);
}
