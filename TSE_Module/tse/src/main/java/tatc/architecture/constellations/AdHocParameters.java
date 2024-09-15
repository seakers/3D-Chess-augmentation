package tatc.architecture.constellations;

import tatc.architecture.specifications.Satellite;

/**
 * Class that contains the parameters that define an AdHoc constellation
 */
public class AdHocParameters extends ConstellationParameters{
    /**
     * Number of satellites in the constellation
     */
    private final int nsat;
    /**
     * Satellite object
     */
    private final Satellite satellite;
    /**
     * Type of constellation
     */
    private final String type;

    /**
     * Constructs an AdHoc constellation parameters object
     * @param nsat the number of satellites
     * @param satellite the satellite object
     */
    public AdHocParameters(int nsat, Satellite satellite) {
        super();
        this.nsat = nsat;
        this.satellite = satellite;
        this.type = "AD_HOC";
    }

    /**
     * Gets the number of satellites in the constellation
     * @return the number of satellites in the constellation
     */
    public int getNsat() {
        return nsat;
    }

    /**
     * Gets the satellite object in the constellation
     * @return the satellite object in the constellation
     */
    public Satellite getSatellite() {
        return satellite;
    }

    @Override
    public String getType() {
        return type;
    }
}
