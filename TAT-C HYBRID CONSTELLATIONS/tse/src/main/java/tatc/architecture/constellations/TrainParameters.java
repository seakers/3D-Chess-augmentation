package tatc.architecture.constellations;

import tatc.architecture.specifications.Satellite;

/**
 * Class that contains the parameters that define a Train constellation
 */
public class TrainParameters extends ConstellationParameters{
    /**
     * Altitude in km
     */
    private final double a;
    /**
     * Number of satellites in the constellation
     */
    private final int nsat;
    /**
     * Longitude Time of the Ascending Node of the first(reference) satellite in "hh:mm:ss" format
     */
    private final String LTANref;
    /**
     * The duration of time between satellites in a train constellation in ISO 8601 duration format
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
     * Constructs a heterogeneous Walker constellation parameters object
     * @param a the altitude in km of the orbits
     * @param nsat the number of satellites in the train constellation
     * @param LTANref the Longitude Time of the Ascending node of the reference satellite
     * @param satelliteInterval the duration of time between satellites in the train constellation
     * @param satellite the satellite object
     */
    public TrainParameters(double a,  int nsat, String LTANref, String satelliteInterval ,Satellite satellite) {
        super();
        this.a = a;
        this.nsat = nsat;
        this.LTANref = LTANref;
        this.satelliteInterval = satelliteInterval;
        this.satellite = satellite;
        this.type = "TRAIN";
    }

    /**
     * Gets the altitude in km
     * @return the altitude in km
     */
    public double getA() {
        return a;
    }

    /**
     * Gets the number of satellites
     * @return the number of satellites
     */
    public int getNsat() {
        return nsat;
    }

    /**
     * Gets the Longitude Time of the Ascending Node of the first(reference) satellite
     * @return the Longitude Time of the Ascending Node of the first satellite in the constellation
     */
    public String getLTANref() {
        return LTANref;
    }

    /**
     * Gets the duration of time between satellites in a train constellation
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

    @Override
    public String getType() {
        return type;
    }
}
