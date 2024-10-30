package tatc.architecture.constellations;

import java.util.List;

import tatc.architecture.specifications.Satellite;

/**
 * Class that contains the parameters that define a homogeneous Walker constellation
 */
public class HomogeneousWalkerParameters extends ConstellationParameters {
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

    // Payload parameters
    private final double payloadFocalLength;
    private final int payloadBitsPerPixel;
    private final int payloadNumDetectorsRows;
    private final double payloadApertureDia;

    /**
     * Constructs a homogeneous Walker constellation parameters object
     *
     * @param a                     the altitude in km of the orbits
     * @param i                     the inclination in deg of the orbits
     * @param t                     the number of satellites in the constellation
     * @param p                     the number of planes in the constellation
     * @param f                     the relative spacing parameter
     * @param satellite             the satellite object
     * @param payloadFocalLength    the payload focal length
     * @param payloadBitsPerPixel   the payload bits per pixel
     * @param payloadNumDetectorsRows the payload number of detectors rows along track
     * @param payloadApertureDia    the payload aperture diameter
     */
    public HomogeneousWalkerParameters(double a, double i, int t, int p, int f, Satellite satellite,
                                       double payloadFocalLength, int payloadBitsPerPixel,
                                       int payloadNumDetectorsRows, double payloadApertureDia) {
        super();
        this.a = a;
        this.i = i;
        this.t = t;
        this.p = p;
        this.f = f;
        this.satellite = satellite;
        this.type = "DELTA_HOMOGENEOUS";
        this.payloadFocalLength = payloadFocalLength;
        this.payloadBitsPerPixel = payloadBitsPerPixel;
        this.payloadNumDetectorsRows = payloadNumDetectorsRows;
        this.payloadApertureDia = payloadApertureDia;
    }

    // Getters for orbital parameters
    public double getA() {
        return this.a;
    }

    public double getI() {
        return this.i;
    }

    public int getT() {
        return this.t;
    }

    public int getP() {
        return this.p;
    }

    public int getF() {
        return this.f;
    }

    public Satellite getSatellite() {
        return this.satellite;
    }

    public String getType() {
        return type;
    }

    // Getters for payload parameters
    public double getPayloadFocalLength() {
        return payloadFocalLength;
    }

    public int getPayloadBitsPerPixel() {
        return payloadBitsPerPixel;
    }

    public int getPayloadNumDetectorsRows() {
        return payloadNumDetectorsRows;
    }

    public double getPayloadApertureDia() {
        return payloadApertureDia;
    }
}
