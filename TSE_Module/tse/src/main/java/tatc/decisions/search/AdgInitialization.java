package tatc.decisions.search;


import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.operator.RandomInitialization;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Custom initialization where you can generate solutions in any way you like.
 * For instance, hooking into your own ADG-based random generator, or reading
 * from an external file, etc.
 */
public class AdgInitialization extends RandomInitialization{ 
    private final Problem problem;
    private final int populationSize;
    private final Random rand;

    public AdgInitialization(Problem problem, int populationSize) {
        // By default, RandomInitialization also needs (problem, populationSize).
        super(problem, populationSize);
        this.problem = problem;
        this.populationSize = populationSize;
        this.rand = new Random();
    }

    @Override
	public Solution[] initialize() {
		Solution[] initialPopulation = new Solution[populationSize];

		for (int i = 0; i < populationSize; i++) {
			Solution solution = problem.newSolution();
			initialPopulation[i] = solution;
		}

		return initialPopulation;
	}

}
