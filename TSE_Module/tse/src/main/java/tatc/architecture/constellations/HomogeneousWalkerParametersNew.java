package tatc.architecture.constellations;

import java.util.Map;

import tatc.architecture.specifications.Satellite;

/**
 * Class that contains the parameters that define a homogeneous Walker constellation.
 * Now it dynamically assigns variables from the design map.
 */
public class HomogeneousWalkerParametersNew extends ConstellationParameters {
    // Orbital parameters
    private double a = 0; // Altitude in km
    private double i = 0; // Inclination in deg
    private int t = 0;    // Number of satellites in the constellation
    private int p = 0;    // Number of planes in the constellation
    private int f = 0;    // Relative spacing (values from 0 to p-1)
    private Satellite satellite; // Satellite object
    private final String type;

    // Payload parameters
    private double payloadFocalLength = 0;
    private int payloadBitsPerPixel = 0;
    private int payloadNumDetectorsRows = 0;
    private double payloadApertureDia = 0;

    /**
     * Constructs a homogeneous Walker constellation parameters object by assigning variables from the design map.
     *
     * @param params Map containing parameter names and their values.
     */
    public HomogeneousWalkerParametersNew(Map<String, Object> params) {
        //super();
        this.type = "DELTA_HOMOGENEOUS";
        // Assign variables from params
        for (String key : params.keySet()) {
            Object value = params.get(key);
            Satellite s = this.getSatelliteFromValue(value);
            switch (key) {
                case "altitude": // Handle potential typo
                    this.a = ((Number) value).doubleValue();
                    break;
                case "inclination":
                    if (value instanceof String && value.equals("SSO")) {
                        this.i = calculateSSOInclination(this.a);
                    } else {
                        this.i = ((Number) value).doubleValue();
                    }
                    break;
                case "numberSatellites":
                    this.t = ((Number) value).intValue();
                    break;
                case "numberPlanes":
                    this.p = ((Number) value).intValue();
                    break;
                case "relativeSpacing":
                    this.f = ((Number) value).intValue();
                    break;
                case "satellite":
                    this.satellite = getSatelliteFromValue(value);
                    break;
                case "focalLength":
                    this.payloadFocalLength = ((Number) value).doubleValue();
                    break;
                case "bitsPerPixel":
                    this.payloadBitsPerPixel = ((Number) value).intValue();
                    break;
                case "numberOfDetectorsRowsAlongTrack":
                    this.payloadNumDetectorsRows = ((Number) value).intValue();
                    break;
                case "apertureDia":
                    this.payloadApertureDia = ((Number) value).doubleValue();
                    break;
                // Handle other variables as needed
                default:
                    System.err.println("Warning: Unrecognized parameter " + key);
                    break;
            }
        }
    }

    // Calculate SSO inclination based on altitude
    private double calculateSSOInclination(double altitude) {
        // Implement the calculation or use existing method
        // Placeholder implementation:
        // For example, use the formula for SSO inclination
        // i = arccos(- (2 * pi * R_earth^2 * f) / (3 * mu * T^2))
        // For now, return a default value
        return 98.0; // Example value
    }

    // Get Satellite object from value
    private Satellite getSatelliteFromValue(Object value) {
        // Implement method to obtain Satellite object from value
        // Placeholder implementation:
        // If value is a JSONObject representing the satellite, parse it accordingly
        // For now, return a default Satellite object
        return new Satellite(); // Assuming default constructor
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
