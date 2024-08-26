package tatc.tradespaceiterator.search;

import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.operator.binary.BitFlip;
import org.moeaframework.core.operator.real.PM;
import org.moeaframework.core.operator.real.SBX;
import org.moeaframework.core.variable.BinaryVariable;
import seakers.conmop.operators.VariablePM;
import tatc.architecture.specifications.Satellite;
import tatc.architecture.variable.*;

/**
 * This "mutation" operator is intended for a hybrid constellation chromosome containing a GroundNetworkVariable
 * and 1 or more constellation variables (including HeterogeneousWalkerVariable, HomogeneousWalkerVariable, TrainVariable or
 * AdHocVariable). The way this operator works is by looping around the different variables of the parents and perform
 * the given fixedLengthOperatorIntegerVariables IntegereUM mutation operator for all constellation variable types except for heterogeneous
 * walker variables, where we use a HeterogeneousWalkerMutation operator.
 */
public class HybridOperatorMutation implements Variation {

    /**
     * Fixed length mutation operator used for integer variables in HomogeneousWalkerVariable, TrainVariable and AdHocVariable
     */
    private final Variation fixedLengthOperatorIntegerVariables;

    /**
     * Fixed length mutation operator used for continuous variables (such as p and f) in HomogeneousWalkerVariable and HeterogeneousWalkerVariable (e.g. SBX)
     */
    private final Variation fixedLengthOperatorContinuousVariables;

    /**
     * Variable length mutation operator used for HeterogeneousWalkerVariable
     */
    private final HeterogeneousWalkerMutation heterogeneousWalkerOperator;

    /**
     * Constructs a hybrid mutation operator
     * @param fixedLengthOperatorIntegerVariables the fixed length mutation operator for integer variables
     * @param fixedLengthOperatorContinuousVariables the fixed length mutation operator for continuous variables
     * @param heterogeneousWalkerOperator the variable length mutation operator
     */
    public HybridOperatorMutation(Variation fixedLengthOperatorIntegerVariables, Variation fixedLengthOperatorContinuousVariables,
                                  HeterogeneousWalkerMutation heterogeneousWalkerOperator) {
        this.fixedLengthOperatorIntegerVariables = fixedLengthOperatorIntegerVariables;
        this.fixedLengthOperatorContinuousVariables = fixedLengthOperatorContinuousVariables;
        this.heterogeneousWalkerOperator = heterogeneousWalkerOperator;
    }

