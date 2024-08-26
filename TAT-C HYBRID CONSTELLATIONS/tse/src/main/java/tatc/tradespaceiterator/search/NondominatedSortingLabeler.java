package tatc.tradespaceiterator.search;

import org.moeaframework.core.NondominatedSortingPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;

/**
 * This labeler will use nondominated sorting from NSGA-II to label the top x
 * fraction of the solutions
 *
 * @author nozomihitomi
 */
public class NondominatedSortingLabeler extends AbstractPopulationLabeler {

    public final double fraction;

    /**
     * Constructor for the labeler
     * @param fraction the top fraction of the population to label as good.
     */
    public NondominatedSortingLabeler(double fraction) {
        this.fraction = fraction;
    }

    @Override
    protected void process(Population population) {
        super.process(population);
        NondominatedSortingPopulation ndspop = new NondominatedSortingPopulation();
        ndspop.addAll(population);
        int numGood = (int)Math.floor(population.size()*fraction);
        ndspop.truncate(numGood);
        for(Solution individual : ndspop){
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

