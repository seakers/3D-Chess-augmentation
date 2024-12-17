package tatc.tradespaceiterator;

import org.moeaframework.algorithm.EpsilonMOEA;
import org.moeaframework.core.*;
import org.moeaframework.core.indicator.QualityIndicator;
import org.moeaframework.core.operator.*;
import tatc.ResultIO;
import tatc.architecture.variable.Decision;
import tatc.decisions.adg.DecisionMutation;
import tatc.decisions.adg.DecisionVariation;
import tatc.interfaces.GUIInterface;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Multi Objective Evolutionary Algorithm (MOEA) search strategy without AOS or KDO. It is the simplest evolutionary
 * algorithm search strategy.
 */

public class TradespaceSearchStrategyMOEAnew extends TradespaceSearchStrategyGAnew {

    /**
     * Constructs a Multi Objective Evolutionary Algorithm (MOEA) strategy
     * @param properties the problem properties
     */
    public TradespaceSearchStrategyMOEAnew(ProblemProperties properties) {
        super(properties);
    }

    public void start() {

        long startTime = System.nanoTime();

        ArrayList<Variation> iOperators = properties.getTradespaceSearch().getSettings().getSearchParameters().getiOperators();

        ArrayList<Variation> dOperators = properties.getTradespaceSearch().getSettings().getSearchParameters().getdOperators();

        CompoundVariation operators = new CompoundVariation();

        // for (int i = 0; i < iOperators.size(); i++) {
        //     operators.appendOperator(iOperators.get(i));
        // }

        // for (int i = 0; i < dOperators.size(); i++) {
        //     operators.appendOperator(dOperators.get(i));
        // }
        List<tatc.decisions.Decision> decisions = ((GAnew) problem).getDecisions();
        DecisionVariation crossoverOperator = new DecisionVariation(decisions);
        DecisionMutation mutationOperator = new DecisionMutation(decisions);
        operators.appendOperator(crossoverOperator);
        operators.appendOperator(mutationOperator);
        Initialization initialization = new RandomInitialization(this.problem, populationSize);
        Population initialPopulation = new Population();
        NondominatedPopulation nondominatedPopulation = new NondominatedPopulation(comparator);

        //create MOEA
        EpsilonMOEA emoea = new EpsilonMOEA(problem, initialPopulation, archive,
                selection, operators, initialization, comparator);

        //for all solutions found
        ArrayList<Solution> allSolutions = new ArrayList<>();

        //for unique solutions found
        HashSet<Solution> uniqueSolutions = new HashSet<>();

        //evaluate initial population first
        System.out.println(String.format("Initializing population... Size = %d", populationSize));
        emoea.step();

        //add initial population to the solutions list
        for (int j = 0; j < initialPopulation.size(); j++) {
            Solution s = initialPopulation.get(j);
            s.setAttribute("NFE", 0);
            allSolutions.add(s);
        }

        //calculate HV of initial population
        QualityIndicator qualityIndicator = new QualityIndicator(problem, emoea.getResult());
        HashMap<Integer, Double[]> hypervolume = new HashMap<>();

        while (!emoea.isTerminated() && emoea.getNumberOfEvaluations() < maxNFE) {
            emoea.step();
            double currentTime = ((System.nanoTime() - startTime) / Math.pow(10, 9)) / 60.;
            System.out.println(
                    String.format("%d NFE out of %d NFE: Time elapsed = %10f min."
                                    + " Approximate time remaining %10f min.",
                            emoea.getNumberOfEvaluations(), maxNFE, currentTime,
                            currentTime / emoea.getNumberOfEvaluations() * (maxNFE - emoea.getNumberOfEvaluations())));

            for (Solution solution : emoea.getPopulation()) {
                uniqueSolutions.add(solution);
            }

            //calculate metrics for search
            qualityIndicator.calculate(emoea.getResult());

            Double[] metrics = new Double[2];
            double hvValue = qualityIndicator.getHypervolume();
            double igd = qualityIndicator.getInvertedGenerationalDistance();
            metrics[0] = hvValue;
            metrics[1] = igd;

            System.out.println(String.format("NFE = %d, HV = %f, IGD = %f", emoea.getNumberOfEvaluations(), hvValue, igd));

            hypervolume.put(emoea.getNumberOfEvaluations(), metrics);
            ResultIO.saveLabels(emoea.getResult(), Paths.get(System.getProperty("tatc.output"), String.format("results%d",emoea.getNumberOfEvaluations())).toString(),",");
        }
        ResultIO.savePopulation(new Population(uniqueSolutions), Paths.get(System.getProperty("tatc.output"), ("uniqueSolutions")).toString());
        ResultIO.savePopulation(emoea.getArchive(), Paths.get(System.getProperty("tatc.output"), ("nonDominatedSolutions")).toString());
        ResultIO.saveLabels(emoea.getResult(), Paths.get(System.getProperty("tatc.output"), "results").toString(), ",");
        ResultIO.saveHyperVolume(hypervolume, Paths.get(System.getProperty("tatc.output"), "hypervolume").toString());
        emoea.terminate();
    }

    @Override
    public void validate() {

        GUIInterface gui = new GUIInterface();

        if (epsilon < 0 || epsilon > 1) {
            gui.sendResponses( "GUIURL", "urlparams","Epsilon values must lie between 0 and 1.");
        }
        if (pCrossover < 0 || pCrossover > 1) {
            gui.sendResponses( "GUIURL", "urlparams","Probability of crossover must lie between 0 and 1.");
        }
        if (pMutation < 0 || pMutation > 1) {
            gui.sendResponses( "GUIURL", "urlparams","Probability of mutation must lie between 0 and 1.");
        }
        else {
            gui.sendResponses("GUIURL","urlparams","Validation complete. Search has been initiated....");
        }
    }
}
