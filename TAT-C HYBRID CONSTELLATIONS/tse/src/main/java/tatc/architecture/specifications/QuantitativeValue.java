package tatc.architecture.specifications;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Class that defines a range of quantitative values with minimum and maximum values (bounds)
 */
public class QuantitativeValue implements Serializable {
    /**
     * Type tag for JSON file
     */
    @SerializedName("@type")
    private final String _type="QuantitativeValue";
    /**
     * Minimum value or lower bound
     */
    private final Double minValue;
    /**
     * Maximum value or upper bound
     */
    private final Double maxValue;

    /**
     * Constructs a quantitative value object
     * @param minValue the minimum value or lower bound
     * @param maxValue the maximum value or upper bound
     */
    public QuantitativeValue(double minValue, double maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
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
}
