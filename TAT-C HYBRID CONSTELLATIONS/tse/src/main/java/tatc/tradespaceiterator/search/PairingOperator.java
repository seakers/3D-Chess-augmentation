package tatc.tradespaceiterator.search;

import org.hipparchus.util.FastMath;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.operator.real.SBX;
import org.moeaframework.core.variable.RealVariable;
import tatc.architecture.specifications.Satellite;
import tatc.architecture.variable.HeterogeneousWalkerVariable;
import tatc.architecture.variable.IntegerVariable;
import tatc.architecture.variable.PlaneVariable;
import tatc.util.Enumeration;
import tatc.util.Utilities;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This "crossover" operator is intended for a variable length chromosome in which the
 * number of planes (p) in a heterogeneous constellation are allowed to vary and a gene is
 * defined as a the total number of satellites (ns) in the constellation and p plane variables
 * with their associated pair of altitude and inclination. Each plane has ns/p satellites.
 * A number of planes equal to the number of planes in the smallest constellations will be
 * grouped randomly with satellites from the other constellations and evolved picking any of the
 * two altitudes and inclinations randomly. The number of satellites for the offspring is selected
 * by giving priority to the number of satellites from the parents if possible or selecting a random
 * number from the allowed values in the tsr.json otherwise). Finally selects the satellite object with
 * 0.5 probability from the two parents.
 */
public class PairingOperator implements Variation, HeterogeneousWalkerVariation {

    /**
     * The probability of applying this operator to solutions.
     */
    private final double probability;


    /**
     * Constructs a variable chromosome length pairing operator with
     * the specified probability of applying this operator to solutions.
     *
     * @param probability the probability of applying this operator to solutions
     */
    public PairingOperator(double probability) {
        this.probability = probability;
    }

