package tatc.architecture.constellations;

/**
 * Abstract class that needs to be extended by any class that defines constellation parameters/decisions
 */
public abstract class ConstellationParameters {

    /**
     * Toggle if this constellation utilizes secondary payloads
     */
    private Boolean secondaryPayload;

    /**
     * Eccentricity of the orbits
     */
    private Double eccentricity;

    public ConstellationParameters(){
        this.secondaryPayload = null;
        this.eccentricity = null;
    }

    /**
     * Gets the constellation type
     * @return constellation type string
     */
    public abstract String getType();

    /**
     * Gets the boolean that specifies if this constellation utilizes secondary payloads
     * @return true if the constellation uses secondary payloads and false otherwise (or null if not specified)
     */
    public Boolean getSecondaryPayload() {
        return secondaryPayload;
    }

    /**
     * Sets the boolean that specifies if this constellation utilizes secondary payloads
     * @param secondaryPayload true if the constellation uses secondary payloads and false otherwise
     */
    public void setSecondaryPayload(Boolean secondaryPayload) {
        this.secondaryPayload = secondaryPayload;
    }

    /**
     * Gets the eccentricity
     * @return the eccentricity
     */
    public Double getEccentricity() {
        return eccentricity;
    }

    /**
     * Sets the eccentricity
     * @param eccentricity the eccentricity
     */
    public void setEccentricity(Double eccentricity) {
        this.eccentricity = eccentricity;
    }
}
