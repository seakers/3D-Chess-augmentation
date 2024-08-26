package tatc.architecture.outputspecifications;

/**
 * Time to coverage metric class (all metrics in seconds)
 */
public class TimeToCoverage {
    /**
     * Minimum time to coverage
     */
    private final double min;
    /**
     * Maximum time to coverage
     */
    private final double max;
    /**
     * Average or mean time to coverage
     */
    private final double avg;

    /**
     * Constructs a time to coverage object
     * @param min the minimum time to coverage
     * @param max the maximum time to coverage
     * @param avg the average/mean time to coverage
     */
    public TimeToCoverage(double min, double max, double avg){
        this.min = min;
        this.max = max;
        this.avg = avg;
    }

    /**
     * Gets the minimum time to coverage
     * @return the minimum time to coverage
     */
    public double getMin() {
        return min;
    }

    /**
     * Gets the maximum time to coverage
     * @return the maximum time to coverage
     */
    public double getMax() {
        return max;
    }

    /**
     * Gets the average/mean time to coverage
     * @return the average/mean time to coverage
     */
    public double getAvg() {
        return avg;
    }
}
