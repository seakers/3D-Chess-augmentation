package tatc.architecture.outputspecifications;

/**
 * Revisit time metric class (all metrics in seconds)
 */
public class RevisitTime {
    /**
     * Minimum revisit time
     */
    private final double min;
    /**
     * Maximum revisit time
     */
    private final double max;
    /**
     * Average or mean revisit time
     */
    private final double avg;

    /**
     * Constructs a revisit time object
     * @param min the minimum revisit time
     * @param max the maximum revisit time
     * @param avg the average/mean revisit time
     */
    public RevisitTime(double min, double max, double avg){
        this.min = min;
        this.max = max;
        this.avg = avg;
    }

    /**
     * Gets the minimum revisit time
     * @return the minimum revisit time
     */
    public double getMin() {
        return min;
    }

    /**
     * Gets the maximum revisit time
     * @return the maximum revisit time
     */
    public double getMax() {
        return max;
    }

    /**
     * Gets the average/mean revisit time
     * @return the average/mean revisit time
     */
    public double getAvg() {
        return avg;
    }
}
