package tatc.architecture.outputspecifications;

/**
 * Response time metric class (all metrics in seconds)
 */
public class ResponseTime {
    /**
     * Minimum response time
     */
    private final double min;
    /**
     * Maximum response time
     */
    private final double max;
    /**
     * Average or mean response time
     */
    private final double avg;

    /**
     * Constructs a response time object
     * @param min the minimum response time
     * @param max the maximum response time
     * @param avg the average/mean response time
     */
    public ResponseTime(double min, double max, double avg){
        this.min = min;
        this.max = max;
        this.avg = avg;
    }

    /**
     * Gets the minimum response time
     * @return the minimum response time
     */
    public double getMin() {
        return min;
    }

    /**
     * Gets the maximum response time
     * @return the maximum response time
     */
    public double getMax() {
        return max;
    }

    /**
     * Gets the average/mean response time
     * @return the average/mean response time
     */
    public double getAvg() {
        return avg;
    }
}