    @Override
    @SuppressWarnings("Duplicates")
    public Solution[] evolve(Solution[] parents) {
        Solution result = parents[0].copy();
        int numberConstellations = result.getNumberOfVariables()-1; // Used to compute the probability of mutation we do not count the ground network variable

        for (int i = 0; i < result.getNumberOfVariables(); i++) {
            if (result.getVariable(i) instanceof GroundNetworkVariable) {

                GroundNetworkVariable groundNetwork = (GroundNetworkVariable) result.getVariable(i);

                if (groundNetwork.getGroundNetwork().isMutable()){
                    String agencyType = null;
                    if (groundNetwork.getGroundNetwork().getAgency() != null){
                        agencyType = groundNetwork.getGroundNetwork().getAgency().getAgencyType();
                    }

                    BinaryVariable binaryVariable = groundNetwork.groundNetworkToBinaryVariable();

                    BitFlip.evolve(binaryVariable, 1.0 / binaryVariable.getNumberOfBits());

                    groundNetwork.setGroundNetworkFromBinaryVariable(binaryVariable, agencyType);
                }

                result.setVariable(i,groundNetwork);

            }else if (result.getVariable(i) instanceof HeterogeneousWalkerVariable) {

                HeterogeneousWalkerVariable constel = (HeterogeneousWalkerVariable) result.getVariable(i);
                HeterogeneousWalkerVariable[] constels = {constel};
                HeterogeneousWalkerVariable[] newVars;

                newVars = heterogeneousWalkerOperator.evolve(constels, numberConstellations);


                result.setVariable(i, newVars[0]);

            } else if (result.getVariable(i) instanceof HomogeneousWalkerVariable) {

                HomogeneousWalkerVariable constel = (HomogeneousWalkerVariable) result.getVariable(i);
                Solution sol = new Solution(6,0);

                sol.setVariable(0, new IntegerVariable(constel.getAltAllowed().indexOf(constel.getAlt()), 0, constel.getAltAllowed().size() - 1));
                sol.setVariable(1, new IntegerVariable(constel.getIncAllowed().indexOf(constel.getInc()), 0, constel.getIncAllowed().size() - 1));
                sol.setVariable(2, new IntegerVariable(constel.gettAllowed().indexOf(constel.getT()), 0, constel.gettAllowed().size() - 1));
                sol.setVariable(3, new IntegerVariable(constel.getSatAllowed().indexOf(constel.getSatellite()), 0, constel.getSatAllowed().size() - 1));

                sol.setVariable(4, new RealVariable(constel.getpReal(),0, 1)); //planes
                sol.setVariable(5, new RealVariable(constel.getfReal(),0, 1)); //phasing

                Solution[] constelsInteger = {sol};
                Solution[] newVars;
                if (fixedLengthOperatorIntegerVariables instanceof VariableIntegerUM){
                    newVars = ((VariableIntegerUM)fixedLengthOperatorIntegerVariables).evolve(constelsInteger, 1.0/numberConstellations/6);
                }else{
                    newVars = fixedLengthOperatorIntegerVariables.evolve(constelsInteger);
                }

                Solution[] newVarsFinal;
                if (fixedLengthOperatorIntegerVariables instanceof VariablePM2){
                    newVarsFinal = ((VariablePM2) fixedLengthOperatorContinuousVariables).evolve(newVars, 1.0/numberConstellations/6);
                }else{
                    newVarsFinal = fixedLengthOperatorContinuousVariables.evolve(newVars);
                }

                int indexAlt = ((IntegerVariable)newVarsFinal[0].getVariable(0)).getValue();
                double newAlt =  constel.getAltAllowed().get(indexAlt);
                ((HomogeneousWalkerVariable) result.getVariable(i)).setAlt(newAlt);
                int indexInc = ((IntegerVariable)newVarsFinal[0].getVariable(1)).getValue();
                Object newInc =  constel.getIncAllowed().get(indexInc);
                ((HomogeneousWalkerVariable) result.getVariable(i)).setInc(newInc);
                int indexT = ((IntegerVariable)newVarsFinal[0].getVariable(2)).getValue();
                Integer newT =  constel.gettAllowed().get(indexT);
                ((HomogeneousWalkerVariable) result.getVariable(i)).setT(newT);
                ((HomogeneousWalkerVariable) result.getVariable(i)).setpReal(((RealVariable)newVarsFinal[0].getVariable(4)).getValue());
                ((HomogeneousWalkerVariable) result.getVariable(i)).setfReal(((RealVariable)newVarsFinal[0].getVariable(5)).getValue());
                int indexSat = ((IntegerVariable)newVarsFinal[0].getVariable(3)).getValue();
                Satellite newSat =  constel.getSatAllowed().get(indexSat);
                ((HomogeneousWalkerVariable) result.getVariable(i)).setSatellite(newSat);


            }else if (result.getVariable(i) instanceof TrainVariable){

                TrainVariable constel = (TrainVariable) result.getVariable(i);
                Solution sol = new Solution(5,0);
                sol.setVariable(0, new IntegerVariable(constel.getAltAllowed().indexOf(constel.getAlt()), 0, constel.getAltAllowed().size() - 1));
                sol.setVariable(1, new IntegerVariable(constel.gettAllowed().indexOf(constel.getT()), 0, constel.gettAllowed().size() - 1));
                sol.setVariable(2, new IntegerVariable(constel.getLtanAllowed().indexOf(constel.getLTAN()), 0, constel.getLtanAllowed().size() - 1));
                sol.setVariable(3, new IntegerVariable(constel.getSatIntervalsAllowed().indexOf(constel.getSatInterval()), 0, constel.getSatIntervalsAllowed().size() - 1));
                sol.setVariable(4, new IntegerVariable(constel.getSatAllowed().indexOf(constel.getSatellite()), 0, constel.getSatAllowed().size() - 1));

                Solution[] constels = {sol};
                Solution[] newVars;
                if (fixedLengthOperatorIntegerVariables instanceof VariableIntegerUM){
                    newVars = ((VariableIntegerUM)fixedLengthOperatorIntegerVariables).evolve(constels, 1.0/numberConstellations/5);
                }else{
                    newVars = fixedLengthOperatorIntegerVariables.evolve(constels);
                }


                int indexAlt = ((IntegerVariable)newVars[0].getVariable(0)).getValue();
                double newAlt =  constel.getAltAllowed().get(indexAlt);
                ((TrainVariable) result.getVariable(i)).setAlt(newAlt);
                int indexT = ((IntegerVariable)newVars[0].getVariable(1)).getValue();
                Integer newT =  constel.gettAllowed().get(indexT);
                ((TrainVariable) result.getVariable(i)).setT(newT);
                int indexLTAN = ((IntegerVariable)newVars[0].getVariable(2)).getValue();
                String newLTAN =  constel.getLtanAllowed().get(indexLTAN);
                ((TrainVariable) result.getVariable(i)).setLTAN(newLTAN);
                int indexSatInterval = ((IntegerVariable)newVars[0].getVariable(3)).getValue();
                String newSatInterval =  constel.getSatIntervalsAllowed().get(indexSatInterval);
                ((TrainVariable) result.getVariable(i)).setSatInterval(newSatInterval);
                int indexSat = ((IntegerVariable)newVars[0].getVariable(4)).getValue();
                Satellite newSat =  constel.getSatAllowed().get(indexSat);
                ((TrainVariable) result.getVariable(i)).setSatellite(newSat);

            }else if (result.getVariable(i) instanceof AdHocVariable){

                AdHocVariable constel = (AdHocVariable) result.getVariable(i);
                Solution sol = new Solution(2,0);
                sol.setVariable(0, new IntegerVariable(constel.gettAllowed().indexOf(constel.getT()), 0, constel.gettAllowed().size() - 1));
                sol.setVariable(1, new IntegerVariable(constel.getSatAllowed().indexOf(constel.getSatellite()), 0, constel.getSatAllowed().size() - 1));

                Solution[] constels = {sol};
                Solution[] newVars;
                if (fixedLengthOperatorIntegerVariables instanceof VariableIntegerUM){
                    newVars = ((VariableIntegerUM)fixedLengthOperatorIntegerVariables).evolve(constels, 1.0/numberConstellations/2);
                }else{
                    newVars = fixedLengthOperatorIntegerVariables.evolve(constels);
                }

                int indexT = ((IntegerVariable)newVars[0].getVariable(0)).getValue();
                Integer newT =  constel.gettAllowed().get(indexT);
                ((AdHocVariable) result.getVariable(i)).setT(newT);
                int indexSat = ((IntegerVariable)newVars[0].getVariable(1)).getValue();
                Satellite newSat =  constel.getSatAllowed().get(indexSat);
                ((AdHocVariable) result.getVariable(i)).setSatellite(newSat);

            } else if (result.getVariable(i) instanceof StringOfPearlsVariable) {

                StringOfPearlsVariable constel = (StringOfPearlsVariable) result.getVariable(i);
                Solution sol = new Solution(6,0);
                sol.setVariable(0, new IntegerVariable(constel.getAltAllowed().indexOf(constel.getAlt()), 0, constel.getAltAllowed().size() - 1));
                sol.setVariable(1, new IntegerVariable(constel.getIncAllowed().indexOf(constel.getInc()), 0, constel.getIncAllowed().size() - 1));
                sol.setVariable(2, new IntegerVariable(constel.gettAllowed().indexOf(constel.getT()), 0, constel.gettAllowed().size() - 1));
                sol.setVariable(3, new IntegerVariable(constel.getRaanAllowed().indexOf(constel.getRaan()), 0, constel.getRaanAllowed().size() - 1));
                sol.setVariable(4, new IntegerVariable(constel.getSatIntervalsAllowed().indexOf(constel.getSatInterval()), 0, constel.getSatIntervalsAllowed().size() - 1));
                sol.setVariable(5, new IntegerVariable(constel.getSatAllowed().indexOf(constel.getSatellite()), 0, constel.getSatAllowed().size() - 1));

                Solution[] constels = {sol};
                Solution[] newVars;
                if (fixedLengthOperatorIntegerVariables instanceof VariableIntegerUM){
                    newVars = ((VariableIntegerUM)fixedLengthOperatorIntegerVariables).evolve(constels, 1.0/numberConstellations/6);
                }else{
                    newVars = fixedLengthOperatorIntegerVariables.evolve(constels);
                }

                int indexAlt = ((IntegerVariable)newVars[0].getVariable(0)).getValue();
                double newAlt =  constel.getAltAllowed().get(indexAlt);
                ((StringOfPearlsVariable) result.getVariable(i)).setAlt(newAlt);
                int indexInc = ((IntegerVariable)newVars[0].getVariable(1)).getValue();
                Object newInc =  constel.getIncAllowed().get(indexInc);
                ((StringOfPearlsVariable) result.getVariable(i)).setInc(newInc);
                int indexT = ((IntegerVariable)newVars[0].getVariable(2)).getValue();
                Integer newT =  constel.gettAllowed().get(indexT);
                ((StringOfPearlsVariable) result.getVariable(i)).setT(newT);
                int indexRaan = ((IntegerVariable)newVars[0].getVariable(3)).getValue();
                double newRaan =  constel.getRaanAllowed().get(indexRaan);
                ((StringOfPearlsVariable) result.getVariable(i)).setRaan(newRaan);
                int indexSatInterval = ((IntegerVariable)newVars[0].getVariable(4)).getValue();
                String newSatInterval =  constel.getSatIntervalsAllowed().get(indexSatInterval);
                ((StringOfPearlsVariable) result.getVariable(i)).setSatInterval(newSatInterval);
                int indexSat = ((IntegerVariable)newVars[0].getVariable(5)).getValue();
                Satellite newSat =  constel.getSatAllowed().get(indexSat);
                ((StringOfPearlsVariable) result.getVariable(i)).setSatellite(newSat);


            }else{
                //TODO: complete
            }
        }


        return new Solution[]{result};
    }

    @Override
    public int getArity() {
        if (fixedLengthOperatorIntegerVariables.getArity() == heterogeneousWalkerOperator.getArity()){
            return fixedLengthOperatorIntegerVariables.getArity();
        }
        return -1;
    }

}

