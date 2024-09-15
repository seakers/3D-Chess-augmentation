package tatc.architecture.variable;

import java.util.List;
import java.util.Objects;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Variable;

/**
 * Variable that contains information about a orbital plane. Used inside heterogeneous variables (a set of walker plane
 * variables with different values of altitude and inclination form a heterogeneous walker constellation).
 */
public class PlaneVariable implements Variable {
    /**
     * List of possible values of altitudes in km
     */
    private final List<Double> altAllowed;

    /**
     * List of possible values of inclination in deg or "SSO"
     */
    private final List<Object> incAllowed;

    /**
     * Altitude value
     */
    private Double alt;

    /**
     * Inclination value
     */
    private Object inc;

    /**
     * Constructs a plane variable. Altitude and inclination are initialized to incorrect values.
     * @param altAllowed the list of possible values of altitudes
     * @param incAllowed the list of possible values of inclinations
     */
    public PlaneVariable(List<Double> altAllowed, List<Object> incAllowed) {
        this.altAllowed = altAllowed;
        this.incAllowed = incAllowed;
        this.alt = -1.0;
        this.inc = null;
    }

    /**
     * Copies the fields of the given plane variable and creates a new instance of a plane.
     *
     * @param var the plane variable to copy
     */
    protected PlaneVariable(PlaneVariable var) {
        this.altAllowed = var.altAllowed;
        this.incAllowed = var.incAllowed;
        this.alt = var.getAlt();
        this.inc = var.getInc();
    }

    /**
     * Gets the altitude
     *
     * @return the altitude
     */
    public Double getAlt() {
        return alt;
    }

    /**
     * Sets the altitude
     *
     * @param alt the altitude
     */
    public void setAlt(Double alt) {
        if (altAllowed.contains(alt)) {
            this.alt = alt;
        } else {
            throw new IllegalArgumentException(String.format("altitude not included in TradespaceSearch.json"));
        }
    }

    /**
     * Gets the inclination
     *
     * @return the inclination
     */
    public Object getInc() {
        return inc;
    }

    /**
     * Sets the inclination
     *
     * @param inc the inclination
     */
    public void setInc(Object inc) {
        if (incAllowed.contains(inc)) {
            this.inc = inc;
        } else {
            throw new IllegalArgumentException(String.format("altitude not included in TradespaceSearch.json"));
        }
    }

    @Override
    public Variable copy() {
        return new PlaneVariable(this);
    }

    @Override
    public void randomize() {
        int indexInclination = PRNG.nextInt(0,incAllowed.size()-1);
        int indexAltitude = PRNG.nextInt(0,altAllowed.size()-1);
        this.setInc(incAllowed.get(indexInclination));
        this.setAlt(altAllowed.get(indexAltitude));
    }

    /**
     * Gets the list of possible altitudes
     * @return the list of possible altitudes
     */
    public List<Double> getAltAllowed() {
        return altAllowed;
    }

    /**
     * Gets the list of possible inclinations
     * @return the list of possible inclinations
     */
    public List<Object> getIncAllowed() {
        return incAllowed;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.alt);
        hash = 89 * hash + Objects.hashCode(this.inc);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PlaneVariable other = (PlaneVariable) obj;
        if (!Objects.equals(this.alt, other.alt)) {
            return false;
        }
        if (!Objects.equals(this.inc, other.inc)) {
            return false;
        }
        return true;
    }

}