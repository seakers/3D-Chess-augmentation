package tatc.tradespaceiterator.search;

import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.operator.binary.HUX;
import org.moeaframework.core.variable.BinaryVariable;
import tatc.architecture.specifications.Satellite;
import tatc.architecture.variable.*;

/**
 * This "crossover" operator is intended for a hybrid constellation chromosome containing a GroundNetworkVariable
 * and 1 or more constellation variables (including HeterogeneousWalkerVariable, HomogeneousWalkerVariable, TrainVariable or
 * AdHocVariable). The way this operator works is by looping around the different variables of the parents and perform
 * the given fixedLengthOperatorIntegerVariables crossover operator (such as TwoPointCrossover) for all constellation variable types except
 * for heterogeneous walker variables (which are variable length chromosomes). For heterogeneous Walker variables we use
 * either a cut and splice operator or a pairing operator (if both are provided, one of them is chosen randomly).
 */
public class HybridOperatorCrossover implements Variation {

    /**
     * Fixed length crossover operator used for integer variables in HomogeneousWalkerVariable, TrainVariable and AdHocVariable (e.g. TwoPointCrossover)
     */
    private final Variation fixedLengthOperatorIntegerVariables;

    /**
     * Fixed length crossover operator used for continuous variables (such as p and f) in HomogeneousWalkerVariable and HeterogeneousWalkerVariable (e.g. SBX)
     */
    private final Variation fixedLengthOperatorContinuousVariables;

    /**
     * Variable length crossover operator(s) used for HeterogeneousWalkerVariable (CutAndSpliceOperator or PairingOperator)
     */
    private final HeterogeneousWalkerVariation[] heterogeneousWalkerOperator;

    /**
     * Constructs a hybrid crossover operator
     * @param fixedLengthOperatorIntegerVariables the fixed length crossover operator for integer variables
     * @param heterogeneousWalkerOperator the variable length crossover operator(s)
     */
    public HybridOperatorCrossover(Variation fixedLengthOperatorIntegerVariables,
                                   Variation fixedLengthOperatorContinuousVariables,
                                   HeterogeneousWalkerVariation[] heterogeneousWalkerOperator) {
        this.fixedLengthOperatorIntegerVariables = fixedLengthOperatorIntegerVariables;
        this.fixedLengthOperatorContinuousVariables = fixedLengthOperatorContinuousVariables;
        this.heterogeneousWalkerOperator = heterogeneousWalkerOperator;
    }

