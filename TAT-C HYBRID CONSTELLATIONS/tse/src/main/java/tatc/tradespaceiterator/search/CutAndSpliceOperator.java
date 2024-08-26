package tatc.tradespaceiterator.search;

import java.util.*;

import org.hipparchus.util.FastMath;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.operator.real.SBX;
import org.moeaframework.core.variable.RealVariable;
import tatc.architecture.specifications.Satellite;
import tatc.architecture.variable.HeterogeneousWalkerVariable;
import tatc.architecture.variable.PlaneVariable;
import tatc.util.Enumeration;
import tatc.util.Utilities;

/**
 * This "crossover" operator is intended for a variable length chromosome in which the
 * number of planes (p) in a heterogeneous constellation are allowed to vary and a gene is
 * defined as a the total number of satellites (ns) in the constellation and p plane variables
 * with their associated pair of altitude and inclination. Each plane has ns/p satellites.
 * It first identifies the parent with fewer number of planes, creates a random crossover point,
 * performs crossover with the plane variables, selects the number of satellites for the offspring (giving
 * priority to the number of satellites from the parents if possible or selecting a random number from
 * the allowed values in the tsr otherwise). Finally selects the satellite object with 0.5 probability
 * from the two parents.
 */
public class CutAndSpliceOperator implements Variation, HeterogeneousWalkerVariation {

    /**
     * The probability of applying this operator to solutions.
     */
    private final double probability;

    /**
     * Flag to declare whether there should be a cross point per chromosome or
     * if the crosspoint should be selected from the shorter of the two
     * chromosomes
     */
    private final boolean doubleCrossPoint;


    /**
     * Constructs a variable chromosome length cut and splice operator with
     * the specified probability of applying this operator to solutions.
     *
     * @param probability the probability of applying this operator to solutions
     * @param doubleCrossPoint Flag to declare whether there should be a cross
     * point per chromosome or if the crosspoint should be selected from the
     * shorter of the two chromosomes
     */
    private CutAndSpliceOperator(double probability, boolean doubleCrossPoint) {
        this.probability = probability;
        this.doubleCrossPoint = doubleCrossPoint;

    }

    /**
     * Constructs a variable chromosome length cut and splice operator with
     * the specified probability of applying this operator to solutions.
     * Flag to declare whether there should be a cross point per chromosome
     * or if the crosspoint should be selected from the shorter of the two
     * chromosomes (doubleCrossPoint) is set to false
     *
     * @param probability the probability of applying this operator to solutions
     */
    public CutAndSpliceOperator(double probability) {
        this(probability, false);
    }

    @Override
    public Solution[] evolve(Solution[] parents) {
        Solution result1 = parents[0].copy();
        Solution result2 = parents[1].copy();

        if (PRNG.nextDouble() <= probability) {
            for (int i = 0; i < result1.getNumberOfVariables(); i++) {
                if (result1.getVariable(i) instanceof HeterogeneousWalkerVariable
                        && result2.getVariable(i) instanceof HeterogeneousWalkerVariable) {

                    HeterogeneousWalkerVariable constel1 = (HeterogeneousWalkerVariable) result1.getVariable(i);
                    HeterogeneousWalkerVariable constel2 = (HeterogeneousWalkerVariable) result2.getVariable(i);
                    HeterogeneousWalkerVariable[] constels = {constel1, constel2};

                    HeterogeneousWalkerVariable[] newVars = evolve(constels,-1);

                    result1.setVariable(i, newVars[0]);
                    result2.setVariable(i, newVars[1]);
                }
            }
        }

        return new Solution[]{result1, result2};
    }

