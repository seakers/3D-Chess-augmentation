package tatc.architecture.variable;

import org.moeaframework.core.PRNG;
import org.moeaframework.core.Variable;
import tatc.architecture.specifications.Satellite;

import java.util.List;
import java.util.Objects;

/**
 * A variable containing the information for a Train constellation.
 */
public class TrainVariable implements Variable {

    /**
     * List of possible values of altitudes in km
     */
    private final List<Double> altAllowed;

    /**
     * List of possible values of number of satellites
     */
    private final List<Integer> tAllowed;

    /**
     * List of possible values of Longitude Time of the Ascending Node of the first(reference) satellite
     * in "hh:mm:ss" format
     */
    private final List<String> ltanAllowed;

    /**
     * List of possible durations of time between satellites in a train constellation in ISO 8601 duration format
     */
    private final List<String> satIntervalsAllowed;

    /**
     * List of possible satellite objects
     */
    private final List<Satellite> satAllowed;

    /**
     * Toggle if this constellation utilizes secondary payloads
     */
    private final Boolean secondaryPayload;

    /**
     * Eccentricity value
     */
    private final Double eccentricity;

    /**
     * Altitude value
     */
    private Double alt;

    /**
     * Number of satellites
     */
    private Integer t;

    /**
     * Longitude Time of the Ascending Node of the first(reference) satellite in "hh:mm:ss" format
     */
    private String LTAN;

    /**
     * Duration of time between satellites in a train constellation in ISO 8601 duration format
     */
    private String satInterval;

    /**
     * Satellite in the constellation
     */
    private Satellite satellite;

    /**
     * Constructs a train variable object
     * @param altAllowed the list of possible values of altitudes in km
     * @param tAllowed the list of possible values of number of satellites
     * @param ltanAllowed the list of possible values of Longitude Time of the Ascending Node of the first(reference)
     *                    satellite in "hh:mm:ss" format
     * @param satIntervalsAllowed the list of possible durations of time between satellites in a train constellation
     *                            in ISO 8601 duration format
     * @param satAllowed the list of possible values of satellites
     * @param secondaryPayload true if the constellation uses secondary payloads and false otherwise (or null if not specified)
     * @param eccentricity the eccentricity of all the orbits
     */
    public TrainVariable(List<Double> altAllowed, List<Integer> tAllowed,List<String> ltanAllowed,
                         List<String> satIntervalsAllowed, List<Satellite> satAllowed, Boolean secondaryPayload,
                         Double eccentricity) {
        this.altAllowed = altAllowed;
        this.tAllowed = tAllowed;
        this.ltanAllowed = ltanAllowed;
        this.satIntervalsAllowed = satIntervalsAllowed;
        this.satAllowed = satAllowed;
        this.secondaryPayload = secondaryPayload;
        this.eccentricity = eccentricity;
        this.alt = -1.0;
        this.t = -1;
        this.LTAN = null;
        this.satInterval = null;
        this.satellite = null;
    }

    /**
     * Copies the fields of the given constellation variable and creates a new instance of a constellation.
     *
     * @param var the constellation variable to copy
     */
    protected TrainVariable(TrainVariable var) {
        this(var.altAllowed,var.tAllowed,var.ltanAllowed,var.satIntervalsAllowed,var.satAllowed, var.secondaryPayload, var.eccentricity);
        this.alt = var.getAlt();
        this.t = var.getT();
        this.LTAN = var.getLTAN();
        this.satInterval = var.getSatInterval();
        this.satellite = var.getSatellite();
    }


    @Override
    public Variable copy() {
        return new TrainVariable(this);
    }

    @Override
    public void randomize() {
        int altIndex = PRNG.nextInt(0, altAllowed.size()-1);
        int tIndex = PRNG.nextInt(0, tAllowed.size()-1);
        int ltanIndex = PRNG.nextInt(0, ltanAllowed.size()-1);
        int satIntervalIndex = PRNG.nextInt(0, satIntervalsAllowed.size()-1);
        int satIndex = PRNG.nextInt(0, satAllowed.size()-1);

        this.setAlt(altAllowed.get(altIndex));
        this.setT(tAllowed.get(tIndex));
        this.setLTAN(ltanAllowed.get(ltanIndex));
        this.setSatInterval(satIntervalsAllowed.get(satIntervalIndex));
        this.setSatellite(satAllowed.get(satIndex));

    }

    /**
     * Gets the list of possible values of altitudes in km
     * @return the list of possible values of altitudes in km
     */
    public List<Double> getAltAllowed() {
        return altAllowed;
    }

    /**
     * Gets the list of possible values of number of satellites
     * @return the list of possible values of number of satellites
     */
    public List<Integer> gettAllowed() {
        return tAllowed;
    }

