package tatc.architecture.outputspecifications;

/**
 * Time metric class (all metrics in seconds)
 */
public class Time {
    /**
     * Minimum time
     */
    private final double min;
    /**
     * Maximum time
     */
    private final double max;


    /**
     * Constructs a time metric object
     * @param min the minimum time
     * @param max the maximum time
     */
    public Time(double min, double max){
        this.min = min;
        this.max = max;
    }

    /**
     * Gets the minimum time
     * @return the minimum time
     */
    public double getMin() {
        return min;
    }

    /**
     * Gets the maximum time
     * @return the maximum time
     */
    public double getMax() {
        return max;
    }
}