    @Override
    @SuppressWarnings("Duplicates")
    public Solution[] evolve(Solution[] parents) {
        Solution result1 = parents[0].copy();
        Solution result2 = parents[1].copy();

        for (int i = 0; i < result1.getNumberOfVariables(); i++) {
            if (result1.getVariable(i) instanceof GroundNetworkVariable && result2.getVariable(i) instanceof GroundNetworkVariable) {

                GroundNetworkVariable groundNetwork1 = (GroundNetworkVariable) result1.getVariable(i);
                GroundNetworkVariable groundNetwork2 = (GroundNetworkVariable) result2.getVariable(i);

                if ((groundNetwork1.getGroundNetwork().isMutable() && groundNetwork2.getGroundNetwork().isMutable()) &&
                        (groundNetwork1.getGroundNetwork().getId() == groundNetwork2.getGroundNetwork().getId())){
                    if ((groundNetwork1.getGroundNetwork().getAgency()==null) ||
                            (groundNetwork2.getGroundNetwork().getAgency()==null) ||
                            (groundNetwork1.getGroundNetwork().getAgency().getAgencyType().equals(groundNetwork2.getGroundNetwork().getAgency().getAgencyType()))){

                        String agencyType = null;
                        if (groundNetwork1.getGroundNetwork().getAgency() != null){
                            agencyType = groundNetwork1.getGroundNetwork().getAgency().getAgencyType();
                        }


                        BinaryVariable binaryVariable1 = groundNetwork1.groundNetworkToBinaryVariable();
                        BinaryVariable binaryVariable2 = groundNetwork2.groundNetworkToBinaryVariable();

                        HUX.evolve(binaryVariable1,binaryVariable2);

                        groundNetwork1.setGroundNetworkFromBinaryVariable(binaryVariable1,agencyType);
                        groundNetwork2.setGroundNetworkFromBinaryVariable(binaryVariable2,agencyType);

                    }
                }

                result1.setVariable(i,groundNetwork1);
                result2.setVariable(i,groundNetwork2);

            }else if (result1.getVariable(i) instanceof HeterogeneousWalkerVariable && result2.getVariable(i) instanceof HeterogeneousWalkerVariable) {

                HeterogeneousWalkerVariable constel1 = (HeterogeneousWalkerVariable) result1.getVariable(i);
                HeterogeneousWalkerVariable constel2 = (HeterogeneousWalkerVariable) result2.getVariable(i);
                HeterogeneousWalkerVariable[] constels = {constel1,constel2};
                HeterogeneousWalkerVariable[] newVars;
                if (heterogeneousWalkerOperator.length!=1){
                    double p = PRNG.nextDouble();
                    if (p < 0.5){
                        newVars=heterogeneousWalkerOperator[0].evolve(constels,-1);
                    }else{
                        newVars=heterogeneousWalkerOperator[1].evolve(constels,-1);
                    }
                }else {
                    newVars = heterogeneousWalkerOperator[0].evolve(constels,-1);
                }

                result1.setVariable(i, newVars[0]);
                result2.setVariable(i, newVars[1]);

            } else if (result1.getVariable(i) instanceof HomogeneousWalkerVariable && result2.getVariable(i) instanceof HomogeneousWalkerVariable) {

                HomogeneousWalkerVariable constel1 = (HomogeneousWalkerVariable) result1.getVariable(i);
                HomogeneousWalkerVariable constel2 = (HomogeneousWalkerVariable) result2.getVariable(i);
                Solution sol1Integer = new Solution(4,0);
                Solution sol2Integer = new Solution(4,0);
                Solution sol1Continuous = new Solution(2,0);
                Solution sol2Continuous = new Solution(2,0);

                sol1Integer.setVariable(0, new IntegerVariable(constel1.getAltAllowed().indexOf(constel1.getAlt()), 0, constel1.getAltAllowed().size() - 1));
                sol1Integer.setVariable(1, new IntegerVariable(constel1.getIncAllowed().indexOf(constel1.getInc()), 0, constel1.getIncAllowed().size() - 1));
                sol1Integer.setVariable(2, new IntegerVariable(constel1.gettAllowed().indexOf(constel1.getT()), 0, constel1.gettAllowed().size() - 1));
                sol1Integer.setVariable(3, new IntegerVariable(constel1.getSatAllowed().indexOf(constel1.getSatellite()), 0, constel1.getSatAllowed().size() - 1));

                sol1Continuous.setVariable(0, new RealVariable(constel1.getpReal(),0, 1)); //planes
                sol1Continuous.setVariable(1, new RealVariable(constel1.getfReal(),0, 1)); //phasing

                sol2Integer.setVariable(0, new IntegerVariable(constel2.getAltAllowed().indexOf(constel2.getAlt()), 0, constel2.getAltAllowed().size() - 1));
                sol2Integer.setVariable(1, new IntegerVariable(constel2.getIncAllowed().indexOf(constel2.getInc()), 0, constel2.getIncAllowed().size() - 1));
                sol2Integer.setVariable(2, new IntegerVariable(constel2.gettAllowed().indexOf(constel2.getT()), 0, constel2.gettAllowed().size() - 1));
                sol2Integer.setVariable(3, new IntegerVariable(constel2.getSatAllowed().indexOf(constel2.getSatellite()), 0, constel2.getSatAllowed().size() - 1));

                sol2Continuous.setVariable(0, new RealVariable(constel2.getpReal(),0, 1)); //planes
                sol2Continuous.setVariable(1, new RealVariable(constel2.getfReal(),0, 1)); //phasing

                Solution[] constelsInteger = {sol1Integer,sol2Integer};
                Solution[] newVarsInteger = fixedLengthOperatorIntegerVariables.evolve(constelsInteger);
                Solution[] constelsContinuous = {sol1Continuous,sol2Continuous};
                Solution[] newVarsContinuous = fixedLengthOperatorContinuousVariables.evolve(constelsContinuous);

                int indexAlt = ((IntegerVariable)newVarsInteger[0].getVariable(0)).getValue();
                double newAlt =  constel1.getAltAllowed().get(indexAlt);
                ((HomogeneousWalkerVariable) result1.getVariable(i)).setAlt(newAlt);
                int indexInc = ((IntegerVariable)newVarsInteger[0].getVariable(1)).getValue();
                Object newInc =  constel1.getIncAllowed().get(indexInc);
                ((HomogeneousWalkerVariable) result1.getVariable(i)).setInc(newInc);
                int indexT = ((IntegerVariable)newVarsInteger[0].getVariable(2)).getValue();
                Integer newT =  constel1.gettAllowed().get(indexT);
                ((HomogeneousWalkerVariable) result1.getVariable(i)).setT(newT);
                ((HomogeneousWalkerVariable) result1.getVariable(i)).setpReal(((RealVariable)newVarsContinuous[0].getVariable(0)).getValue());
                ((HomogeneousWalkerVariable) result1.getVariable(i)).setfReal(((RealVariable)newVarsContinuous[0].getVariable(1)).getValue());
                int indexSat = ((IntegerVariable)newVarsInteger[0].getVariable(3)).getValue();
                Satellite newSat =  constel1.getSatAllowed().get(indexSat);
                ((HomogeneousWalkerVariable) result1.getVariable(i)).setSatellite(newSat);

                indexAlt = ((IntegerVariable)newVarsInteger[1].getVariable(0)).getValue();
                newAlt =  constel2.getAltAllowed().get(indexAlt);
                ((HomogeneousWalkerVariable) result2.getVariable(i)).setAlt(newAlt);
                indexInc = ((IntegerVariable)newVarsInteger[1].getVariable(1)).getValue();
                newInc =  constel2.getIncAllowed().get(indexInc);
                ((HomogeneousWalkerVariable) result2.getVariable(i)).setInc(newInc);
                indexT = ((IntegerVariable)newVarsInteger[1].getVariable(2)).getValue();
                newT =  constel2.gettAllowed().get(indexT);
                ((HomogeneousWalkerVariable) result2.getVariable(i)).setT(newT);
                ((HomogeneousWalkerVariable) result2.getVariable(i)).setpReal(((RealVariable)newVarsContinuous[1].getVariable(0)).getValue());
                ((HomogeneousWalkerVariable) result2.getVariable(i)).setfReal(((RealVariable)newVarsContinuous[1].getVariable(1)).getValue());
                indexSat = ((IntegerVariable)newVarsInteger[1].getVariable(3)).getValue();
                newSat =  constel2.getSatAllowed().get(indexSat);
                ((HomogeneousWalkerVariable) result2.getVariable(i)).setSatellite(newSat);


            }else if (result1.getVariable(i) instanceof TrainVariable && result2.getVariable(i) instanceof TrainVariable){

                TrainVariable constel1 = (TrainVariable) result1.getVariable(i);
                TrainVariable constel2 = (TrainVariable) result2.getVariable(i);
                Solution sol1 = new Solution(5,0);
                Solution sol2 = new Solution(5,0);
                sol1.setVariable(0, new IntegerVariable(constel1.getAltAllowed().indexOf(constel1.getAlt()), 0, constel1.getAltAllowed().size() - 1));
                sol1.setVariable(1, new IntegerVariable(constel1.gettAllowed().indexOf(constel1.getT()), 0, constel1.gettAllowed().size() - 1));
                sol1.setVariable(2, new IntegerVariable(constel1.getLtanAllowed().indexOf(constel1.getLTAN()), 0, constel1.getLtanAllowed().size() - 1));
                sol1.setVariable(3, new IntegerVariable(constel1.getSatIntervalsAllowed().indexOf(constel1.getSatInterval()), 0, constel1.getSatIntervalsAllowed().size() - 1));
                sol1.setVariable(4, new IntegerVariable(constel1.getSatAllowed().indexOf(constel1.getSatellite()), 0, constel1.getSatAllowed().size() - 1));

                sol2.setVariable(0, new IntegerVariable(constel2.getAltAllowed().indexOf(constel2.getAlt()), 0, constel2.getAltAllowed().size() - 1));
                sol2.setVariable(1, new IntegerVariable(constel2.gettAllowed().indexOf(constel2.getT()), 0, constel2.gettAllowed().size() - 1));
                sol2.setVariable(2, new IntegerVariable(constel2.getLtanAllowed().indexOf(constel2.getLTAN()), 0, constel2.getLtanAllowed().size() - 1));
                sol2.setVariable(3, new IntegerVariable(constel2.getSatIntervalsAllowed().indexOf(constel2.getSatInterval()), 0, constel2.getSatIntervalsAllowed().size() - 1));
                sol2.setVariable(4, new IntegerVariable(constel2.getSatAllowed().indexOf(constel2.getSatellite()), 0, constel2.getSatAllowed().size() - 1));

                Solution[] constels = {sol1,sol2};
                Solution[] newVars = fixedLengthOperatorIntegerVariables.evolve(constels);

                int indexAlt = ((IntegerVariable)newVars[0].getVariable(0)).getValue();
                double newAlt =  constel1.getAltAllowed().get(indexAlt);
                ((TrainVariable) result1.getVariable(i)).setAlt(newAlt);
                int indexT = ((IntegerVariable)newVars[0].getVariable(1)).getValue();
                Integer newT =  constel1.gettAllowed().get(indexT);
                ((TrainVariable) result1.getVariable(i)).setT(newT);
                int indexLTAN = ((IntegerVariable)newVars[0].getVariable(2)).getValue();
                String newLTAN =  constel1.getLtanAllowed().get(indexLTAN);
                ((TrainVariable) result1.getVariable(i)).setLTAN(newLTAN);
                int indexSatInterval = ((IntegerVariable)newVars[0].getVariable(3)).getValue();
                String newSatInterval =  constel1.getSatIntervalsAllowed().get(indexSatInterval);
                ((TrainVariable) result1.getVariable(i)).setSatInterval(newSatInterval);
                int indexSat = ((IntegerVariable)newVars[0].getVariable(4)).getValue();
                Satellite newSat =  constel1.getSatAllowed().get(indexSat);
                ((TrainVariable) result1.getVariable(i)).setSatellite(newSat);

                indexAlt = ((IntegerVariable)newVars[1].getVariable(0)).getValue();
                newAlt =  constel2.getAltAllowed().get(indexAlt);
                ((TrainVariable) result2.getVariable(i)).setAlt(newAlt);
                indexT = ((IntegerVariable)newVars[1].getVariable(1)).getValue();
                newT =  constel2.gettAllowed().get(indexT);
                ((TrainVariable) result2.getVariable(i)).setT(newT);
                indexLTAN = ((IntegerVariable)newVars[1].getVariable(2)).getValue();
                newLTAN =  constel2.getLtanAllowed().get(indexLTAN);
                ((TrainVariable) result2.getVariable(i)).setLTAN(newLTAN);
                indexSatInterval = ((IntegerVariable)newVars[1].getVariable(3)).getValue();
                newSatInterval =  constel2.getSatIntervalsAllowed().get(indexSatInterval);
                ((TrainVariable) result2.getVariable(i)).setSatInterval(newSatInterval);
                indexSat = ((IntegerVariable)newVars[1].getVariable(4)).getValue();
                newSat =  constel2.getSatAllowed().get(indexSat);
                ((TrainVariable) result2.getVariable(i)).setSatellite(newSat);

            }else if (result1.getVariable(i) instanceof AdHocVariable && result2.getVariable(i) instanceof AdHocVariable){

                AdHocVariable constel1 = (AdHocVariable) result1.getVariable(i);
                AdHocVariable constel2 = (AdHocVariable) result2.getVariable(i);
                Solution sol1 = new Solution(2,0);
                Solution sol2 = new Solution(2,0);
                sol1.setVariable(0, new IntegerVariable(constel1.gettAllowed().indexOf(constel1.getT()), 0, constel1.gettAllowed().size() - 1));
                sol1.setVariable(1, new IntegerVariable(constel1.getSatAllowed().indexOf(constel1.getSatellite()), 0, constel1.getSatAllowed().size() - 1));

                sol2.setVariable(0, new IntegerVariable(constel2.gettAllowed().indexOf(constel2.getT()), 0, constel2.gettAllowed().size() - 1));
                sol2.setVariable(1, new IntegerVariable(constel2.getSatAllowed().indexOf(constel2.getSatellite()), 0, constel2.getSatAllowed().size() - 1));

                Solution[] constels = {sol1,sol2};
                Solution[] newVars = fixedLengthOperatorIntegerVariables.evolve(constels);


                int indexT = ((IntegerVariable)newVars[0].getVariable(0)).getValue();
                Integer newT =  constel1.gettAllowed().get(indexT);
                ((AdHocVariable) result1.getVariable(i)).setT(newT);
                int indexSat = ((IntegerVariable)newVars[0].getVariable(1)).getValue();
                Satellite newSat =  constel1.getSatAllowed().get(indexSat);
                ((AdHocVariable) result1.getVariable(i)).setSatellite(newSat);

                indexT = ((IntegerVariable)newVars[1].getVariable(0)).getValue();
                newT =  constel2.gettAllowed().get(indexT);
                ((AdHocVariable) result2.getVariable(i)).setT(newT);
                indexSat = ((IntegerVariable)newVars[1].getVariable(1)).getValue();
                newSat =  constel2.getSatAllowed().get(indexSat);
                ((AdHocVariable) result2.getVariable(i)).setSatellite(newSat);

            } else if (result1.getVariable(i) instanceof StringOfPearlsVariable && result2.getVariable(i) instanceof StringOfPearlsVariable) {
                StringOfPearlsVariable constel1 = (StringOfPearlsVariable) result1.getVariable(i);
                StringOfPearlsVariable constel2 = (StringOfPearlsVariable) result2.getVariable(i);
                Solution sol1 = new Solution(6,0);
                Solution sol2 = new Solution(6,0);
                sol1.setVariable(0, new IntegerVariable(constel1.getAltAllowed().indexOf(constel1.getAlt()), 0, constel1.getAltAllowed().size() - 1));
                sol1.setVariable(1, new IntegerVariable(constel1.getIncAllowed().indexOf(constel1.getInc()), 0, constel1.getIncAllowed().size() - 1));
                sol1.setVariable(2, new IntegerVariable(constel1.gettAllowed().indexOf(constel1.getT()), 0, constel1.gettAllowed().size() - 1));
                sol1.setVariable(3, new IntegerVariable(constel1.getRaanAllowed().indexOf(constel1.getRaan()), 0, constel1.getRaanAllowed().size() - 1));
                sol1.setVariable(4, new IntegerVariable(constel1.getSatIntervalsAllowed().indexOf(constel1.getSatInterval()), 0, constel1.getSatIntervalsAllowed().size() - 1));
                sol1.setVariable(5, new IntegerVariable(constel1.getSatAllowed().indexOf(constel1.getSatellite()), 0, constel1.getSatAllowed().size() - 1));

                sol2.setVariable(0, new IntegerVariable(constel2.getAltAllowed().indexOf(constel2.getAlt()), 0, constel2.getAltAllowed().size() - 1));
                sol2.setVariable(1, new IntegerVariable(constel2.getIncAllowed().indexOf(constel2.getInc()), 0, constel2.getIncAllowed().size() - 1));
                sol2.setVariable(2, new IntegerVariable(constel2.gettAllowed().indexOf(constel2.getT()), 0, constel2.gettAllowed().size() - 1));
                sol2.setVariable(3, new IntegerVariable(constel2.getRaanAllowed().indexOf(constel2.getRaan()), 0, constel2.getRaanAllowed().size() - 1));
                sol2.setVariable(4, new IntegerVariable(constel2.getSatIntervalsAllowed().indexOf(constel2.getSatInterval()), 0, constel2.getSatIntervalsAllowed().size() - 1));
                sol2.setVariable(5, new IntegerVariable(constel2.getSatAllowed().indexOf(constel2.getSatellite()), 0, constel2.getSatAllowed().size() - 1));

                Solution[] constels = {sol1,sol2};
                Solution[] newVars = fixedLengthOperatorIntegerVariables.evolve(constels);

                int indexAlt = ((IntegerVariable)newVars[0].getVariable(0)).getValue();
                double newAlt =  constel1.getAltAllowed().get(indexAlt);
                ((StringOfPearlsVariable) result1.getVariable(i)).setAlt(newAlt);
                int indexInc = ((IntegerVariable)newVars[0].getVariable(1)).getValue();
                Object newInc =  constel1.getIncAllowed().get(indexInc);
                ((StringOfPearlsVariable) result1.getVariable(i)).setInc(newInc);
                int indexT = ((IntegerVariable)newVars[0].getVariable(2)).getValue();
                Integer newT =  constel1.gettAllowed().get(indexT);
                ((StringOfPearlsVariable) result1.getVariable(i)).setT(newT);
                int indexRaan = ((IntegerVariable)newVars[0].getVariable(3)).getValue();
                double newRaan =  constel1.getRaanAllowed().get(indexRaan);
                ((StringOfPearlsVariable) result1.getVariable(i)).setRaan(newRaan);
                int indexSatInterval = ((IntegerVariable)newVars[0].getVariable(4)).getValue();
                String newSatInterval =  constel1.getSatIntervalsAllowed().get(indexSatInterval);
                ((StringOfPearlsVariable) result1.getVariable(i)).setSatInterval(newSatInterval);
                int indexSat = ((IntegerVariable)newVars[0].getVariable(5)).getValue();
                Satellite newSat =  constel1.getSatAllowed().get(indexSat);
                ((StringOfPearlsVariable) result1.getVariable(i)).setSatellite(newSat);

                indexAlt = ((IntegerVariable)newVars[1].getVariable(0)).getValue();
                newAlt =  constel2.getAltAllowed().get(indexAlt);
                ((StringOfPearlsVariable) result2.getVariable(i)).setAlt(newAlt);
                indexInc = ((IntegerVariable)newVars[1].getVariable(1)).getValue();
                newInc =  constel2.getIncAllowed().get(indexInc);
                ((StringOfPearlsVariable) result2.getVariable(i)).setInc(newInc);
                indexT = ((IntegerVariable)newVars[1].getVariable(2)).getValue();
                newT =  constel2.gettAllowed().get(indexT);
                ((StringOfPearlsVariable) result2.getVariable(i)).setT(newT);
                indexRaan = ((IntegerVariable)newVars[1].getVariable(3)).getValue();
                newRaan =  constel2.getRaanAllowed().get(indexRaan);
                ((StringOfPearlsVariable) result2.getVariable(i)).setRaan(newRaan);
                indexSatInterval = ((IntegerVariable)newVars[1].getVariable(4)).getValue();
                newSatInterval =  constel2.getSatIntervalsAllowed().get(indexSatInterval);
                ((StringOfPearlsVariable) result2.getVariable(i)).setSatInterval(newSatInterval);
                indexSat = ((IntegerVariable)newVars[1].getVariable(5)).getValue();
                newSat =  constel2.getSatAllowed().get(indexSat);
                ((StringOfPearlsVariable) result2.getVariable(i)).setSatellite(newSat);

            }else{
                //TODO: complete
            }
        }


        return new Solution[]{result1, result2};
    }

    @Override
    public int getArity() {
        if (heterogeneousWalkerOperator.length == 1){
            if (fixedLengthOperatorIntegerVariables.getArity() == heterogeneousWalkerOperator[0].getArity()){
                return fixedLengthOperatorIntegerVariables.getArity();
            }
        }else {
            if ((fixedLengthOperatorIntegerVariables.getArity() == heterogeneousWalkerOperator[0].getArity()) && (fixedLengthOperatorIntegerVariables.getArity() == heterogeneousWalkerOperator[1].getArity())){
                return fixedLengthOperatorIntegerVariables.getArity();
            }
        }
        return -1;
    }

}

