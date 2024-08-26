package tatc.architecture.variable;

import java.util.*;

import org.moeaframework.core.PRNG;
import org.moeaframework.core.Variable;
import tatc.architecture.specifications.Satellite;
import tatc.util.Enumeration;
import tatc.util.Factor;

/**
 * A variable containing the information for a heterogeneous Walker constellation.
 * All planes within this variable have the same bounds on their orbital parameters
 */
public class HeterogeneousWalkerVariable implements Variable {

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
    private final List<Integer> planesAllowed;

    /**
     * List of possible satellite objects
     */
    private final List<Satellite> satsAllowed;

    /**
     * Toggle if this constellation utilizes secondary payloads
     */
    private final Boolean secondaryPayload;

    /**
     * Eccentricity value
     */
    private final Double eccentricity;

    /**
     * List of planes inside this heterogeneous Walker constellation
     */
    private final List<PlaneVariable> planeVars;

    /**
     * Number of satellites in the constellation
     */
    private Integer t;

    /**
     * Real value from 0 to 1 that contains information about the relative spacing (f) in the walker constellation
     */
    private Double fReal;

    /**
     * Satellite in the constellation
     */
    private Satellite satellite;

    /**
     * Constructs a heterogeneous walker variable. The list of plane variables is initialized to an empty list,
     * the number of satellites to -1 (incorrect number) and the satellite to null.
     * @param altAllowed the list of possible values of altitudes
     * @param incAllowed the list of possible values of inclinations
     * @param tAllowed the list of possible values of number of satellites
     * @param planesAllowed the list of possible values of number of planes
     * @param satsAllowed the list of possible values of satellites
     * @param secondaryPayload true if the constellation uses secondary payloads and false otherwise (or null if not specified)
     * @param eccentricity the eccentricity of all the orbits
     */
    public HeterogeneousWalkerVariable(List<Double> altAllowed, List<Object> incAllowed, List<Integer> tAllowed,
                                       List<Integer> planesAllowed, List<Satellite> satsAllowed, Boolean secondaryPayload,
                                       Double eccentricity) {
        this.tAllowed = tAllowed;
        if (planesAllowed!=null){
            this.planesAllowed = planesAllowed;
        }else{
            this.planesAllowed = Factor.divisors(Collections.max(tAllowed));
        }
        this.altAllowed = altAllowed;
        this.incAllowed = incAllowed;
        this.satsAllowed = satsAllowed;
        this.secondaryPayload = secondaryPayload;
        this.eccentricity = eccentricity;
        this.planeVars = new ArrayList<>();
        this.t = -1;
        this.fReal = -1.0;
        this.satellite = null;
    }

    /**
     * Copies the fields of the given constellation variable and creates a new
     * instance of a constellation.
     *
     * @param var the constellation variable to copy
     */
    protected HeterogeneousWalkerVariable(HeterogeneousWalkerVariable var) {
        this(var.altAllowed, var.incAllowed,var.tAllowed, var.planesAllowed, var.satsAllowed, var.secondaryPayload, var.eccentricity);
        for (PlaneVariable p : var.getPlaneVariables()) {
            planeVars.add((PlaneVariable) p.copy());
        }
        this.t = var.getT();
        this.fReal = var.getfReal();
        this.satellite = var.getSatellite();
    }

    /**
     * Creates a template plane variable with the same possible altitudes and inclinations given to this
     * constellation variable
     *
     * @return the plane variable with the same possible altitudes and inclinations given to this constellation variable
     */
    public PlaneVariable createPlaneVariable() {
        return new PlaneVariable(altAllowed, incAllowed);
    }

    @Override
    public Variable copy() {
        return new HeterogeneousWalkerVariable(this);
    }

