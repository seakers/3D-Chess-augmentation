package tatc.architecture.constellations;

import tatc.architecture.specifications.Satellite;

/**
 * Class that contains the parameters that define a homogeneous Walker constellation
 */
public class HomogeneousWalkerParameters extends ConstellationParameters{
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
     * Number of planes in the constellation
     */
    private final int p;
    /**
     * Relative spacing (values from 0 to p-1)
     */
    private final int f;
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
     * @param a the altitude in km of the orbits
     * @param i the inclination in deg of the orbits
     * @param t the number of satellites in the constellation
     * @param p the number of planes in the constellation
     * @param f the relative spacing parameter
     * @param satellite the satellite object
     */
    public HomogeneousWalkerParameters(double a, double i, int t, int p, int f, Satellite satellite) {
        super();
        this.a = a;
        this.i = i;
        this.t = t;
        this.p = p;
        this.f = f;
        this.satellite=satellite;
        this.type="DELTA_HOMOGENEOUS";
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
     * Gets the number of planes in the constellation
     * @return the number of planes in the constellation
     */
    public int getP() {
        return this.p;
    }

    /**
     * Gets the relative spacing parameter
     * @return the relative spacing parameter
     */
    public int getF() {
        return this.f;
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
