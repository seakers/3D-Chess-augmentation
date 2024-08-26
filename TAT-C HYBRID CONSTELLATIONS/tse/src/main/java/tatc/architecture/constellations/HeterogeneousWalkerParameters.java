package tatc.architecture.constellations;

import tatc.architecture.specifications.Satellite;

import java.util.List;

/**
 * Class that contains the parameters that define a heterogeneous Walker constellation
 */
public class HeterogeneousWalkerParameters extends ConstellationParameters{
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
     * List of Walker planes in the constellation
     */
    private final List<WalkerPlane> planes;
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
     * @param t the number of satellites
     * @param p the number of planes
     * @param f the relative spacing parameter
     * @param planes the list of Walker planes
     * @param satellite the satellite object
     */
    public HeterogeneousWalkerParameters(int t, int p, int f, List<WalkerPlane> planes, Satellite satellite) {
        super();
        this.t = t;
        this.p = p;
        this.f = f;
        this.planes = planes;
        this.satellite=satellite;
        this.type="DELTA_HETEROGENEOUS";
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
     * Gets the list of Walker planes in the constellation
     * @return the list of Walker planes in the constellation
     */
    public List<WalkerPlane> getPlanes() {
        return planes;
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
