package tatc.tradespaceiterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.moeaframework.core.*;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.RandomInitialization;
import org.moeaframework.core.operator.TournamentSelection;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import tatc.decisions.Decision;
import tatc.decisions.adg.Graph;
import tatc.decisions.search.AdgInitialization;

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
        this.initialization = new AdgInitialization(this.problem, populationSize);
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
        // Build the graph from the TSERequest JSON in properties
        // We assume Graph.Builder or a similar approach. For example:
        // Create a Graph from properties.getTsrObject()
        org.json.JSONObject tseRequestJson = properties.getTsrObject();
        // Suppose we have a method that constructs the Graph from tseRequestJson
        tatc.decisions.adg.Graph graph = buildGraphFromTSERequest(tseRequestJson, properties);
        // Determine number of objectives from properties
        int totalObjectives = properties.getObjectives().size();
        // Create and return a Problem that uses these decisions
        // GAnew should be updated to rely on the graph and its decisions rather than building them here
        return new GAnew(properties, graph, totalObjectives);
    }

/**
 * Example helper method to build the graph from the TSERequest.
 * This is a placeholder; implement as needed.
 */
private tatc.decisions.adg.Graph buildGraphFromTSERequest(org.json.JSONObject tseRequestJson, ProblemProperties properties) {
    Graph graph = new Graph(properties);

    return graph;
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
