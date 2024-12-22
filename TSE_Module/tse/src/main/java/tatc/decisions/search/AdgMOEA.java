package tatc.decisions.search;
import org.moeaframework.core.*;
import org.moeaframework.algorithm.EpsilonMOEA;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.comparator.ChainedComparator;
import org.moeaframework.core.operator.CompoundVariation;

/**
 * A specialized version of EpsilonMOEA for your ADG-based problem,
 * which you can tweak as needed.
 */
public class AdgMOEA extends EpsilonMOEA {

    /**
     * Creates a specialized EpsilonMOEA with your custom or default components.
     *
     * @param problem       the problem definition
     * @param population    the population (initially empty or partially-filled)
     * @param archive       the EpsilonBoxDominanceArchive
     * @param variation     the Variation operator(s) (crossover, mutation, etc.)
     * @param initialization your custom initialization, e.g., MyCustomInitialization
     * @param epsilon       array (or single) for the epsilon values
     */
    private double[] epsilon;
    public AdgMOEA(Problem problem,
                                   Population population,
                                   EpsilonBoxDominanceArchive archive,
                                   Variation variation,
                                   Initialization initialization,
                                   double[] epsilon) {
        // We define the selection operator, etc. here
        super(problem,
              population,
              archive,
              // e.g., a basic tournament selection with Pareto + crowding, or just Pareto:
              new TournamentSelection(2,
                  new ChainedComparator(new ParetoDominanceComparator())),
              variation,
              initialization
        );
        this.epsilon = epsilon;
    }

    /**
     * Optionally override `initialize()` if you want to do more
     * than just calling super.initialize().
     */
    @Override
    protected void initialize() {
        // Example: do something custom prior to calling super
        System.out.println("PersonalizedEpsilonMOEA: Custom initialization step");
        super.initialize();
        // Optionally, do post-processing after the parent's initialization
    }

    /**
     * Optionally override `iterate()` or `step()` if needed to tweak the main loop logic.
     */
    @Override
    public void iterate() {
        // If you want to do something custom each iteration, do it here
        // e.g. printing debug info or applying adaptive logic
        super.iterate();
    }

    /**
     * If you want to store or modify the final result, you can override `terminate()`.
     */
    @Override
    public void terminate() {
        // Perform any final actions prior to or after calling super
        super.terminate();
    }
}
