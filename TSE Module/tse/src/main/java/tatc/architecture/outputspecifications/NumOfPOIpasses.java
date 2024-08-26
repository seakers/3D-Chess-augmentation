package tatc.architecture.outputspecifications;

/**
 * Number of point of interest passes metric class
 */
public class NumOfPOIpasses {
    /**
     * Minimum number of point of interest passes
     */
    private final double min;
    /**
     * Maximum number of point of interest passes
     */
    private final double max;
    /**
     * Average or mean number of point of interest passes
     */
    private final double avg;

    /**
     * Constructs a number of point of interest passes object
     * @param min the minimum number of point of interest passes
     * @param max the maximum number of point of interest passes
     * @param avg the average/mean number of point of interest passes
     */
    public NumOfPOIpasses(double min, double max, double avg){
        this.min = min;
        this.max = max;
        this.avg = avg;
    }

    /**
     * Gets the minimum number of point of interest passes
     * @return the minimum number of point of interest passes
     */
    public double getMin() {
        return min;
    }

    /**
     * Gets the maximum number of point of interest passes
     * @return the maximum number of point of interest passes
     */
    public double getMax() {
        return max;
    }

    /**
     * Gets the average/mean number of point of interest passes
     * @return the average/mean number of point of interest passes
     */
    public double getAvg() {
        return avg;
    }
}