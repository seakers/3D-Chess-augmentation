package tatc.architecture.outputspecifications;

import com.google.gson.annotations.SerializedName;

public class ValueOutput {

    /**
     * Architecture identifier
     */
    @SerializedName("Architecture")
    private final String architecture;
    /**
     * The number of regions of interest in the coverage analysis
     */
    @SerializedName("Number of Regions")
    private final Integer numberOfRegions;
    /**
     * Total architecture value in Mbits
     */
    @SerializedName("Total Architecture Value [Mbits]")
    private final String totalArchitectureValue;
    /**
     * Total Lifecycle Cost in $M
     */
    @SerializedName("Total Lifecycle Cost [$M]")
    private final String totalLifecycleCost;
    /**
     * Total ratio of value to cost Mbits/$M
     */
    @SerializedName("Ratio of Value to Cost [Mbits/$M]")
    private final String ratioOfValueToCost;
    /**
     * Total data collected Mbits
     */
    @SerializedName("Total Data Collected [Mbits]")
    private final String totalDataCollected;
    /**
     * EDA scaling factor
     */
    @SerializedName("EDA Scaling Factor")
    private final String eDAScalingFactor;
    /**
     * Average ground point resolution in km^2
     */
    @SerializedName("Average GP Resolution [km^2]")
    private final String averageGPResolution;

    /**
     * The number of satellites in the architecture
     */
    @SerializedName("Number of Satellites")
    private final Integer numberOfSatellites;

    /**
     * Constructs a value output object
     * @param architecture the architecture identifier
     * @param numberOfRegions the number of regions of interest
     * @param totalArchitectureValue the total architecture value in Mbits
     * @param totalLifecycleCost the total lifecycle cost in $M
     * @param ratioOfValueToCost the ratio of value to cost in Mbits/$M
     * @param totalDataCollected the total data collected in Mbits
     * @param eDAScalingFactor the EDA scaling factor
     * @param averageGPResolution the average ground point resolution in km^2
     * @param numberOfSatellites the number of satellites
     */
    public ValueOutput(String architecture, Integer numberOfRegions, String totalArchitectureValue,
                       String totalLifecycleCost, String ratioOfValueToCost, String totalDataCollected,
                       String eDAScalingFactor, String averageGPResolution, Integer numberOfSatellites) {
        this.architecture = architecture;
        this.numberOfRegions = numberOfRegions;
        this.totalArchitectureValue = totalArchitectureValue;
        this.totalLifecycleCost = totalLifecycleCost;
        this.ratioOfValueToCost = ratioOfValueToCost;
        this.totalDataCollected = totalDataCollected;
        this.eDAScalingFactor = eDAScalingFactor;
        this.averageGPResolution = averageGPResolution;
        this.numberOfSatellites = numberOfSatellites;
    }

    /**
     * Gets the architecture identifier
     * @return the architecture identifier
     */
    public String getArchitecture() {
        return architecture;
    }

    /**
     * Gets the number of regions of interest
     * @return the number of regions of interest
     */
    public Integer getNumberOfRegions() {
        return numberOfRegions;
    }

    /**
     * Gets the total architecture value in Mbits
     * @return the total architecture value in Mbits
     */
    public String getTotalArchitectureValue() {
        return totalArchitectureValue;
    }

    /**
     * Gets the total lifecycle cost in $M
     * @return the total lifecycle cost in $M
     */
    public String getTotalLifecycleCost() {
        return totalLifecycleCost;
    }

    /**
     * Gets the ratio of value to cost in Mbits/$M
     * @return the ratio of value to cost in Mbits/$M
     */
    public String getRatioOfValueToCost() {
        return ratioOfValueToCost;
    }

    /**
     * Gets the total data collected in Mbits
     * @return the total data collected in Mbits
     */
    public String getTotalDataCollected() {
        return totalDataCollected;
    }

    /**
     * Gets the EDA scaling factor
     * @return the EDA scaling factor
     */
    public String getEDAScalingFactor() {
        return eDAScalingFactor;
    }

    /**
     * Gets the average ground point resolution in km^2
     * @return the average ground point resolution in km^2
     */
    public String getAverageGPResolution() {
        return averageGPResolution;
    }

    /**
     * Gets the number of satellites
     * @return the number of satellites
     */
    public Integer getNumberOfSatellites() {
        return numberOfSatellites;
    }

}