package tatc.architecture.constellations;

import tatc.architecture.specifications.Satellite;

/**
 * Class that contains the parameters that define a homogeneous Walker constellation
 */
public class StringOfPearlsParameters extends ConstellationParameters{
    /**
     * Altitude in km
     */
    private final double a;
    /**
     * Inclination in deg
     */
    private final double i;
    /**
     * Number of satellites in the constellation
     */
    private final int t;
    /**
     * RAAN in deg
     */
    private final double raan;
    /**
     * The duration of time between satellites in the constellation in ISO 8601 duration format
     */
    private final String satelliteInterval;
    /**
     * Satellite object
     */
    private final Satellite satellite;
    /**
     * Type of constellation
     */
    private final String type;

    /**
     * Constructs a homogeneous Walker constellation parameters object
     * @param a the altitude in km of the orbit
     * @param i the inclination in deg of the orbit
     * @param t the number of satellites in the constellation
     * @param raan the raan in deg of the orbit
     * @param satelliteInterval the duration of time between satellites in the constellation
     * @param satellite the satellite object
     */
    public StringOfPearlsParameters(double a, double i, int t, double raan, String satelliteInterval, Satellite satellite) {
        super();
        this.a = a;
        this.i = i;
        this.t = t;
        this.raan = raan;
        this.satelliteInterval = satelliteInterval;
        this.satellite=satellite;
        this.type="STRING_OF_PEARLS";
    }

    /**
     * Gets the altitude in km
     * @return the altitude in km
     */
    public double getA() {
        return this.a;
    }

    /**
     * Gets the inclination in deg
     * @return the inclination in deg
     */
    public double getI() {
        return this.i;
    }

    /**
     * Gets the number of satellites in the constellation
     * @return the number of satellites in the constellation
     */
    public int getT() {
        return this.t;
    }

    /**
     * Gets the raan in deg
     * @return the raan in deg
     */
    public double getRAAN() {
        return this.raan;
    }

    /**
     * Gets the duration of time between satellites in the constellation
     * @return the duration of time between satellites
     */
    public String getSatelliteInterval() {
        return satelliteInterval;
    }
    /**
     * Gets the satellite object in the constellation
     * @return the satellite object in the constellation
     */
    public Satellite getSatellite() {
        return satellite;
    }

    public String getType() {
        return type;
    }
}
