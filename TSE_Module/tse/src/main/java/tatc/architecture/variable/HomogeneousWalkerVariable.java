package tatc.architecture.variable;

import org.moeaframework.core.PRNG;
import org.moeaframework.core.Variable;
import tatc.architecture.specifications.Satellite;
import java.util.List;
import java.util.Objects;

/**
 * A variable containing the information for a homogeneous Walker constellation.
 */
public class HomogeneousWalkerVariable implements Variable {

    /**
     * List of possible values of altitudes in km
     */
    private final List<Double> altAllowed;

    /**
     * List of possible values of inclination in deg or "SSO"
     */
    private final List<Object> incAllowed;

    /**
     * List of possible values of number of satellites
     */
    private final List<Integer> tAllowed;

    /**
     * List of possible values of number of planes
     */
    private final List<Integer> pAllowed;

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
     * Inclination value
     */
    private Object inc;

    /**
     * Number of satellites in the constellation
     */
    private Integer t;

    /**
     * Real value from 0 to 1 that contains information about the number of planes in the walker constellation
     */
    private Double pReal;

    /**
     * Real value from 0 to 1 that contains information about the relative spacing (f) in the walker constellation
     */
    private Double fReal;

    /**
     * Satellite in the constellation
     */
    private Satellite satellite;

    /**
     * Constructs a homogeneous walker variable. Altitude, inclination, number of satellites, real plane value,
     * real relative spacing value and satellite are either initialized to null or to incorrect values.
     * @param altAllowed the list of possible values of altitudes
     * @param incAllowed the list of possible values of inclinations
     * @param tAllowed the list of possible values of number of satellites
     * @param pAllowed the list of possible values of number of planes
     * @param satAllowed the list of possible values of satellites
     * @param secondaryPayload true if the constellation uses secondary payloads and false otherwise (or null if not specified)
     * @param eccentricity the eccentricity of all the orbits
     */
    public HomogeneousWalkerVariable(List<Double> altAllowed, List<Object> incAllowed, List<Integer> tAllowed,
                                     List<Integer> pAllowed, List<Satellite> satAllowed, Boolean secondaryPayload, Double eccentricity) {
        this.altAllowed = altAllowed;
        this.incAllowed = incAllowed;
        this.tAllowed = tAllowed;
        this.pAllowed = pAllowed;
        this.satAllowed = satAllowed;
        this.secondaryPayload = secondaryPayload;
        this.eccentricity = eccentricity;
        this.alt = -1.0;
        this.inc = null;
        this.t = -1;
        this.pReal = -1.0;
        this.fReal = -1.0;
        this.satellite = null;
    }

    /**
     * Copies the fields of the given constellation variable and creates a new
     * instance of a constellation.
     *
     * @param var the constellation variable to copy
     */
    protected HomogeneousWalkerVariable(HomogeneousWalkerVariable var) {
        this(var.altAllowed, var.incAllowed, var.tAllowed, var.pAllowed, var.satAllowed, var.secondaryPayload, var.eccentricity);
        this.alt = var.getAlt();
        this.inc = var.getInc();
        this.t = var.getT();
        this.pReal = var.getpReal();
        this.fReal = var.getfReal();
        this.satellite = var.getSatellite();
    }


    @Override
    public Variable copy() {
        return new HomogeneousWalkerVariable(this);
    }

    @Override
    public void randomize() {
        int altIndex = PRNG.nextInt(0, altAllowed.size()-1);
        int incIndex = PRNG.nextInt(0, incAllowed.size()-1);
        int tIndex = PRNG.nextInt(0, tAllowed.size()-1);
        int satIndex = PRNG.nextInt(0, satAllowed.size()-1);

        this.setAlt(altAllowed.get(altIndex));
        this.setInc(incAllowed.get(incIndex));
        this.setT(tAllowed.get(tIndex));
        this.setpReal(PRNG.nextDouble());
        this.setfReal(PRNG.nextDouble());
        this.setSatellite(satAllowed.get(satIndex));

    }

