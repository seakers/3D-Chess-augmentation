package tatc.architecture.specifications;

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class that defines a range of quantitative values with minimum and maximum values and either a step size of
 * equally-spaced steps.
 */
public class QuantitativeRange implements Serializable {
    /**
     * Type tag for JSON file
     */
    @SerializedName("@type")
    private final String _type="QuantitativeRange";
    /**
     * Minimum value or lower bound
     */
    private final Double minValue;
    /**
     * Maximum value or upper bound
     */
    private final Double maxValue;
    /**
     * Step size
     */
    private final Double stepSize;
    /**
     * Number of steps
     */
    private final Integer numberSteps;
    private double min;
    private double max;
    private double step;

    /**
     * Constructs a quantitative range object
     * @param minValue the minimum value or lower bound
     * @param maxValue the maximum value or upper bound
     * @param stepSize the step size
     * @param numberSteps the number of steps
     */
    public QuantitativeRange(double minValue, double maxValue, double stepSize, int numberSteps) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.stepSize = stepSize;
        this.numberSteps = numberSteps;
    }
    /**
     * Gets the minimum value or lower bound
     * @return the minimum value or lower bound
     */
    public double getMinValue() {
        return minValue;
    }

    /**
     * Gets the maximum value or upper bound
     * @return the maximum value or upper bound
     */
    public double getMaxValue() {
        return maxValue;
    }

    /**
     * Gets the step size
     * @return the step size
     */
    public double getStepSize() {
        return stepSize;
    }

    /**
     * Gets the number of steps
     * @return the number of steps
     */
    public int getNumberSteps() {
        return numberSteps;
    }

    /**
     * Discretizes the quantitative range
     * @return list of doubles containing the possible discrete values of this quantitative range
     */
    public ArrayList<Double> discretize() {

        double l = this.getMinValue();
        double u = this.getMaxValue();

        ArrayList<Double> values = new ArrayList<>();

        for (double value = l; value <= u; value = value + this.getStepSize()) {
            values.add(value);
        }
        return values;
    }

    /**
     * Creates a quantitative range value from a linked tree map. Needed when reading quantitative ranges for parameters
     * that can also be single values or lists of values in the TSR
     * @param map the linked tree map containing a quantitative range
     * @return the quantitative range object
     * @throws IllegalArgumentException
     */
    public static QuantitativeRange createQuantitativeRangeFromLinkedTreeMap(LinkedTreeMap map) throws IllegalArgumentException{
        QuantitativeRange qr;
        double minValue = (double)map.get("minValue");
        double maxValue = (double)map.get("maxValue");
        if (map.get("stepSize")!=null){
            double stepSize = (double)map.get("stepSize");
            qr = new QuantitativeRange(minValue,maxValue,stepSize,(int) ( ( (maxValue-minValue) / stepSize ) + 1));
        }else if (map.get("numberSteps")!=null){
            int numberSteps = ((Double)map.get("numberSteps")).intValue();
            qr = new QuantitativeRange(minValue,maxValue,(maxValue-minValue)/(numberSteps-1),numberSteps);
        }else{
            throw new IllegalArgumentException("QuantitativeRange must have either a step size or number of steps");
        }
        return qr;
    }
}
