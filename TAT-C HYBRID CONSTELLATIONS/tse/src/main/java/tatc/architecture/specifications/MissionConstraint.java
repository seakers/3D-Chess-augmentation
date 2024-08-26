package tatc.architecture.specifications;

import com.google.gson.annotations.SerializedName;
import org.hipparchus.util.FastMath;
import tatc.ResultIO;

import java.io.File;
import java.io.Serializable;

/**
 * A class that defines a mission constraints. Mission constraints are included in the intelligent search (i.e. genetic algorithm)
 */
public class MissionConstraint implements Serializable {
    /**
     * Type tag for JSON file
     */
    @SerializedName("@type")
    private final String _type="MissionConstraint";
    /**
     * Name of the constraint. It has to match the outputs key values from gbl.json, CostRisk_Output.json
     * or level_2_metrics.csv. TODO: Value module objectives need to be added.
     */
    private final String constraintName;

    /**
     * constraint type including: "GEQ" (greater or equal) , "LEQ" (less or equal), "EQ" (equality), "NEQ" (not equal)
     */
    private final String constraintType;
    /**
     * Level value of the constraint
     */
    private final Double constraintLevel;
    /**
     * Boolean to indicate if it has to be treated as a hard or soft constraint
     */
    private final Boolean isHard;
    /**
     * Weight of the constraint
     */
    private final Double constraintWeight;
    /**
     * The minimum value of this constraint used for normalization
     */
    private final Double constraintMinValue;
    /**
     * The maximum value of this constraint used for normalization
     */
    private final Double constraintMaxValue;

    /**
     * Constructs a mission constraint object
     * @param name the constraint name
     * @param type the constraint type (GEQ, LEQ, EQ or NEQ)
     * @param level the target value for constraints of type TAR
     * @param isHard true if it is a hard constraint
     * @param constraintWeight lagrange multiplier if it is a soft constraint
     * @param constraintMinValue minimum value of this constraint
     * @param constraintMaxValue maximum value of this constraint
     */
    public MissionConstraint(String name, String type, double level, Boolean isHard, double constraintWeight, double constraintMinValue, double constraintMaxValue) {
        this.constraintName = name;
        this.constraintType = type;
        this.constraintLevel = level;
        this.isHard = isHard;
        this.constraintWeight = constraintWeight;
        this.constraintMinValue = constraintMinValue;
        this.constraintMaxValue = constraintMaxValue;
    }

    /**
     * Gets the name of the constraint
     * @return the name of the constraint
     */
    public String getName() {
        return constraintName;
    }


    /**
     * Gets the type of the constraint (GEQ, LEQ, EQ or INEQ)
     * @return the type of the constraint (GEQ, LEQ, EQ or INEQ)
     */
    public String getType() {
        return constraintType;
    }

    /**
     * Gets the constraint level value
     * @return the level value
     */
    public Double getLevel() {
        return constraintLevel;
    }

    /**
     * Checks if this constraint is a hard constraint
     * @return true if it is a hard constraint and false if it is a soft constraint
     */
    public Boolean isHard() {
        return isHard;
    }

    /**
     * Gets the weight of this constraint
     * @return the weight of this constraint
     */
    public Double getWeight() {
        return constraintWeight;
    }

    /**
     * Gets the minimum value of this constraint
     * @return the minimum value of this constraint
     */
    public Double getMinValue() {
        return constraintMinValue;
    }

    /**
     * Gets the maximum value of this constraint
     * @return the maximum value of this constraint
     */
    public Double getMaxValue() {
        return constraintMaxValue;
    }

    /**
     * Gets the value of a particular constraint for a particular architecture
     * @param archCounter the architecture number
     * @return the value of the constraint
     * @throws IllegalArgumentException
     */
    public double getConstraintValue(int archCounter) throws IllegalArgumentException{
        String constraintName=this.getName();
        double constraintValue = ResultIO.readOutput(constraintName, archCounter);

        switch (this.getType()) {
            case "GEQ":
                return constraintValue >= this.constraintLevel ? 0.0 : FastMath.abs(constraintValue-this.constraintLevel );
            case "LEQ":
                return constraintValue <= this.constraintLevel ? 0.0 : FastMath.abs(constraintValue-this.constraintLevel );
            case "EQ":
                return constraintValue == this.constraintLevel ? 0.0 : FastMath.abs(constraintValue-this.constraintLevel );
            case "NEQ":
                return constraintValue != this.constraintLevel ? 0.0 : 1;
            default:
                throw new IllegalArgumentException("Constraint type has not been defined in TradespaceSearch.json.");
        }
    }

    /**
     * Gets the normalized value of a particular constraint for a particular architecture
     * @param archCounter the architecture number
     * @return the normalized value of the constraint
     * @throws IllegalArgumentException
     */
    public double getConstraintValueNormalized(int archCounter) throws IllegalArgumentException{
        double constraintValue = this.getConstraintValue(archCounter);
        if (constraintValue == 0.0){
            return constraintValue;
        }else{
            return constraintValue/(this.constraintMaxValue-this.constraintMinValue);
        }

    }
}
