package tatc.tradespaceiterator.search;

import tatc.architecture.variable.HeterogeneousWalkerVariable;

/**
 * Interface for heterogeneous walker variation operators. Variation operators manipulate one or more existing
 * solutions, called parents, to produce one or more new solutions, called children or offspring.
 */
public interface HeterogeneousWalkerVariation {

     /**
      * Evolves one or more parent heterogeneous walker solutions (specified by getArity) and produces one or more
      * child solutions.
      * @param vars the parent solutions
      * @param numberConstellations number of total constellations in the TAT-C chromosome (used to compute the variable probability of mutation)
      * @return the child solutions
      */
     HeterogeneousWalkerVariable[] evolve(HeterogeneousWalkerVariable[] vars, int numberConstellations);

     /**
      * Returns the number of solutions that must be supplied to the evolve method
      * @return the number of solutions that must be supplied to the evolve method
      */
     int getArity();
}
