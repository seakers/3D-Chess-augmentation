package tatc.architecture.specifications;

import com.google.gson.annotations.SerializedName;
import org.moeaframework.core.Variation;
import org.moeaframework.core.operator.CompoundVariation;
import org.moeaframework.core.operator.TwoPointCrossover;
import org.moeaframework.core.operator.real.PM;
import org.moeaframework.core.operator.real.SBX;
import seakers.conmop.operators.OrbitElementOperator;
import seakers.conmop.operators.VariableLengthOnePointCrossover;
import seakers.conmop.operators.VariablePM;
import seakers.conmop.util.Bounds;
import tatc.tradespaceiterator.search.*;
import tatc.util.Factor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that contains the values of all the parameters needed to set up the genetic algorithm.
 */
public class SearchParameters implements Serializable {
    /**
     * Type tag for JSON file
     */
    @SerializedName("@type")
    private final String _type = "SearchParameters";
    /**
     * Maximum number of function evaluations
     */
    private final Integer maxNFE;
    /**
     * Size of the initial population of solutions
     */
    private final Integer populationSize;
    /**
     * List of epsilons for the dominance archive (one per objective)
     */
    private final Double epsilons;
    /**
     * Size of the tournament selection
     */
    private final Integer sizeTournament;
    /**
     * Probability of crossover
     */
    private final Double pCrossover;
    /**
     * Probability of mutation
     */
    private final Double pMutation;
    /**
     * Learning rate parameter for credit updates in adaptive operator selection
     */
    private final Double alpha;
    /**
     * Learning rate parameter for probability updates in adaptive operator selection
     */
    private final Double beta;
    /**
     * Minimum probability of selection for adaptive operator selection
     */
    private final Double pmin;
    /**
     * List of domain-independent operators
     */
    private final ArrayList<String> iOperators;
    /**
     * List of domain-dependent operators
     */
    private final ArrayList<String> dOperators;
    /**
     * Number of evaluations between successive rule mining algorithm applications
     */
    private final Integer NFEtriggerDM;
    /**
     * Number of operators to replace after each rule mining
     */
    private final Integer nOperRepl;

    /**
     * Constructs a search parameters object
     * @param maxNFE the maximum number of function evaluations
     * @param populationSize the size of the initial population of solutions
     * @param epsilons the list of epsilons for the dominance archive (one per objective)
     * @param sizeTournament the size of the tournament selection
     * @param pCrossover the probability of crossover
     * @param pMutation the probability of mutation
     * @param alpha the learning rate parameter for credit updates in adaptive operator selection
     * @param beta the learning rate parameter for probability updates in adaptive operator selection
     * @param pmin the minimum probability of selection for adaptive operator selection
     * @param iOperators the list of domain-independent operators
     * @param dOperators the list of domain-dependent operators
     * @param NFEtriggerDM the number of evaluations between successive rule mining algorithm applications
     * @param nOperRepl the number of operators to replace after each rule mining
     */
    public SearchParameters(int maxNFE, int populationSize, Double epsilons, int sizeTournament, double pCrossover, double pMutation, double alpha, double beta, double pmin, ArrayList<String> iOperators, ArrayList<String> dOperators, int NFEtriggerDM, int nOperRepl) {
        this.maxNFE = maxNFE;
        this.populationSize = populationSize;
        this.epsilons = epsilons;
        this.sizeTournament = sizeTournament;
        this.pCrossover = pCrossover;
        this.pMutation = pMutation;
        this.alpha = alpha;
        this.beta = beta;
        this.pmin = pmin;
        this.iOperators = iOperators;
        this.dOperators = dOperators;
        this.NFEtriggerDM = NFEtriggerDM;
        this.nOperRepl = nOperRepl;
    }

    /**
     * Gets the maximum number of function evaluations
     * @return the maximum number of function evaluations
     */
    public int getMaxNFE() {
        return maxNFE;
    }

    /**
     * Gets the size of the initial population of solutions
     * @return the size of the initial population of solutions
     */
    public int getPopulationSize() {
        return populationSize;
    }

    /**
     * Gets the list of epsilons for the dominance archive (one per objective)
     * @return the list of epsilons for the dominance archive (one per objective)
     */
    public Double getEpsilons() {
        return epsilons;
    }

