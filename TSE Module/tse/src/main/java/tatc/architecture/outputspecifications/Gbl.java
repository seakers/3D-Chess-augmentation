package tatc.architecture.outputspecifications;

/**
 * Class that contains all the global outputs/metrics from the orbits module  (all time metrics in seconds)
 */
public class Gbl {
    /**
     * Time
     */
    private final Time Time;
    /**
     * Time to coverage
     */
    private final TimeToCoverage TimeToCoverage;
    /**
     * Access time
     */
    private final AccessTime AccessTime;
    /**
     * Revisit time
     */
    private final RevisitTime RevisitTime;
    /**
     * Response time
     */
    private final ResponseTime ResponseTime;
    /**
     * Percentage of Earth covered
     */
    private final double Coverage;
    /**
     * Number of point of interest passes
     */
    private final NumOfPOIpasses NumOfPOIpasses;
    /**
     * Data latency
     */
    private final DataLatency DataLatency;
    /**
     * Number of ground station passes divided by the simulation time in days
     */
    private final double numGSpassesPD;
    /**
     * Total downlink time divided by the simulation time in days
     */
    private final double totalDownlinkTimePD;
    /**
     * Downlink time per pass
     */
    private final DownlinkTimePerPass DownlinkTimePerPass;

    /**
     * Constructs an orbits module global object with all of the metrics
     * @param time the time
     * @param timeToCoverage the time to coverage
     * @param accessTime the access time
     * @param revisitTime the revisit time
     * @param responseTime the response time
     * @param Coverage the percentage of Earth covered
     * @param numOfPOIpasses the number of point of interest passes
     * @param dataLatency the data latency
     * @param numGSpassesPD the number of ground station passes divided by the simulation time in days
     * @param totalDownlinkTimePD the total downlink time divided by the simulation time in days
     * @param downlinkTimePerPass the downlink time per pass
     */
    public Gbl(Time time, TimeToCoverage timeToCoverage, AccessTime accessTime, RevisitTime revisitTime, ResponseTime responseTime, double Coverage, NumOfPOIpasses numOfPOIpasses, DataLatency dataLatency, double numGSpassesPD, double totalDownlinkTimePD, DownlinkTimePerPass downlinkTimePerPass){
        this.Time = time;
        this.TimeToCoverage = timeToCoverage;
        this.AccessTime = accessTime;
        this.RevisitTime = revisitTime;
        this.ResponseTime = responseTime;
        this.Coverage = Coverage;
        this.NumOfPOIpasses = numOfPOIpasses;
        this.DataLatency = dataLatency;
        this.numGSpassesPD = numGSpassesPD;
        this.totalDownlinkTimePD = totalDownlinkTimePD;
        this.DownlinkTimePerPass = downlinkTimePerPass;
    }

    /**
     * Gets the time
     * @return the time
     */
    public Time getTime() {
        return Time;
    }

    /**
     * Gets the time to coverage
     * @return the time to coverage
     */
    public TimeToCoverage getTimeToCoverage() {
        return TimeToCoverage;
    }

    /**
     * Gets the access time
     * @return the access time
     */
    public AccessTime getAccessTime() {
        return AccessTime;
    }

    /**
     * Gets the revisit time
     * @return the revisit time
     */
    public RevisitTime getRevisitTime() {
        return RevisitTime;
    }

    /**
     * Gets the response time
     * @return the response time
     */
    public ResponseTime getResponseTime() {
        return ResponseTime;
    }

    /**
     * Gets the percentage of Earth covered
     * @return the percentage of Earth covered
     */
    public double getCoverage() {
        return Coverage;
    }

    /**
     * Gets the number of point of interest passes
     * @return the number of point of interest passes
     */
    public NumOfPOIpasses getNumOfPOIpasses() {
        return NumOfPOIpasses;
    }

    /**
     * Gets the data latency
     * @return the data latency
     */
    public DataLatency getDataLatency() {
        return DataLatency;
    }

    /**
     * Gets the number of ground station passes divided by the simulation time in days
     * @return the number of ground station passes divided by the simulation time in days
     */
    public double getNumGSpassesPD() {
        return numGSpassesPD;
    }

    /**
     * Gets the total downlink time divided by the simulation time in days
     * @return the total downlink time divided by the simulation time in days
     */
    public double getTotalDownlinkTimePD() {
        return totalDownlinkTimePD;
    }

    /**
     * Gets the downlink time per pass
     * @return the downlink time per pass
     */
    public DownlinkTimePerPass getDownlinkTimePerPass() {
        return DownlinkTimePerPass;
    }
}
