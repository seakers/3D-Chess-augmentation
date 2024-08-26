package tatc.architecture.specifications;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A compound mission objective
 */
public class CompoundObjective implements Serializable {
    /**
     * Parent objective
     */
    private MissionObjective parent;
    /**
     * Child objectives
     */
    private List<MissionObjective> childs;

    /**
     * Constructs a compound objective. No childs initialized.
     * @param parent the parent objective
     */
    public CompoundObjective(MissionObjective parent) {
        this.parent = parent;
        this.childs = new ArrayList<>();
    }

    /**
     * Gets the objective parent
     * @return the objective parent
     */
    public MissionObjective getParent() {
        return parent;
    }

    /**
     * Gets the child objectives
     * @return the child objetives
     */
    public List<MissionObjective> getChilds() {
        return childs;
    }

    /**
     * Adds a child to this compound objective
     * @param obj
     */
    public void addChild (MissionObjective obj){
        this.getChilds().add(obj);
    }

    /**
     * Computes the value of this compound objective
     * @param archCounter the architecture counter
     * @return the value of this compound objective
     */
    public double getObjectiveValue(int archCounter){
        if (childs.size()>0){
            double[] objectives = new double[childs.size()];
            double[] weights = new double[childs.size()];
            int objectiveCounter = 0;
            for (MissionObjective obj : this.getChilds()){
                objectives[objectiveCounter] = obj.getObjectiveValueNormalized(archCounter);
                weights[objectiveCounter] = obj.getWeight();
                objectiveCounter++;
            }
            double compoundObjectiveValue = 0;
            double sumWeights = Arrays.stream(weights).sum();
            for (int i=0; i<objectives.length; i++){
                compoundObjectiveValue = compoundObjectiveValue + (weights[i]/sumWeights)*objectives[i];
            }
            return compoundObjectiveValue;
        }else{
            return parent.getObjectiveValue(archCounter);
        }
    }
}