    public HeterogeneousWalkerVariable[] evolve(HeterogeneousWalkerVariable[] vars, int numberConstellations) {
        HeterogeneousWalkerVariable var1 = (HeterogeneousWalkerVariable) vars[0].copy();
        HeterogeneousWalkerVariable var2 = (HeterogeneousWalkerVariable) vars[1].copy();

        //identify the constellation with fewer satellites
        HeterogeneousWalkerVariable constel1;
        HeterogeneousWalkerVariable constel2;
        if (var1.getNumberOfPlanes() <= var2.getNumberOfPlanes()) {
            constel1 = var1;
            constel2 = var2;
        } else {
            constel1 = var2;
            constel2 = var1;
        }

        if (constel1.getNumberOfPlanes() > 1 && constel2.getNumberOfPlanes() > 1) {
            //First select random cross point
            int crossoverPoint1 = PRNG.nextInt(1, constel1.getNumberOfPlanes() - 1);
            //select the second cross point based on the first cross point in
            //order to create offspring with constellations within allowable
            //bounds on the number of planes
            int crossoverPoint2;
            if (doubleCrossPoint) {
                //identify furthest possible right side index for second cross site
                int boundedCrossPoint = (Collections.max(var1.getPlanesAllowed()) - 1) - (crossoverPoint1 + 1);
                int maxRightCrossPoint = FastMath.min(boundedCrossPoint, constel2.getNumberOfPlanes() - 1);

                //identify furthest possible left side index for second cross site
                //nSats.getLowerBound() - crossoverPoint1 because absolute minimum is 1 and max is
                int minLeftCrossPoint = FastMath.max(1, Collections.min(var1.getPlanesAllowed()) - crossoverPoint1);

                crossoverPoint2 = PRNG.nextInt(minLeftCrossPoint, maxRightCrossPoint);
            } else {
                crossoverPoint2 = crossoverPoint1;
            }

            ArrayList<PlaneVariable> pList1 = new ArrayList<>();
            ArrayList<PlaneVariable> pList2 = new ArrayList<>();
            Iterator<PlaneVariable> iter1 = constel1.getPlaneVariables().iterator();
            Iterator<PlaneVariable> iter2 = constel2.getPlaneVariables().iterator();

            //exchange the first few planes until the crossover point
            for (int j = 0; j < crossoverPoint1; j++) {
                pList2.add(iter1.next());
            }

            //exchange the first few planes until the crossover point
            for (int j = 0; j < crossoverPoint2; j++) {
                pList1.add(iter2.next());
            }

            //place the rest of constel1 planes in list1
            while (iter1.hasNext()) {
                pList1.add(iter1.next());
            }
            //place the rest of constel2 planes in list2
            while (iter2.hasNext()) {
                pList2.add(iter2.next());
            }

            /*
            Choose the number of satellites for the two childs (the number of satellites from any of the 2 parents is
            prioritized. If they are not feasible due to the new number of planes of the childs, a random number of
            satellites is picked from the allowed values defined in the tradespace.
             */
            int t1previous = var1.getT();
            int t2previous = var2.getT();
            int t1;
            int t2;
            ArrayList<Integer> t1Possibles = Enumeration.allowedNumberOfSatellites(pList1.size(),var1.gettAllowed());
            ArrayList<Integer> t2Possibles = Enumeration.allowedNumberOfSatellites(pList2.size(),var1.gettAllowed());
            if (t1Possibles.contains(t1previous) && t1Possibles.contains(t2previous)){
                double p = PRNG.nextDouble();
                if (p < 0.5){
                    t1=t1previous;
                }else{
                    t1=t2previous;
                }
            }else if (t1Possibles.contains(t1previous)){
                t1=t1previous;
            }else if (t1Possibles.contains(t2previous)){
                t1=t2previous;
            }else{
                double realValue = PRNG.nextDouble();
                t1= Utilities.obtainValueFromListAndRealValue(t1Possibles,realValue);
            }

            if (t2Possibles.contains(t1previous) && t2Possibles.contains(t2previous)){
                double p = PRNG.nextDouble();
                if (p < 0.5){
                    t2=t1previous;
                }else{
                    t2=t2previous;
                }
            }else if (t2Possibles.contains(t1previous)){
                t2=t1previous;
            }else if (t2Possibles.contains(t2previous)){
                t2=t2previous;
            }else{
                double realValue = PRNG.nextDouble();
                t2=Utilities.obtainValueFromListAndRealValue(t2Possibles,realValue);
            }

            //Choose the satellite object for the two childs
            Satellite sat1previous = var1.getSatellite();
            Satellite sat2previous = var2.getSatellite();
            Satellite sat1;
            Satellite sat2;
            double p = PRNG.nextDouble();
            if (p < 0.5){
                sat1=sat1previous;
            }else{
                sat1=sat2previous;
            }
            p = PRNG.nextDouble();
            if (p < 0.5){
                sat2=sat1previous;
            }else{
                sat2=sat2previous;
            }

            //Retrieve fReal from the two childs
            double f1Previous = var1.getfReal();
            double f2Previous = var2.getfReal();
            RealVariable f1 = new RealVariable(f1Previous,0,1);
            RealVariable f2 = new RealVariable(f2Previous,0,1);
            SBX.evolve(f1,f2,20);

            //sets the new planes, number of satellites and satellite object of the childs
            constel1.setPlaneVariablesAndTAndSatellite(pList1,t1, f1.getValue(), sat1);
            constel2.setPlaneVariablesAndTAndSatellite(pList2,t2, f2.getValue(), sat2);
        }
        return new HeterogeneousWalkerVariable[]{constel1, constel2};
    }

    @Override
    public int getArity() {
        return 2;
    }

}

