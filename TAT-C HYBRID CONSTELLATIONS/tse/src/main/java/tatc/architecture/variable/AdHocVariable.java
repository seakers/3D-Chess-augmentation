package tatc.architecture.variable;

import org.moeaframework.core.PRNG;
import org.moeaframework.core.Variable;
import tatc.architecture.specifications.Satellite;

import java.util.List;
import java.util.Objects;

/**
 * A variable containing the parameters of an ad-hoc constellation.
 */
public class AdHocVariable implements Variable {
    /**
     * List of possible number of satellites in the constellation (defined on the design space of the tradespace JSON file)
     */
    private final List<Integer> tAllowed;

    /**
     * List of possible satellites in the constellation (defined on the design space of the tradespace JSON file)
     */
    private final List<Satellite> satAllowed;

    /**
     * Toggle if this constellation utilizes secondary payloads
     */
    private final Boolean secondaryPayload;

    /**
     * Number of satellites in the ad-hoc constellation
     */
    private Integer t;

    /**
     * Satellite in the constellation (all the satellites in the constellation are the same)
     */
    private Satellite satellite;

    /**
     * Constructs an ad-hoc constellation object given the possible number of satellites and satellite objects.
     * The number of satellites and the satellite itself are initialized as -1 (incorrect value) and null, respectively
     * @param tAllowed the list of possible number of satellites in the constellation
     * @param satAllowed the list of possible satellites in the constellation
     * @param secondaryPayload true if the constellation uses secondary payloads and false otherwise (or null if not specified)
     */
    public AdHocVariable(List<Integer> tAllowed, List<Satellite> satAllowed, Boolean secondaryPayload) {
        this.tAllowed = tAllowed;
        this.satAllowed = satAllowed;
        this.secondaryPayload = secondaryPayload;
        this.t = -1;
        this.satellite = null;
    }

    /**
     * Copies the fields of the given constellation variable and creates a new
     * instance of a constellation.
     *
     * @param var the constellation variable to copy
     */
    protected AdHocVariable(AdHocVariable var) {
        this(var.tAllowed,var.satAllowed, var.secondaryPayload);
        this.t = var.getT();
        this.satellite = var.getSatellite();
    }


    @Override
    public Variable copy() {
        return new AdHocVariable(this);
    }

    @Override
    public void randomize() {
        int tIndex = PRNG.nextInt(0, tAllowed.size()-1);
        int satIndex = PRNG.nextInt(0, satAllowed.size()-1);

        this.setT(tAllowed.get(tIndex));
        this.setSatellite(satAllowed.get(satIndex));

    }

    /**
     * Gets the list of possible number of satellites in the constellation
     * @return the list of possible number of satellites in the constellation
     */
    public List<Integer> gettAllowed() {
        return tAllowed;
    }

    /**
     * Gets the list of possible satellites in the constellation
     * @return the list of possible satellites in the constellation
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
     * Gets the number of satellites for this ad-hoc constellation
     * @return the number of satellites
     */
    public Integer getT() {
        return t;
    }

    /**
     * Gets the satellite for this ad-hoc constellation
     * @return the satellite object of this constellation
     */
    public Satellite getSatellite() {
        return satellite;
    }

    /**
     * Sets the number of satellites value
     * @param t the new number of satellites value
     */
    public void setT(Integer t) {
        if (tAllowed.contains(t)) {
            this.t = t;
        } else {
            throw new IllegalArgumentException(String.format("Number of satellites not included in TradespaceSearch.json"));
        }
    }

    /**
     * Sets the satellite object for this constellation
     * @param satellite the new satellite object
     */
    public void setSatellite(Satellite satellite) {
        this.satellite = satellite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdHocVariable that = (AdHocVariable) o;
        return Objects.equals(tAllowed, that.tAllowed) &&
                Objects.equals(satAllowed, that.satAllowed) &&
                Objects.equals(t, that.t) &&
                Objects.equals(satellite, that.satellite);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tAllowed, satAllowed, t, satellite);
    }
}
