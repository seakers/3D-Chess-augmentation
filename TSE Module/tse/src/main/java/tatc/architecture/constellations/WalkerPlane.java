package tatc.architecture.constellations;

import java.util.List;
import java.util.Objects;

/**
 * Class that contains all the parameters that define a Walker plane
 */
public class WalkerPlane {
    /**
     * Altitude in km
     */
    private final double a;
    /**
     * Inclination in deg
     */
    private final double i;
    /**
     * Longitude of the ascending node (RAAN) in deg
     */
    private final double raan;
    /**
     * List of true anomalies of the satellites in the Walker plane in deg
     */
    private final List<Double> tas;

    /**
     * Constructs a Walker plane
     * @param a the altitude in km
     * @param i the inclination in deg
     * @param raan the longitude of the ascending node in deg
     * @param tas the list of true anomalies of the satellites in the Walker plane in deg
     */
    public WalkerPlane(double a, double i, double raan, List<Double> tas) {
        this.a = a;
        this.i = i;
        this.raan = raan;
        this.tas = tas;
    }

    /**
     * Gets the altitude in km
     * @return the altitude in km
     */
    public double getA() {
        return a;
    }

    /**
     * Gets the inclination in deg
     * @return the inclination in deg
     */
    public double getI() {
        return i;
    }

    /**
     * Gets the longitude of the ascending node (RAAN) in deg
     * @return the longitude of the ascending node (RAAN) in deg
     */
    public double getRaan() {
        return raan;
    }

    /**
     * Gets the list of true anomalies of the satellites in the Walker plane in deg
     * @return the list of true anomalies of the satellites in the Walker plane in deg
     */
    public List<Double> getTas() {
        return tas;
    }

    /**
     * Gets the number of satellites in the Walker plane
     * @return the number of satellites in the Walker plane
     */
    public int getNumberOfSatellites(){
        return tas.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WalkerPlane that = (WalkerPlane) o;
        return Double.compare(that.a, a) == 0 &&
                Double.compare(that.i, i) == 0 &&
                Double.compare(that.raan, raan) == 0 &&
                Objects.equals(tas, that.tas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, i, raan, tas);
    }
}
