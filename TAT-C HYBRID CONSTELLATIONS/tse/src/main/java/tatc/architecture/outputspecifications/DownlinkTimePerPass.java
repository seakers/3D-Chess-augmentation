package tatc.architecture.outputspecifications;

/**
 * Downlink time per pass metric class (all metrics in seconds)
 */
public class DownlinkTimePerPass {
    /**
     * Minimum downlink time per pass
     */
    private final double min;
    /**
     * Maximum downlink time per pass
     */
    private final double max;
    /**
     * Average or mean downlink time per pass
     */
    private final double avg;

    /**
     * Constructs a downlink time per pass object
     * @param min the minimum downlink time per pass
     * @param max the maximum downlink time per pass
     * @param avg the average/mean downlink time per pass
     */
    public DownlinkTimePerPass(double min, double max, double avg){
        this.min = min;
        this.max = max;
        this.avg = avg;
    }

    /**
     * Gets the minimum downlink time per pass
     * @return the minimum downlink time per pass
     */
    public double getMin() {
        return min;
    }

    /**
     * Gets the maximum downlink time per pass
     * @return the maximum downlink time per pass
     */
    public double getMax() {
        return max;
    }

    /**
     * Gets the average/mean downlink time per pass
     * @return the average/mean downlink time per pass
     */
    public double getAvg() {
        return avg;
    }
}
