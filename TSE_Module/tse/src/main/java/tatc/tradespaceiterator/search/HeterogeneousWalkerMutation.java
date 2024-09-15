package tatc.tradespaceiterator.search;

import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.operator.real.PM;
import org.moeaframework.core.variable.RealVariable;
import seakers.conmop.operators.VariablePM;
import tatc.architecture.specifications.Satellite;
import tatc.architecture.variable.HeterogeneousWalkerVariable;
import tatc.architecture.variable.IntegerVariable;
import tatc.architecture.variable.PlaneVariable;
import tatc.util.Enumeration;
import tatc.util.Utilities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * This "mutation" operator is intended for a variable length chromosome in which the
 * number of planes (p) in a heterogeneous constellation are allowed to vary and a gene is
 * defined as a the total number of satellites (ns) in the constellation and p plane variables
 * with their associated pair of altitude and inclination. Each plane has ns/p satellites.
 * This operator performs mutation on the altitude and inclination decisions of each plane
 * variable and on the satellite object of a heterogeneous constellation.
 */
public class HeterogeneousWalkerMutation implements Variation, HeterogeneousWalkerVariation {

    /**
     * Integer mutation operator such as IntegerUM
     */
    private final Variation mutation;

    /**
     * Constructs a heterogeneous walker mutation operator
     * @param mutation an integer mutation operator such as IntegerUM
     */
    public HeterogeneousWalkerMutation(Variation mutation) {
        this.mutation = mutation;
    }

    @Override
    public int getArity() {
        return 1;
    }

    @Override
    public Solution[] evolve(Solution[] parents) {
        Solution result = parents[0].copy();


        for (int i = 0; i < result.getNumberOfVariables(); i++) {
            if (result.getVariable(i) instanceof HeterogeneousWalkerVariable) {

                HeterogeneousWalkerVariable constel = (HeterogeneousWalkerVariable) result.getVariable(i);
                HeterogeneousWalkerVariable[] constels = {constel};

                HeterogeneousWalkerVariable[] newVars = evolve(constels,1);

                result.setVariable(i, newVars[0]);
            }
        }

        return new Solution[]{result};
    }

    public Solution[] evolve(Solution[] parents, int numberConstellations) {
        Solution result = parents[0].copy();


        for (int i = 0; i < result.getNumberOfVariables(); i++) {
            if (result.getVariable(i) instanceof HeterogeneousWalkerVariable) {

                HeterogeneousWalkerVariable constel = (HeterogeneousWalkerVariable) result.getVariable(i);
                HeterogeneousWalkerVariable[] constels = {constel};

                HeterogeneousWalkerVariable[] newVars = evolve(constels, numberConstellations);

                result.setVariable(i, newVars[0]);
            }
        }

        return new Solution[]{result};
    }



    public HeterogeneousWalkerVariable[] evolve(HeterogeneousWalkerVariable[] constellation, int numberConstellations) {
        HeterogeneousWalkerVariable constel = (HeterogeneousWalkerVariable) constellation[0].copy();
        int numVariables=constel.getNumberOfPlanes()*2 + 1;
        ArrayList<PlaneVariable> planes = new ArrayList<>(constel.getPlaneVariables());
        Solution solution = new Solution(numVariables,0);
        int planeCount = 0;
        for (int j = 0; j < constel.getNumberOfPlanes(); j++) {
            PlaneVariable plane = planes.get(j);
            solution.setVariable(planeCount, new IntegerVariable(plane.getAltAllowed().indexOf(plane.getAlt()), 0, plane.getAltAllowed().size()-1));
            solution.setVariable(planeCount + 1, new IntegerVariable(plane.getIncAllowed().indexOf(plane.getInc()), 0, plane.getIncAllowed().size()-1));
            planeCount += 2;
        }
        //solution.setVariable(, new IntegerVariable(constel.gettAllowed().indexOf(constel.getT()), 0, constel.gettAllowed().size()-1));
        solution.setVariable(planeCount, new IntegerVariable(constel.getSatsAllowed().indexOf(constel.getSatellite()), 0, constel.getSatsAllowed().size()-1));
        Solution[] sol = {solution};

        Solution[] newSol;
        if (mutation instanceof VariableIntegerUM){
            newSol = ((VariableIntegerUM)mutation).evolve(sol, 1.0/numberConstellations/(solution.getNumberOfVariables()+1));
        }else {
            newSol = mutation.evolve(sol);
        }

        double fPrevious = constel.getfReal();
        RealVariable f = new RealVariable(fPrevious,0,1);
        if (PRNG.nextDouble() <= 1.0/numberConstellations/(solution.getNumberOfVariables()+1)){
            VariablePM.evolve(f,20);
        }

        //reading the solution after mutation and creating the new HeterogeneousWalkerVariable
        Solution newSolution = newSol[0];
        ArrayList<PlaneVariable> planeList = new ArrayList<>(constel.getPlaneVariables());
        planeCount = 0;
        for (int j = 0; j < constel.getNumberOfPlanes(); j++) {
            PlaneVariable plane = planeList.get(j);
            Integer indexAlt = ((IntegerVariable) newSolution.getVariable(planeCount + 0)).getValue();
            plane.setAlt(plane.getAltAllowed().get(indexAlt));
            Integer indexInc = ((IntegerVariable) newSolution.getVariable(planeCount + 1)).getValue();
            plane.setInc(plane.getIncAllowed().get(indexInc));

            planeList.set(j, plane);
            planeCount += 2;
        }
        //solution.setVariable(, new IntegerVariable(constel.gettAllowed().indexOf(constel.getT()), 0, constel.gettAllowed().size()-1));
        Integer indexSat = ((IntegerVariable) newSolution.getVariable(planeCount)).getValue();
        Satellite sat = constel.getSatsAllowed().get(indexSat);

        int t = constel.getT();
        constel.setPlaneVariablesAndTAndSatellite(planeList,t,f.getValue(),sat);
        HeterogeneousWalkerVariable[] out = {constel};

        return out;
    }



}