    /**
     * Gets the list of possible values of altitudes
     * @return the list of possible values of altitudes
     */
    public List<Double> getAltAllowed() {
        return altAllowed;
    }

    /**
     * Gets the list of possible values of inclinations
     * @return the list of possible values of inclinations
     */
    public List<Object> getIncAllowed() {
        return incAllowed;
    }

    /**
     * Gets the list of possible values of number of satellites
     * @return the list of possible values of number of satellites
     */
    public List<Integer> gettAllowed() {
        return tAllowed;
    }

    /**
     * Gets the list of possible values of number of planes
     * @return the list of possible values of number of planes
     */
    public List<Integer> getpAllowed() { return pAllowed; }

    /**
     * Gets the list of possible satellites
     * @return the list of possible satellites
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
     * Gets the altitude
     * @return the altitude
     */
    public Double getAlt() {
        return alt;
    }

    /**
     * Gets the inclination
     * @return the inclination
     */
    public Object getInc() {
        return inc;
    }

    /**
     * Gets the number of satellites
     * @return the number of satellites
     */
    public Integer getT() {
        return t;
    }

    /**
     * Gets the number of planes real value
     * @return the number of planes real value
     */
    public Double getpReal() {
        return pReal;
    }

    /**
     * Gets the relative spacing real value
     * @return the relative spacing real value
     */
    public Double getfReal() {
        return fReal;
    }

    /**
     * Gets the satellite in the constellation
     * @return the satellite in the constellation
     */
    public Satellite getSatellite() {
        return satellite;
    }

    /**
     * Sets the altitude of the constellation
     * @param alt the new value of altitude
     */
    public void setAlt(Double alt) {
        if (altAllowed.contains(alt)) {
            this.alt = alt;
        } else {
            throw new IllegalArgumentException(String.format("altitude not included in TradespaceSearch.json"));
        }
    }

    /**
     * Sets the inclination of the orbits in the constellation
     * @param inc the new inclination of the orbits in the constellation
     */
    public void setInc(Object inc) {
        if (incAllowed.contains(inc)) {
            this.inc = inc;
        } else {
            throw new IllegalArgumentException(String.format("inclination not included in TradespaceSearch.json"));
        }
    }

    /**
     * Sets the number of satellites in the constellation
     * @param t the new number of satellites in the constellation
     */
    public void setT(Integer t) {
        if (tAllowed.contains(t)) {
            this.t = t;
        } else {
            throw new IllegalArgumentException(String.format("Number of satellites not included in TradespaceSearch.json"));
        }
    }

    /**
     * Sets the number of planes real value
     * @param pReal the new number of planes real value (from 0 to 1)
     */
    public void setpReal(Double pReal) {
        this.pReal = pReal;
    }

    /**
     * Sets the relative spacing real value
     * @param fReal the new relative spacing real value (from 0 to 1)
     */
    public void setfReal(Double fReal) {
        this.fReal = fReal;
    }

    /**
     * Sets the satellite in the constellation
     * @param satellite the new satellite in the constellation
     */
    public void setSatellite(Satellite satellite) {
        this.satellite = satellite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HomogeneousWalkerVariable that = (HomogeneousWalkerVariable) o;
        return Objects.equals(altAllowed, that.altAllowed) &&
                Objects.equals(incAllowed, that.incAllowed) &&
                Objects.equals(tAllowed, that.tAllowed) &&
                Objects.equals(satAllowed, that.satAllowed) &&
                Objects.equals(alt, that.alt) &&
                Objects.equals(inc, that.inc) &&
                Objects.equals(t, that.t) &&
                Objects.equals(pReal, that.pReal) &&
                Objects.equals(fReal, that.fReal) &&
                Objects.equals(satellite, that.satellite);
    }

    @Override
    public int hashCode() {
        return Objects.hash(altAllowed, incAllowed, tAllowed, satAllowed, alt, inc, t, pReal, fReal, satellite);
    }
}
