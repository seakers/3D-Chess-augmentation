package tatc.tradespaceiterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.moeaframework.core.*;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.RandomInitialization;
import org.moeaframework.core.operator.TournamentSelection;

import tatc.decisions.Decision;

/**
 * Abstract class for any tradespace search strategy involving an optimization evolutionary algorithm
 */
public abstract class TradespaceSearchStrategyGAnew implements TradespaceSearchStrategy {

    /**
     * Problem properties
     */
    public final ProblemProperties properties;
    /**
     * The optimization problem definition
     */
    public final Problem problem;
    /**
     * Maximum number of function evaluations
     */
    int maxNFE;
    /**
     * Population size
     */
    int populationSize;
    /**
     * Initialization object
     */
    Initialization initialization;
    /**
     * Population object
     */
    Population population;
    /**
     * Comparator
     */
    DominanceComparator comparator;
    /**
     * Epsilon for the dominance archive
     */
    Double epsilon;
    /**
     * Epsilon box dominance archive
     */
    EpsilonBoxDominanceArchive archive;
    /**
     * The tournament selection
     */
    TournamentSelection selection;
    /**
     * Probability of crossover
     */
    Double pCrossover;
    /**
     * Probability of mutation
     */
    Double pMutation;
    /**
     * Learning rate parameter for credit updates in adaptive operator selection
     */
    Double alpha;
    /**
     * Learning rate parameter for probability updates in adaptive operator selection
     */
    Double beta;
    /**
     * Minimum probability of selection for adaptive operator selection
     */
    Double pmin;
    /**
     * Number of operators to replace after each rule mining
     */
    int nOperRepl;
    /**
     * Number of evaluations between successive rule mining algorithm applications
     */
    int getNFEtriggerDM;

    /**
     * Constructs an genetic algorithm search strategy
     * @param properties the problem properties
     */
    public TradespaceSearchStrategyGAnew(ProblemProperties properties) {

        this.properties = properties;
        this.problem = createProblem(properties);
        this.maxNFE = properties.getTradespaceSearch().getSettings().getSearchParameters().getMaxNFE();
        this.populationSize = properties.getTradespaceSearch().getSettings().getSearchParameters().getPopulationSize();
        this.initialization = new RandomInitialization(this.problem, populationSize);
        this.population = new Population();
        this.comparator = new ParetoDominanceComparator();
        this.epsilon = properties.getTradespaceSearch().getSettings().getSearchParameters().getEpsilons();
        this.archive = new EpsilonBoxDominanceArchive(epsilon);
        this.pCrossover = properties.getTradespaceSearch().getSettings().getSearchParameters().getpCrossover();
        this.pMutation = properties.getTradespaceSearch().getSettings().getSearchParameters().getpMutation();
        this.alpha = properties.getTradespaceSearch().getSettings().getSearchParameters().getAlpha();
        this.beta = properties.getTradespaceSearch().getSettings().getSearchParameters().getBeta();
        this.nOperRepl = properties.getTradespaceSearch().getSettings().getSearchParameters().getnOperRepl();
        this.pmin = properties.getTradespaceSearch().getSettings().getSearchParameters().getPmin();
        this.getNFEtriggerDM = properties.getTradespaceSearch().getSettings().getSearchParameters().getNFEtriggerDM();
        this.selection = new TournamentSelection(properties.getTradespaceSearch().getSettings().getSearchParameters().getSizeTournament(), comparator);
    }

    /**
     * Creates the optimization problem
     * @param properties the problem properties
     * @return the optimization problem
     * @throws IllegalArgumentException
     */
    protected Problem createProblem(ProblemProperties properties) throws IllegalArgumentException {
        // Extract decision variables and their patterns
        Map<String, String> decisionVariables = properties.getDecisionVariables();
        //Map<String, List<Object>> variableValues = properties.getDecisionVariableValues(decisionVariables);

        // Create decision objects
        List<Decision> decisions = new ArrayList<>();
        // We have a JSON object that defines decision variables; needed for Combining decisions
        org.json.JSONObject designSpace = properties.getTsrObject().getJSONObject("designSpace");
        org.json.JSONObject decisionVariablesObject = designSpace.getJSONObject("decisionVariables");

        for (Map.Entry<String, String> entry : decisionVariables.entrySet()) {
            String varName = entry.getKey();
            String varType = entry.getValue();

            if (varType.equalsIgnoreCase("Combining")) {
                // Construct a Combining decision
                tatc.decisions.Combining comb = new tatc.decisions.Combining(properties, varName);

                // Extract sub-decision names from the TSE request
                org.json.JSONObject varObj = decisionVariablesObject.getJSONObject(varName);
                org.json.JSONArray subDecs = varObj.getJSONArray("combiningDecisions");
                List<String> subDecisionNames = new ArrayList<>();
                for (int i = 0; i < subDecs.length(); i++) {
                    subDecisionNames.add(subDecs.getString(i));
                }
                comb.setSubDecisions(subDecisionNames);

                // For each sub-decision, we must deduce their distinct values.
                // The variableValues map for a "Combining" decision currently contains a list of combinations (cartesian product).
                // However, the Combining class expects a separate list of alternatives for each sub-decision.
                // We'll directly fetch distinct values for each sub-decision again from properties.
                List<List<Object>> alternatives = new ArrayList<>();
                for (String subVar : subDecisionNames) {
                    // Re-use the logic from properties to find distinct values for each subVar
                    List<Object> subVarValues = properties.getDistinctValuesForVariable(subVar);
                    alternatives.add(subVarValues);
                }

                comb.setAlternatives(alternatives);
                comb.initializeDecisionVariables(); // Ensure internal structures are ready
                decisions.add(comb);
            } 
            else if (varType.equalsIgnoreCase("Assigning")) {
                // Future: Once Assigning decision is implemented, create it here
                // Assigning assign = new Assigning(properties, varName);
                // ... configure assign ...
                // decisions.add(assign);
            } 
            else if (varType.equalsIgnoreCase("Partitioning")) {
                // Future: Partitioning decision
            } 
            else if (varType.equalsIgnoreCase("Permuting")) {
                // Future: Permuting decision
            } 
            else {
                // Unknown or not yet implemented decision pattern
                // For now, ignore or throw exception
                System.err.println("Unknown decision pattern: " + varType + " for variable: " + varName);
            }
        }

        // Determine number of objectives
        int totalObjectives = properties.getObjectives().size();

        // Create and return the GAnew problem
        return new GAnew(properties, decisions, totalObjectives);
    }

    /**
     * This method validates TradespaceSearch.JSON according to the search strategy selected
     * and throws error to the user (GUI) if any parameter values don't make sense.
     */
    public abstract void validate();

    /**
     * Method that starts the search algorithm
     */
    public abstract void start();
}