    /**
     * Gets the size of the tournament selection
     * @return the size of the tournament selection
     */
    public int getSizeTournament() {
        return sizeTournament;
    }

    /**
     * Gets the probability of crossover
     * @return the probability of crossover
     */
    public double getpCrossover() {
        return pCrossover;
    }

    /**
     * Gets the probability of mutation
     * @return the probability of mutation
     */
    public double getpMutation() {
        return pMutation;
    }

    /**
     * Gets the learning rate parameter for credit updates in adaptive operator selection
     * @return the learning rate parameter for credit updates in adaptive operator selection
     */
    public double getAlpha() {
        return alpha;
    }

    /**
     * Gets the learning rate parameter for probability updates in adaptive operator selection
     * @return the learning rate parameter for probability updates in adaptive operator selection
     */
    public double getBeta() {
        return beta;
    }

    /**
     * Gets the minimum probability of selection for adaptive operator selection
     * @return the minimum probability of selection for adaptive operator selection
     */
    public double getPmin() {
        return pmin;
    }

    /**
     * Gets the list of domain-independent operators
     * @return the list of domain-independent operators
     */
    public ArrayList<Variation> getiOperators() {
        ArrayList<Variation> independentOperators = new ArrayList<>();
        for (String s : this.iOperators){
            if (s.equals("IntegerUM")){
                independentOperators.add(new HybridOperatorMutation(new VariableIntegerUM(), new VariablePM2(20), new HeterogeneousWalkerMutation(new VariableIntegerUM())));
            }else if (s.equals("TwoPointCrossover")){
                HeterogeneousWalkerVariation[] cutAndSpliceAndPairing = {new CutAndSpliceOperator(getpCrossover()),new PairingOperator(getpCrossover())};
                independentOperators.add(new HybridOperatorCrossover(new TwoPointCrossover(getpCrossover()), new SBX(getpCrossover(),20),cutAndSpliceAndPairing));
            }else if (s.equals("General")){
                Variation[] operators = {new OrbitElementOperator(new CompoundVariation(new SBX(getpCrossover(), 20), new VariablePM(20))),
                        new CompoundVariation(new VariableLengthOnePointCrossover(getpCrossover(), new Bounds<>(1,12)), new VariablePM(20))};
                GeneralOperator generalOperator = new GeneralOperator(operators);
                independentOperators.add(generalOperator);
            }
        }
        return independentOperators;
    }

    /**
     * Gets the list of domain-dependent operators
     * @return the list of domain-dependent operators
     */
    public ArrayList<Variation> getdOperators() {
        ArrayList<Variation> dependentOperators = new ArrayList<>();
//        try {
//            for (String operator : this.dOperators) {
//                HeterogeneousWalkerVariation[] cutAndSpliceAndPairing = {new CutAndSpliceOperator(getpCrossover()),new PairingOperator(getpCrossover())};
//                HybridOperatorCrossover hybridOperatorCrossover = new HybridOperatorCrossover(new TwoPointCrossover(getpCrossover()),cutAndSpliceAndPairing);
//                HybridOperatorMutation mutation = new HybridOperatorMutation(new IntegerUM(pMutation), new HeterogeneousWalkerMutation(new IntegerUM(pMutation)), pMutation);
//                CompoundVariation compoundVariation = new CompoundVariation(hybridOperatorCrossover, new Operator(operator), mutation);
//
//                //add Compound Variation operator to the pool
//                StringBuilder sb = new StringBuilder();
//                sb.append(hybridOperatorCrossover.getClass().getSimpleName()).append("+");
//                sb.append((operator)).append("+");
//                sb.append(mutation.getClass().getSimpleName());
//                compoundVariation.setName(sb.toString());
//                dependentOperators.add(compoundVariation);
//            }
//        } catch (Exception ex) {
//            Logger.getLogger(Factor.class.getName()).log(Level.SEVERE, "Invalid Operator! ", ex);
//        }
        return dependentOperators;
    }

    /**
     * Gets the number of evaluations between successive rule mining algorithm applications
     * @return the number of evaluations between successive rule mining algorithm applications
     */
    public int getNFEtriggerDM() {
        return NFEtriggerDM;
    }

    /**
     * Gets the number of operators to replace after each rule mining
     * @return the number of operators to replace after each rule mining
     */
    public int getnOperRepl() {
        return nOperRepl;
    }
}
