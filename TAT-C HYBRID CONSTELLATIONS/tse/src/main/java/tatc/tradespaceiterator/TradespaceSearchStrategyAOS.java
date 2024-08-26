package tatc.tradespaceiterator;

import org.moeaframework.algorithm.EpsilonMOEA;
import org.moeaframework.core.*;
import org.moeaframework.core.indicator.QualityIndicator;
import org.moeaframework.core.operator.*;
import seakers.aos.aos.AOSMOEA;
import seakers.aos.creditassignment.setimprovement.SetImprovementDominance;
import seakers.aos.history.AOSHistoryIO;
import seakers.aos.operator.AOSVariation;
import seakers.aos.operator.AOSVariationSI;
import seakers.aos.operatorselectors.AdaptivePursuit;
import seakers.aos.operatorselectors.OperatorSelector;
import tatc.ResultIO;
import tatc.interfaces.GUIInterface;
import java.io.File;
import java.nio.file.Paths;
import java.util.*;

/**
 * Adaptive Operator Selection (AOS) search strategy. It uses an MOEA with an adaptive operator selector controlling
 * the use of the operators.
 */
public class TradespaceSearchStrategyAOS extends TradespaceSearchStrategyGA {

    /**
     * Constructs an Adaptive Operator Selection (AOS) search strategy
     * @param properties the problem properties
     */
    public TradespaceSearchStrategyAOS(ProblemProperties properties) {
        super(properties);
    }

    public void start() {

        long startTime = System.nanoTime();

        //create a collection of operators
        Collection<Variation> operators = new ArrayList<>();

        //get individual operators from a list
        //domain independent operators
        ArrayList<Variation> iOperators = properties.getTradespaceSearch().getSettings().getSearchParameters().getiOperators();
        CompoundVariation compoundVariation = new CompoundVariation();
        for (int i = 0; i <iOperators.size(); i++) {
            compoundVariation.appendOperator(iOperators.get(i));
        }

        //domain specific operators from the knowledge base
        //make a call to Knowledge Base Interface class to get operators specific to this problem
        ArrayList<Variation> dOperators = properties.getTradespaceSearch().getSettings().getSearchParameters().getdOperators();

        operators.add(compoundVariation);
        operators.addAll(dOperators);

        //initialize the population
        Initialization initialization = new RandomInitialization(this.problem, populationSize);
        Population initialPopulation = new Population();
        NondominatedPopulation nondominatedPopulation = new NondominatedPopulation(comparator);

        //create operator selector
        OperatorSelector operatorSelector = new AdaptivePursuit(operators, alpha, beta, pmin);

        //create credit assignment strategy
        //receives credit = 1 if the new solution is nondominated, credit = 0 if new solution is dominated
        SetImprovementDominance creditAssignment = new SetImprovementDominance(archive, 1, 0);

        //create AOS
        AOSVariation aosStrategy = new AOSVariationSI(operatorSelector, creditAssignment, populationSize);
        EpsilonMOEA emoea = new EpsilonMOEA(problem, initialPopulation, archive,
                selection, aosStrategy, initialization, comparator);
        AOSMOEA aos = new AOSMOEA(emoea, aosStrategy, true);

        //for all solutions found
        ArrayList<Solution> allSolutions = new ArrayList<>();

        //for unique solutions found
        HashSet<Solution> uniqueSolutions = new HashSet<>();

        System.out.println(String.format("Initializing population... Size = %d", populationSize));
        aos.step(); // to make sure that the initial population is evaluated

        //add initial population to the solutions list
        for (int j = 0; j < initialPopulation.size(); j++) {
            Solution s = initialPopulation.get(j);
            s.setAttribute("NFE", 0);
            allSolutions.add(s);
            nondominatedPopulation.add(s);
        }

        //calculate HV of initial population
        QualityIndicator qualityIndicator = new QualityIndicator(problem, aos.getResult());
        HashMap<Integer, Double[]> hypervolume = new HashMap<>();

        while (!aos.isTerminated() && aos.getNumberOfEvaluations() < maxNFE) {
            aos.step();
            double currentTime = ((System.nanoTime() - startTime) / Math.pow(10, 9)) / 60.;
            System.out.println(
                    String.format("%d NFE out of %d NFE: Time elapsed = %10f min."
                                    + " Approximate time remaining %10f min.",
                            aos.getNumberOfEvaluations(), maxNFE, currentTime,
                            currentTime / emoea.getNumberOfEvaluations() * (maxNFE - aos.getNumberOfEvaluations())));

            for (Variation op : aos.getOperatorSelector().getOperators()) {
                System.out.println(String.format("Using: %s", op.getClass().getSimpleName()));
            }

            for (Solution solution : aos.getPopulation()) {
                uniqueSolutions.add(solution);
            }

            //calculate metrics for search
            qualityIndicator.calculate(aos.getResult());

            Double[] metrics = new Double[2];
            double hvValue = qualityIndicator.getHypervolume();
            double igd = qualityIndicator.getInvertedGenerationalDistance();
            metrics[0] = hvValue;
            metrics[1] = igd;

            System.out.println(String.format("NFE = %d, HV = %f, IGD = %f", aos.getNumberOfEvaluations(), hvValue, igd));

            hypervolume.put(aos.getNumberOfEvaluations(), metrics);
            ResultIO.saveLabels(aos.getResult(), Paths.get(System.getProperty("tatc.output"), String.format("results%d",aos.getNumberOfEvaluations())).toString(),",");
        }
        ResultIO.savePopulation(new Population(uniqueSolutions), Paths.get(System.getProperty("tatc.output"), ("uniqueSolutions")).toString());
        ResultIO.savePopulation(aos.getArchive(), Paths.get(System.getProperty("tatc.output"), ("nonDominatedSolutions")).toString());
        ResultIO.saveLabels(aos.getResult(), Paths.get(System.getProperty("tatc.output"), "results").toString(), ",");
        ResultIO.saveHyperVolume(hypervolume, Paths.get(System.getProperty("tatc.output"), "hypervolume").toString());
        AOSHistoryIO.saveQualityHistory(aos.getQualityHistory(), new File(System.getProperty("tatc.output") + File.separator + "quality.text"), ",");
        AOSHistoryIO.saveCreditHistory(aos.getCreditHistory(), new File(System.getProperty("tatc.output") + File.separator + "credit.text"), ",");
        AOSHistoryIO.saveSelectionHistory(aos.getSelectionHistory(), new File(System.getProperty("tatc.output") + File.separator + "history.text"), ",");
        aos.terminate();
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