    @Override
    public void randomize() {
        planeVars.clear();
        int nPlanesIndex = PRNG.nextInt(0, planesAllowed.size()-1);
        int nPlanes = planesAllowed.get(nPlanesIndex);
        for (int i = 0; i < nPlanes; i++) {
            PlaneVariable var = new PlaneVariable(altAllowed, incAllowed);
            var.randomize();
            planeVars.add(var);
        }
        List<Integer> nSats = Enumeration.allowedNumberOfSatellites(nPlanes,tAllowed);
        int nSatsIndex = PRNG.nextInt(0, nSats.size()-1);
        this.t=nSats.get(nSatsIndex);
        this.setfReal(PRNG.nextDouble());
        int satIndex = PRNG.nextInt(0, satsAllowed.size()-1);
        this.satellite=satsAllowed.get(satIndex);

    }

    /**
     * Gets the number of planes on this constellation
     *
     * @return the number of planes in the constellation
     */
    public int getNumberOfPlanes() {
        return planeVars.size();
    }

    /**
     * Gets the plane variables stored within this constellation
     *
     * @return the plane variables stored within this constellation
     */
    public List<PlaneVariable> getPlaneVariables() {
        return planeVars;
    }


    /**
     * Sets the plane variables, number of satellites and satellite object within this constellation. All plane
     * variables must have the same possible altitudes and inclinations as this constellation.
     *
     * @param planes the planes variables to assign to this
     * constellation. Any planes variables that existed previously to this
     * call are cleared out.
     * @param t the number of satellites
     * @param sat the satellite object
     */
    public final void setPlaneVariablesAndTAndSatellite(Collection<PlaneVariable> planes, Integer t, double fReal,Satellite sat) {
        //check that all the bounds are still the same and
        for (PlaneVariable var : planes) {
            if (!(var.getAltAllowed().equals(this.altAllowed)
                    && var.getIncAllowed().equals(this.incAllowed))) {
                throw new IllegalArgumentException(
                        "Given satellites and this constellation have different"
                                + " bounds on the allowable orbital parameters."
                                + " Expected the same bounds.");
            }
        }
        planeVars.clear();
        planeVars.addAll(planes);
        this.t = t;
        this.fReal = fReal;
        this.satellite = sat;
    }

    /**
     * Gets the number of satellites in the constellation
     * @return the number of satellites in the constellation
     */
    public Integer getT() {
        return t;
    }

    /**
     * Gets the relative spacing real value
     * @return the relative spacing real value
     */
    public Double getfReal() {
        return fReal;
    }

    /**
     * Sets the relative spacing real value
     * @param fReal the new relative spacing real value (from 0 to 1)
     */
    public void setfReal(Double fReal) {
        this.fReal = fReal;
    }

    /**
     * Gets the list of possible number of satellites for this constellation
     * @return the list of possible number of satellites for this constellation
     */
    public List<Integer> gettAllowed() {
        return tAllowed;
    }

    /**
     * Gets the list of possible number of planes for this constellation
     * @return the list of possible number of planes for this constellation
     */
    public List<Integer> getPlanesAllowed() {
        return planesAllowed;
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
     * Gets the satellite in the constellation
     * @return the satellite in the constellation
     */
    public Satellite getSatellite() {
        return satellite;
    }

    /**
     * Gets the list of possible satellites
     * @return the list of possible satellites
     */
    public List<Satellite> getSatsAllowed() {
        return satsAllowed;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HeterogeneousWalkerVariable that = (HeterogeneousWalkerVariable) o;
        return Objects.equals(altAllowed, that.altAllowed) &&
                Objects.equals(incAllowed, that.incAllowed) &&
                Objects.equals(tAllowed, that.tAllowed) &&
                Objects.equals(planesAllowed, that.planesAllowed) &&
                Objects.equals(satsAllowed, that.satsAllowed) &&
                Objects.equals(planeVars, that.planeVars) &&
                Objects.equals(t, that.t) &&
                Objects.equals(fReal, that.fReal) &&
                Objects.equals(satellite, that.satellite);
    }

    @Override
    public int hashCode() {
        return Objects.hash(altAllowed, incAllowed, tAllowed, planesAllowed, satsAllowed, planeVars, t, fReal, satellite);
    }
}
