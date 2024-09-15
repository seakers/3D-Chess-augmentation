package tatc.architecture.specifications;

import com.google.gson.annotations.SerializedName;
import tatc.ResultIO;

import java.io.File;
import java.io.Serializable;

/**
 * A variable to be maximized or minimized to achieve mission objectives. Mission objectives are used as the values
 * to optimize during the intelligent search (i.e. genetic algorithm)
 */
public class MissionObjective implements Serializable {
    /**
     * Type tag for JSON file
     */
    @SerializedName("@type")
    private final String _type="MissionObjective";
    /**
     * Name of the objective. It has to match the ouputs key values from gbl.json, CostRisk_Output.json
     * or level_2_metrics.csv. TODO: Value module objectives need to be added.
     */
    private final String objectiveName;
    /**
     * The parent of this objective
     */
    private final String objectiveParent;
    /**
     * Weight of the objective ranging from 0 to 1. Used to scalarize a set of objectives into a single objective by
     * adding each objective pre-multiplied by a user-supplied weight
     */
    private final Double objectiveWeight;
    /**
     * Objective type including: MAX (maximize), MIN (minimize), TAR (target)
     */
    private final String objectiveType;
    /**
     * Target value for objectives of type TAR
     */
    private final Double objectiveTarget;
    /**
     * The minimum value of this objective used for normalization
     */
    private final Double objectiveMinValue;
    /**
     * The maximum value of this objective used for normalization
     */
    private final Double objectiveMaxValue;

    /**
     * Constructs a mission objective object
     * @param name the objective name
     * @param parent the objective's parent
     * @param weight the objective weight
     * @param type the objective type (MAX, MIN or TAR)
     * @param target the target value for objectives of type TAR
     * @param minValue the minimum value of the objective
     * @param maxValue the maximum value of the objective
     */
    public MissionObjective(String name, String parent, double weight, String type, double target, double minValue, double maxValue) {
        this.objectiveName = name;
        this.objectiveParent = parent;
        this.objectiveWeight = weight;
        this.objectiveType = type;
        this.objectiveTarget = target;
        this.objectiveMinValue = minValue;
        this.objectiveMaxValue = maxValue;
    }

    /**
     * Gets the name of the objective
     * @return the name of the objective
     */
    public String getName() {
        return objectiveName;
    }

    /**
     * Gets the parent name of the objective
     * @return the parent name of the objective
     */
    public String getParent() {
        if (objectiveParent == null){
            return null;
        }else{
            return objectiveParent;
        }

    }

    /**
     * Gets the weight of the objective
     * @return the weight of the objective
     */
    public Double getWeight() {
        if (objectiveWeight == null){
            return null;
        }else{
            return objectiveWeight;
        }

    }

    /**
     * Gets the type of the objective (MIN, MAX or TAR)
     * @return the type of the objective (MIN, MAX or TAR)
     */
    public String getType() {
        return objectiveType;
    }

    /**
     * Gets the target value for objectives of type TAR
     * @return the target value
     */
    public Double getTarget() {
        if (objectiveTarget == null){
            return null;
        }else{
            return objectiveTarget;
        }

    }

    /**
     * Gets the value of a particular objective for a particular architecture
     * @param archCounter the architecture number
     * @return the value of the objective
     * @throws IllegalArgumentException
     */
    public double getObjectiveValue(int archCounter) throws IllegalArgumentException{

        String objectiveName=this.getName();
        double objectiveValue = ResultIO.readOutput(objectiveName, archCounter);

        /*
         Check to see if the objective is supposed to be:
         minimized: default case for moea framework
         maximized: add "-" sign before the objective
         close to some target value: Abs(target value - objective value)
         */
        switch (this.getType()) {
            case "MIN":
                return objectiveValue;
            case "MAX":
                return -objectiveValue;
            case "TAR":
                return Math.abs(this.getTarget() - objectiveValue);
            default:
                throw new IllegalArgumentException("Objective type has not been defined in TradespaceSearch.json.");
        }
    }

    /**
     * Gets the normalized value of a particular objective for a particular architecture
     * @param archCounter the architecture number
     * @return the normalized value of the objective
     * @throws IllegalArgumentException
     */
    public double getObjectiveValueNormalized(int archCounter) throws IllegalArgumentException{
        if (!this.getType().equals("TAR")){
            return (this.getObjectiveValue(archCounter)-this.objectiveMinValue)/(this.objectiveMaxValue-this.objectiveMinValue);
        }else{
            return this.getObjectiveValue(archCounter)/(this.objectiveMaxValue-this.objectiveMinValue);
        }

    }
}