    @Override
    public int getArity() {
        return 2;
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

                    HeterogeneousWalkerVariable[] newVars = this.evolve(constels, -1);

                    result1.setVariable(i, newVars[0]);
                    result2.setVariable(i, newVars[1]);
                }
            }
        }

        return new Solution[]{result1, result2};
    }


    public HeterogeneousWalkerVariable[] evolve(HeterogeneousWalkerVariable[] constellations, int numberConstellations) {

        HeterogeneousWalkerVariable constellation1 = (HeterogeneousWalkerVariable) constellations[0].copy();
        HeterogeneousWalkerVariable constellation2 = (HeterogeneousWalkerVariable) constellations[1].copy();
        //find the minimum number of planes contained in any of the constellations and save the ts of the parents
        int minNPlanes = Integer.MAX_VALUE;
        minNPlanes = FastMath.min(minNPlanes, constellation1.getNumberOfPlanes());
        minNPlanes = FastMath.min(minNPlanes, constellation2.getNumberOfPlanes());
        int t1previous = constellation1.getT();
        int t2previous = constellation2.getT();

        //create a 2-D array of the 2 parent constellation variables involved
        PlaneVariable[][] planesToCross = new PlaneVariable[2][minNPlanes];
        int[][] planesToCrossIndex = new int[2][minNPlanes];
        ArrayList<PlaneVariable> candidates1 = new ArrayList<>(constellation1.getPlaneVariables());
        List<Integer> indexes1 = IntStream.rangeClosed(0, candidates1.size()-1).boxed().collect(Collectors.toList());
        Collections.shuffle(indexes1);
        for (int j = 0; j < minNPlanes; j++) {
            int index = indexes1.get(j);
            planesToCross[0][j] = candidates1.get(index);
            planesToCrossIndex[0][j] = index;
        }
        ArrayList<PlaneVariable> candidates2 = new ArrayList<>(constellation2.getPlaneVariables());
        List<Integer> indexes2 = IntStream.rangeClosed(0, candidates2.size()-1).boxed().collect(Collectors.toList());
        Collections.shuffle(indexes2);
        for (int j = 0; j < minNPlanes; j++) {
            int index = indexes2.get(j);
            planesToCross[1][j] = candidates2.get(index);
            planesToCrossIndex[1][j] = index;
        }

        //create the vector representation of the constellation
        Solution[] parents = new Solution[2];
        for (int i = 0; i < parents.length; i++) {
            Solution parent = new Solution(2 * minNPlanes, 0);
            int planeCount = 0;
            for (int j = 0; j < minNPlanes; j++) {
                PlaneVariable plane = planesToCross[i][j];
                //TODO: maybe instead:
                //parent.setVariable(planeCount, new IntegerVariable(plane.getAltAllowed().indexOf(plane.getAlt()), plane.getAltAllowed().indexOf(plane.getAlt()), plane.getAltAllowed().indexOf(plane.getAlt())));
                //parent.setVariable(planeCount + 1, new IntegerVariable(plane.getIncAllowed().indexOf(plane.getInc()), plane.getIncAllowed().indexOf(plane.getInc()), plane.getIncAllowed().indexOf(plane.getInc())));
                parent.setVariable(planeCount, new IntegerVariable(plane.getAltAllowed().indexOf(plane.getAlt()), 0, plane.getAltAllowed().size()-1));
                parent.setVariable(planeCount + 1, new IntegerVariable(plane.getIncAllowed().indexOf(plane.getInc()), 0, plane.getIncAllowed().size()-1));
                planeCount += 2;
            }
            parents[i] = parent;
        }
        Solution[] children = evolve(parents[0],parents[1]);

        HeterogeneousWalkerVariable out1 = constellation1;
        HeterogeneousWalkerVariable out2 = constellation2;

        ArrayList<PlaneVariable> planeList1 = new ArrayList<>(constellation1.getPlaneVariables());
        int planeCount = 0;
        Solution child1 = children[0];
        for (int j = 0; j < minNPlanes; j++) {
            PlaneVariable plane = planesToCross[0][j];
            Integer indexAlt = ((IntegerVariable) child1.getVariable(planeCount + 0)).getValue();
            plane.setAlt(plane.getAltAllowed().get(indexAlt));
            Integer indexInc = ((IntegerVariable) child1.getVariable(planeCount + 1)).getValue();
            plane.setInc(plane.getIncAllowed().get(indexInc));

            planeList1.set(planesToCrossIndex[0][j], plane);
            planeCount += 2;
        }

        ArrayList<PlaneVariable> planeList2 = new ArrayList<>(constellation2.getPlaneVariables());
        planeCount = 0;
        Solution child2 = children[1];
        for (int j = 0; j < minNPlanes; j++) {
            PlaneVariable plane = planesToCross[1][j];
            Integer indexAlt = ((IntegerVariable) child2.getVariable(planeCount + 0)).getValue();
            plane.setAlt(plane.getAltAllowed().get(indexAlt));
            Integer indexInc = ((IntegerVariable) child2.getVariable(planeCount + 1)).getValue();
            plane.setInc(plane.getIncAllowed().get(indexInc));

            planeList2.set(planesToCrossIndex[1][j], plane);
            planeCount += 2;
        }

        int t1;
        int t2;
        ArrayList<Integer> t1Possibles = Enumeration.allowedNumberOfSatellites(planeList1.size(),constellation1.gettAllowed());
        ArrayList<Integer> t2Possibles = Enumeration.allowedNumberOfSatellites(planeList2.size(),constellation1.gettAllowed());
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


        Satellite sat1previous = constellation1.getSatellite();
        Satellite sat2previous = constellation2.getSatellite();
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
        double f1Previous = constellation1.getfReal();
        double f2Previous = constellation2.getfReal();
        RealVariable f1 = new RealVariable(f1Previous,0,1);
        RealVariable f2 = new RealVariable(f2Previous,0,1);
        SBX.evolve(f1,f2,20);

        out1.setPlaneVariablesAndTAndSatellite(planeList1,t1, f1.getValue(), sat1);
        out2.setPlaneVariablesAndTAndSatellite(planeList2,t2, f2.getValue(), sat2);

        return new HeterogeneousWalkerVariable[]{out1, out2};
    }

    /**
     * Crosses the grouped planes from the two constellations picking any of the two altitudes and inclinations randomly
     * @param planes1 plane from first constellation
     * @param planes2 plane from second constellation
     * @return the evolved planes
     */
    private Solution[] evolve(Solution planes1, Solution planes2){
        Solution out1 = planes1.copy();
        Solution out2 = planes2.copy();
        for (int i=0; i<planes1.getNumberOfVariables(); i=i+2){
            IntegerVariable alt1 = (IntegerVariable)planes1.getVariable(i);
            IntegerVariable inc1 = (IntegerVariable)planes1.getVariable(i+1);
            IntegerVariable alt2 = (IntegerVariable)planes2.getVariable(i);
            IntegerVariable inc2 = (IntegerVariable)planes2.getVariable(i+1);

            IntegerVariable out1_alt;
            IntegerVariable out1_inc;
            IntegerVariable out2_alt;
            IntegerVariable out2_inc;
            double p = PRNG.nextDouble();
            if (p < 0.5){
                out1_alt=alt1;
            }else{
                out1_alt=alt2;
            }
            p = PRNG.nextDouble();
            if (p < 0.5){
                out1_inc=inc1;
            }else{
                out1_inc=inc2;
            }
            p = PRNG.nextDouble();
            if (p < 0.5){
                out2_alt=alt1;
            }else{
                out2_alt=alt2;
            }
            p = PRNG.nextDouble();
            if (p < 0.5){
                out2_inc=inc1;
            }else{
                out2_inc=inc2;
            }
            out1.setVariable(i,out1_alt);
            out1.setVariable(i+1,out1_inc);
            out2.setVariable(i,out2_alt);
            out2.setVariable(i+1,out2_inc);
        }
        return new Solution[]{out1, out2};
    }

}

