package tatc.architecture.outputspecifications;

/**
 * Data latency metric class (all metrics in seconds)
 */
public class DataLatency {
    /**
     * Minimum data latency
     */
    private final double min;
    /**
     * Maximum data latency
     */
    private final double max;
    /**
     * Average or mean data latency
     */
    private final double avg;

    /**
     * Constructs a data latency object
     * @param min the minimum data latency
     * @param max the maximum data latency
     * @param avg the average/mean data latency
     */
    public DataLatency(double min, double max, double avg){
        this.min = min;
        this.max = max;
        this.avg = avg;
    }

    /**
     * Gets the minimum data latency
     * @return the minimum data latency
     */
    public double getMin() {
        return min;
    }

    /**
     * Gets the maximum data latency
     * @return the maximum data latency
     */
    public double getMax() {
        return max;
    }

    /**
     * Gets the average/mean data latency
     * @return the average/mean data latency
     */
    public double getAvg() {
        return avg;
    }
}