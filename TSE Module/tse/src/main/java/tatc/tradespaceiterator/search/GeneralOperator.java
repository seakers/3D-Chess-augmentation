package tatc.tradespaceiterator.search;

import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.operator.binary.HUX;
import org.moeaframework.core.variable.BinaryVariable;
import seakers.conmop.variable.ConstellationVariable;
import tatc.architecture.variable.*;

/**
 * This "crossover" operator is intended for a hybrid constellation chromosome containing a GroundNetworkVariable
 * and 1 or more constellation variables (including HeterogeneousWalkerVariable, HomogeneousWalkerVariable, TrainVariable or
 * AdHocVariable). The way this operator works is by looping around the different variables of the parents and perform
 * the given fixedLengthOperator crossover operator (such as TwoPointCrossover) for all constellation variable types except
 * for heterogeneous walker variables (which are variable length chromosomes). For heterogeneous Walker variables we use
 * either a cut and splice operator or a pairing operator (if both are provided, one of them is chosen randomly).
 */
public class GeneralOperator implements Variation {

    /**
     */
    private final Variation[] operatorList;

    /**
     *
     * @param operatorList
     */
    public GeneralOperator(Variation[] operatorList) {
        this.operatorList = operatorList;
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

            }else if (result1.getVariable(i) instanceof ConstellationVariable && result2.getVariable(i) instanceof ConstellationVariable) {

                Solution[] constels = {result1,result2};
                Solution[] newVars;
                if (operatorList.length!=1){
                    double p = PRNG.nextDouble();
                    if (p < 0.5){
                        newVars=operatorList[0].evolve(constels);
                    }else{
                        newVars=operatorList[1].evolve(constels);
                    }
                }else {
                    newVars = operatorList[0].evolve(constels);
                }

                result1=newVars[0];
                result2=newVars[1];

            }else{
                //TODO: complete
            }
        }


        return new Solution[]{result1, result2};
    }

    @Override
    public int getArity() {
        if (operatorList.length == 1){
            return operatorList[0].getArity();
        }else {
            if ((operatorList[0].getArity() == operatorList[1].getArity())){
                return operatorList[0].getArity();
            }
        }
        return -1;
    }

}

