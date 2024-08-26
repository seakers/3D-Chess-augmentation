package tatc.architecture.outputspecifications;

/**
 * Access time metric class (all metrics in seconds)
 */
public class AccessTime{
    /**
     * Minimum access time
     */
    private final double min;
    /**
     * Maximum access time
     */
    private final double max;
    /**
     * Average or mean access time
     */
    private final double avg;

    /**
     * Constructs an access time object
     * @param min the minimum access time
     * @param max the maximum access time
     * @param avg the average/mean access time
     */
    public AccessTime(double min, double max, double avg){
        this.min = min;
        this.max = max;
        this.avg = avg;
    }

    /**
     * Gets the minimum access time
     * @return the minimum access time
     */
    public double getMin() {
        return min;
    }

    /**
     * Gets the maximum access time
     * @return the maximum access time
     */
    public double getMax() {
        return max;
    }

    /**
     * Gets the average/mean access time
     * @return the average/mean access time
     */
    public double getAvg() {
        return avg;
    }
}