    /**
     * Gets the list of possible values of Longitude Time of the Ascending Node of the first(reference) satellite
     * in "hh:mm:ss" format
     * @return the list of possible values of Longitude Time of the Ascending Node of the first(reference) satellite
     * in "hh:mm:ss" format
     */
    public List<String> getLtanAllowed() {
        return ltanAllowed;
    }

    /**
     * Gets the list of possible durations of time between satellites in a train constellation in ISO 8601 duration format
     * @return the list of possible durations of time between satellites in a train constellation in ISO 8601 duration format
     */
    public List<String> getSatIntervalsAllowed() {
        return satIntervalsAllowed;
    }

    /**
     * Gets the list of possible values of satellites
     * @return the list of possible values of satellites
     */
    public List<Satellite> getSatAllowed() {
        return satAllowed;
    }

    /**
     * Gets the boolean that specifies if this constellation utilizes secondary payloads
     * @return true if the constellation uses secondary payloads and false otherwise (or null if not specified)
     */
    public Boolean getSecondaryPayload() {
        return secondaryPayload;
    }

    /**
     * Gets the eccentricity
     * @return the eccentricity
     */
    public Double getEccentricity() {
        return eccentricity;
    }

    /**
     * Gets the altitude in km
     * @return the altitude in km
     */
    public Double getAlt() {
        return alt;
    }

    /**
     * Gets the number of satellites
     * @return the number of satellites
     */
    public Integer getT() {
        return t;
    }

    /**
     * Gets the Longitude Time of the Ascending Node of the first(reference) satellite
     * @return the Longitude Time of the Ascending Node of the first(reference) satellite
     */
    public String getLTAN() {
        return LTAN;
    }

    /**
     * Gets the duration of time between satellites in a train constellation in ISO 8601 duration format
     * @return the duration of time between satellites in a train constellation in ISO 8601 duration format
     */
    public String getSatInterval() {
        return satInterval;
    }

    /**
     * Gets the satellite object in the constellation
     * @return the satellite object in the constellation
     */
    public Satellite getSatellite() {
        return satellite;
    }

    /**
     * Sets the altitude in km
     * @param alt the altitude in km
     */
    public void setAlt(Double alt) {
        if (altAllowed.contains(alt)) {
            this.alt = alt;
        } else {
            throw new IllegalArgumentException(String.format("altitude not included in TradespaceSearch.json"));
        }
    }

    /**
     * Sets the number of satellites
     * @param t the number of satellites
     */
    public void setT(Integer t) {
        if (tAllowed.contains(t)) {
            this.t = t;
        } else {
            throw new IllegalArgumentException(String.format("Number of satellites not included in TradespaceSearch.json"));
        }
    }

    /**
     * Sets the Longitude Time of the Ascending Node of the first(reference) satellite
     * @param ltan the Longitude Time of the Ascending Node of the first(reference) satellite
     */
    public void setLTAN(String ltan) {
        if (ltanAllowed.contains(ltan)) {
            this.LTAN = ltan;
        } else {
            throw new IllegalArgumentException(String.format("LTAN not included in TradespaceSearch.json"));
        }
    }

    /**
     * Sets the duration of time between satellites in a train constellation in ISO 8601 duration format
     * @param satInterval the duration of time between satellites in a train constellation in ISO 8601 duration format
     */
    public void setSatInterval(String satInterval) {
        if (satIntervalsAllowed.contains(satInterval)) {
            this.satInterval = satInterval;
        } else {
            throw new IllegalArgumentException(String.format("Satellite interval not included in TradespaceSearch.json"));
        }
    }

    /**
     * Sets the satellite in the constellation
     * @param satellite the satellite in the constellation
     */
    public void setSatellite(Satellite satellite) {
        this.satellite = satellite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrainVariable that = (TrainVariable) o;
        return Objects.equals(altAllowed, that.altAllowed) &&
                Objects.equals(tAllowed, that.tAllowed) &&
                Objects.equals(ltanAllowed, that.ltanAllowed) &&
                Objects.equals(satIntervalsAllowed, that.satIntervalsAllowed) &&
                Objects.equals(satAllowed, that.satAllowed) &&
                Objects.equals(alt, that.alt) &&
                Objects.equals(t, that.t) &&
                Objects.equals(LTAN, that.LTAN) &&
                Objects.equals(satInterval, that.satInterval) &&
                Objects.equals(satellite, that.satellite);
    }

    @Override
    public int hashCode() {
        return Objects.hash(altAllowed, tAllowed, ltanAllowed, satIntervalsAllowed, satAllowed, alt, t, LTAN, satInterval, satellite);
    }
}
