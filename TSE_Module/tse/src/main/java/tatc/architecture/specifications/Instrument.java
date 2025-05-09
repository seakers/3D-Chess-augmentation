package tatc.architecture.specifications;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

/**
 * A payload component that performs scientific observation functions. Examples include imagers which observe and produce one or more image products.
 */
public class Instrument implements Serializable {
    /**
     * Type tag for JSON file
     */
    @SerializedName("@type")
    private final String _type = "Instrument";
    /**
    * Instrument name
    */
    private String name;
    /**
     * Instrument acronym
     */
    private String acronym;
    /**
     * Instrument agency
     */
    private Agency agency;
        /**
         * Instrument mass in kg
         */
    private Double mass;
        /**
             * Instrument volume in m^3
             */
            private Double volume;
/**
             * Instrument nominal operating power in Watts
             */
            private  Double power;
            /**
             * Orientation of the instrument wrt Nadir-frame
             */
            private  Orientation orientation;
            /**
             * Instrument field of view specification
             */
            private  FieldOfView fieldOfView;
            /**
             * Instrument data rate during nominal operation in Mbps
             */
            private  Double dataRate;
            /**
             * Technology Readiness Level (TRL)
             */
            private  Integer techReadinessLevel;
            /**
             * Instrument mount type (Accepted values include BODY, MAST and PROBE)
             */
            private  String mountType;
            /**
             * Bits encoded per pixel of image
             */
            private  Integer bitsPerPixel;
        
            private String id;
            private String scanTechnique;
            private int numberOfDetectorsRowsAlongTrack;
            private int numberOfDetectorsColsCrossTrack;
            private double Fnum;
            private double focalLength;
            private double apertureDia;
            private double operatingWavelength;
            private double bandwidth;
            private double opticsSysEff;
            private double quantumEff;
            private int numOfReadOutE;
            private double targetBlackBodyTemp;
            private double detectorWidth;
            private double maxDetectorExposureTime;
            private int snrThreshold;
            private String type;
            private List<Number> dimensions;
            private List<Number> tempRange;
            private double resolution;
            private double FOV;
            private String specRange;
            private double Nv;  // Number of vertical detectors
            private double Ns;  // Number of horizontal detectors
            private double pv;  // Pixel VNIR size
            private double ps;  // Pixel SWIR size
            private boolean hasTIR; // Whether the instrument has TIR capability
            private static final double EARTH_RADIUS = 6378.137; // Earth radius in km
        
            /**
             * Constructs an instrument object
             * @param name the instrument name
             * @param acronym the instrument acronym
             * @param agency the agency that produced the instrument
             * @param mass the instrument mass in kg
             * @param volume the instrument volume in m^3
             * @param power the instrument nominal power in Watts
             * @param orientation the instrument orientation wrt nadir frame
             * @param fieldOfView the instrument field of view specification
             * @param dataRate the instrument data rate in Mbps
             * @param techReadinessLevel the instrument Technology Readiness Level (TRL)
             * @param mountType the instrument mount type
             * @param bitsPerPixel the bits encoded per pixel of image
             */
            public Instrument(String name, String acronym, Agency agency, Double mass, Double volume, Double power, Orientation orientation, FieldOfView fieldOfView, Double dataRate, Integer techReadinessLevel, String mountType, Integer bitsPerPixel) {
                this.name = name;
                this.acronym = acronym;
                this.agency = agency;
                this.mass = mass;
                this.volume = volume;
                this.power = power;
                this.orientation = orientation;
                this.fieldOfView = fieldOfView;
                this.dataRate = dataRate;
                this.techReadinessLevel = techReadinessLevel;
                this.mountType = mountType;
                this.bitsPerPixel = bitsPerPixel;
                this.Nv = 0.0;  // Initialize with default value
                this.Ns = 0.0;  // Initialize with default value
                this.pv = 0.0;  // Initialize with default value
                this.ps = 0.0;  // Initialize with default value
            }
            // Instrument fields
            
        
            // Constructor that initializes fields from instrumentParams map
            public Instrument(Map<String, Object> instrumentParams) {
                // Initialize final fields with default values
                this.id = "";
                this.name = "";
                this.type = "";
                this.acronym = "";
                this.agency = new Agency("","","");
                this.mass = 0.0;
                this.volume = 0.0;
                this.power = 0.0;
                this.dataRate = 0.0;
                this.bitsPerPixel = 0;
                this.techReadinessLevel = 0;
                this.mountType = "";
                this.scanTechnique = "";
                this.Nv = 0.0;  // Initialize with default value
                this.Ns = 0.0;  // Initialize with default value
                this.pv = 0.0;  // Initialize with default value
                this.ps = 0.0;  // Initialize with default value
                this.hasTIR = false;  // Initialize with default value

                // First, set all parameters from instrumentParams
                if (instrumentParams.containsKey("id")) {
                    this.id = (String) instrumentParams.get("id");
                }
                if (instrumentParams.containsKey("type")) {
                    this.type = (String) instrumentParams.get("type");
                }
                if (instrumentParams.containsKey("name")) {
                    this.name = (String) instrumentParams.get("name");
                }
                if (instrumentParams.containsKey("acronym")) {
                    this.acronym = (String) instrumentParams.get("acronym");
                }
                if (instrumentParams.containsKey("agency")) {
                    this.agency = (Agency) instrumentParams.get("agency");
                }
                if (instrumentParams.containsKey("mass")) {
                    Object value = instrumentParams.get("mass");
                    if (value instanceof Number) {
                        this.mass = ((Number) value).doubleValue();
                    }
                }
                if (instrumentParams.containsKey("volume")) {
                    Object value = instrumentParams.get("volume");
                    if (value instanceof Number) {
                        this.volume = ((Number) value).doubleValue();
                    }
                }
                if (instrumentParams.containsKey("orientation")) {
                    Object value = instrumentParams.get("orientation");
                    if (value instanceof Orientation) {
                        this.orientation = (Orientation) value;
                    }
                }
                if (instrumentParams.containsKey("fieldOfView")) {
                    Object value = instrumentParams.get("fieldOfView");
                    if (value instanceof FieldOfView) {
                        this.fieldOfView = (FieldOfView) value;
                    }
                }
                if (instrumentParams.containsKey("power")) {
                    Object value = instrumentParams.get("power");
                    if (value instanceof Number) {
                        this.power = ((Number) value).doubleValue();
                    }
                }
                if (instrumentParams.containsKey("focalLength")) {
                    Object value = instrumentParams.get("focalLength");
                    if (value instanceof Number) {
                        this.focalLength = ((Number) value).doubleValue();
                    }
                }
                if (instrumentParams.containsKey("apertureDia")) {
                    Object value = instrumentParams.get("apertureDia");
                    if (value instanceof Number) {
                        this.apertureDia = ((Number) value).doubleValue();
                    }
                }
                if (instrumentParams.containsKey("quantumEff")) {
                    Object value = instrumentParams.get("quantumEff");
                    if (value instanceof Number) {
                        this.quantumEff = ((Number) value).doubleValue();
                    }
                }
                if (instrumentParams.containsKey("bandwidth")) {
                    Object value = instrumentParams.get("bandwidth");
                    if (value instanceof Number) {
                        this.bandwidth = ((Number) value).doubleValue();
                    }
                }
                if (instrumentParams.containsKey("targetBlackBodyTemp")) {
                    Object value = instrumentParams.get("targetBlackBodyTemp");
                    if (value instanceof Number) {
                        this.targetBlackBodyTemp = ((Number) value).doubleValue();
                    }
                }
                if (instrumentParams.containsKey("numOfReadOutE")) {
                    Object value = instrumentParams.get("numOfReadOutE");
                    if (value instanceof Number) {
                        this.numOfReadOutE = ((Number) value).intValue();
                    }
                }
                if (instrumentParams.containsKey("detectorWidth")) {
                    Object value = instrumentParams.get("detectorWidth");
                    if (value instanceof Number) {
                        this.detectorWidth = ((Number) value).doubleValue();
                    }
                }
                if (instrumentParams.containsKey("operatingWavelength")) {
                    Object value = instrumentParams.get("operatingWavelength");
                    if (value instanceof Number) {
                        this.operatingWavelength = ((Number) value).doubleValue();
                    }
                }
                if (instrumentParams.containsKey("snrThreshold")) {
                    Object value = instrumentParams.get("snrThreshold");
                    if (value instanceof Number) {
                        this.snrThreshold = ((Number) value).intValue();
                    }
                }
                if (instrumentParams.containsKey("maxDetectorExposureTime")) {
                    Object value = instrumentParams.get("maxDetectorExposureTime");
                    if (value instanceof Number) {
                        this.maxDetectorExposureTime = ((Number) value).doubleValue();
                    }
                }
                if (instrumentParams.containsKey("opticsSysEff")) {
                    Object value = instrumentParams.get("opticsSysEff");
                    if (value instanceof Number) {
                        this.opticsSysEff = ((Number) value).doubleValue();
                    }
                }
                if (instrumentParams.containsKey("Fnum")) {
                    Object value = instrumentParams.get("Fnum");
                    if (value instanceof Number) {
                        this.Fnum = ((Number) value).doubleValue();
                    }
                }
                if (instrumentParams.containsKey("dataRate")) {
                    Object value = instrumentParams.get("dataRate");
                    if (value instanceof Number) {
                        this.dataRate = ((Number) value).doubleValue();
                    }
                }
                if (instrumentParams.containsKey("numberOfDetectorsColsCrossTrack")) {
                    Object value = instrumentParams.get("numberOfDetectorsColsCrossTrack");
                    if (value instanceof Number) {
                        this.numberOfDetectorsColsCrossTrack = ((Number) value).intValue();
                    }
                }
                if (instrumentParams.containsKey("numberOfDetectorsRowsAlongTrack")) {
                    Object value = instrumentParams.get("numberOfDetectorsRowsAlongTrack");
                    if (value instanceof Number) {
                        this.numberOfDetectorsRowsAlongTrack = ((Number) value).intValue();
                    }
                }
                if (instrumentParams.containsKey("bitsPerPixel")) {
                    Object value = instrumentParams.get("bitsPerPixel");
                    if (value instanceof Number) {
                        this.bitsPerPixel = ((Number) value).intValue();
                    }
                }
                if (instrumentParams.containsKey("techReadinessLevel")) {
                    Object value = instrumentParams.get("techReadinessLevel");
                    if (value instanceof Number) {
                        this.techReadinessLevel = ((Number) value).intValue();
                    }
                }
                if (instrumentParams.containsKey("mountType")) {
                    this.mountType = (String) instrumentParams.get("mountType");
                }
                if (instrumentParams.containsKey("scanTechnique")) {
                    this.scanTechnique = (String) instrumentParams.get("scanTechnique");
                }
                if (instrumentParams.containsKey("dimensions")) {
                    Object value = instrumentParams.get("dimensions");
                    this.dimensions = convertToNumberList(value);
                }
                if (instrumentParams.containsKey("temperatureRange")) {
                    Object value = instrumentParams.get("temperatureRange");
                    this.tempRange = convertToNumberList(value);
                }
                if (instrumentParams.containsKey("resolution")) {
                    Object value = instrumentParams.get("resolution");
                    if (value instanceof Number) {
                        this.resolution = ((Number) value).doubleValue();
                    }
                }
                if (instrumentParams.containsKey("FOV")) {
                    Object value = instrumentParams.get("FOV");
                    if (value instanceof Number) {
                        this.FOV = ((Number) value).doubleValue();
                    }
                }
                if (instrumentParams.containsKey("specRange")) {
                    this.specRange = (String) instrumentParams.get("specRange");
                }
                if (instrumentParams.containsKey("Nv")) {
                    Object value = instrumentParams.get("Nv");
                    if (value instanceof Number) {
                        this.Nv = ((Number) value).doubleValue();
                    }
                }
                if (instrumentParams.containsKey("Ns")) {
                    Object value = instrumentParams.get("Ns");
                    if (value instanceof Number) {
                        this.Ns = ((Number) value).doubleValue();
                    }
                }
                if (instrumentParams.containsKey("pv")) {
                    Object value = instrumentParams.get("pv");
                    if (value instanceof Number) {
                        this.pv = ((Number) value).doubleValue();
                    }
                }
                if (instrumentParams.containsKey("ps")) {
                    Object value = instrumentParams.get("ps");
                    if (value instanceof Number) {
                        this.ps = ((Number) value).doubleValue();
                    }
                }
                if (instrumentParams.containsKey("hasTIR")) {
                    Object value = instrumentParams.get("hasTIR");
                    if (value instanceof Boolean) {
                        this.hasTIR = (Boolean) value;
                    }
                }

                // After setting all parameters, check if we can use the paper's methodology
                if (hasAllPaperParameters()) {
                    // Get height and inclination from instrumentParams or use defaults
                    double height = 500.0; // Default height in km
                    double inclination = 98.0; // Default inclination in degrees
                    
                    if (instrumentParams.containsKey("height")) {
                        Object value = instrumentParams.get("height");
                        if (value instanceof Number) {
                            height = ((Number) value).doubleValue();
                        }
                    }
                    if (instrumentParams.containsKey("inclination")) {
                        Object value = instrumentParams.get("inclination");
                        if (value instanceof Number) {
                            inclination = ((Number) value).doubleValue();
                        }
                    }

                    // Calculate advanced parameters
                    calculateAdvancedParameters(height, inclination);
                }
            }
            
private List<Number> convertToNumberList(Object value) {
        List<Number> numberList = new ArrayList<>();
        if (value instanceof List) {
            for (Object item : (List<?>) value) {
                if (item instanceof Number) {
                    numberList.add((Number) item);
                }
            }
        } else if (value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            for (int i = 0; i < array.length(); i++) {
                numberList.add(array.getNumber(i));
            }
        }
        return numberList;
    }
    /**
     * Gets the instrument name
     * @return the instrument name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the instrument acronym
     * @return the instrument acronym
     */
    public String getAcronym() {
        return acronym;
    }

    /**
     * Gets the instrument agency
     * @return the instrument agency
     */
    public Agency getAgency() {
        return agency;
    }

    /**
     * Gets the instrument mass in kg
     * @return the instrument mass in kg
     */
    public Double getMass() {
        return mass;
    }

    /**
     * Gets the instrument volume in m^3
     * @return the instrument volume in m^3
     */
    public Double getVolume() {
        return volume;
    }

    /**
     * Gets the instrument nominal power in Watts
     * @return the instrument nominal power in Watts
     */
    public Double getPower() {
        return power;
    }

    /**
     * Gets the instrument orientation wrt the nadir frame
     * @return the instrument orientation wrt the nadir frame
     */
    public Orientation getOrientation() {
        return orientation;
    }

    /**
     * Gets the field of view specifications
     * @return the field of view specification
     */
    public FieldOfView getFieldOfView() {
        return fieldOfView;
    }

    /**
     * Gets the instrument data rate during nominal operation
     * @return the instrument data rate during nominal operation
     */
    public Double getDataRate() {
        return dataRate;
    }

    /**
     * Gets the instrument Technology Readiness Level (TRL)
     * @return the instrument Technology Readiness Level (TRL)
     */
    public Integer getTechReadinessLevel() {
        return techReadinessLevel;
    }

    /**
     * Gets the instrument mount type
     * @return the instrument mount type
     */
    public String getMountType() {
        return mountType;
    }

    /**
     * Gets the bits encoded per pixel of image
     * @return the bits encoded per pixel of image
     */
    public Integer getBitsPerPixel() {
        return bitsPerPixel;
    }

    /**
 * Gets the instrument ID
 * @return the instrument ID
 */
public String getId() {
    return id;
}

/**
 * Gets the instrument scan technique
 * @return the instrument scan technique
 */
public String getScanTechnique() {
    return scanTechnique;
}

/**
 * Gets the number of vertical detectors
 * @return the number of vertical detectors
 */
public double getNv() {
    return Nv;
}

/**
 * Gets the number of horizontal detectors
 * @return the number of horizontal detectors
 */
public double getNs() {
    return Ns;
}

/**
 * Gets the pixel VNIR size
 * @return the pixel VNIR size
 */
public double getPv() {
    return pv;
}

/**
 * Gets the pixel SWIR size
 * @return the pixel SWIR size
 */
public double getPs() {
    return ps;
}

    private boolean hasAllPaperParameters() {
        return fieldOfView != null && numberOfDetectorsColsCrossTrack != 0 &&
               focalLength != 0 && Nv != 0 && Ns != 0 && apertureDia != 0 && 
               ps != 0;
    }

    private double calculateSpatialPixels(double pixelSize, double focalLength, double fov) {
        double maxPhysicalNx = 8192;
        return Math.min(Math.floor(fov * focalLength / pixelSize), maxPhysicalNx);
    }

    private double calculateVNIRMass(double Nx, double Nv) {
        return 0.363 + (1.40e-6) * Nx * Nv;
    }

    private double calculateSWIRMass(double Nx, double Ns) {
        return 0.618 + (2.26e-5) * Nx * Ns;
    }

    private double calculateTIRMass(boolean hasTIR) {
        return hasTIR ? 13.1 : 0;
    }

    private double calculateLensMass(double focalLength, double apertureDia) {
        return Math.exp(4.365 * focalLength + 2.009 * apertureDia - 2.447);
    }

    private double calculateTotalMass(double m_vnir, double m_swir, double m_tir, double m_lens) {
        return m_vnir + m_swir + m_tir + m_lens;
    }

    private double calculatePower(double Nx, double Nv, double Ns, boolean hasTIR) {
        double basePower = (2.69e-5) * (Nv + Ns) * Nx + 1.14;
        return basePower + (hasTIR ? 200 : 0);
    }
    private double calculateFOV(double focalLength, double p, int Nx) {
        return (Nx * p) / focalLength;

    }

    private double calculateGroundVelocity(double height, double inclination) {
        // Constants
        final double mu = 3.986004418e14; // Earth's gravitational parameter [m^3/s^2]
        final double R_E = EARTH_RADIUS * 1000; // Convert Earth radius to meters
        final double a = (height + EARTH_RADIUS) * 1000; // Semimajor axis in meters
    
        // Orbital velocity (circular orbit)
        double orbitalVelocity = Math.sqrt(mu / a); // [m/s]
    
        // Project orbital velocity onto the ground (avoid zero at 90 deg inclination)
        double projected = orbitalVelocity * Math.cos(Math.toRadians(inclination));
        return Math.max(projected, 1000.0);  // Clamp to a minimum reasonable value
    }
    

    private double computeDeltaX(double height_m, double pixel_size_m, double focal_length_m, 
                               double aperture_m, boolean hasSWIR) {
        double wavelength = hasSWIR ? 2.5e-6 : 1.0e-6;

        // Sampling-limited resolution
        double samplingTerm = (height_m * pixel_size_m) / focal_length_m;

        // Diffraction-limited resolution
        double diffractionTerm = (1.22 * height_m * wavelength) / aperture_m;

        return Math.max(samplingTerm, diffractionTerm);
    }

    private double calculateDataRate(double Nx, double Nv, double Ns, double bitsPerPixel, 
                                   double vg, double delta_x) {
        return ((Nv + Ns) * Nx * bitsPerPixel * vg / delta_x)/8;
    }

    public void calculateAdvancedParameters(double height, double inclination) {
        if (hasAllPaperParameters()) {
            // Convert FOV from degrees to radians
            double theta_p = Math.toRadians(fieldOfView.getCrossTrackFieldOfView());
            double Nx = this.numberOfDetectorsColsCrossTrack;

            double fovRad = calculateFOV(focalLength, ps, numberOfDetectorsColsCrossTrack);
            this.fieldOfView.setCrossTrackFieldOfView(Math.toDegrees(fovRad));
            
            // Calculate spatial pix
            // Calculate masses
            double m_vnir = calculateVNIRMass(Nx, Nv);
            double m_swir = calculateSWIRMass(Nx, Ns);
            double m_tir = calculateTIRMass(hasTIR);
            double m_lens = calculateLensMass(focalLength, apertureDia);
            
            // Calculate total mass
            this.mass = calculateTotalMass(m_vnir, m_swir, m_tir, m_lens);
            
            // Calculate power
            this.power = calculatePower(Nx, Nv, Ns, hasTIR);
            
            // Calculate ground velocity
            double vg = calculateGroundVelocity(height, inclination);
            
            // Calculate delta_x
            double delta_x = computeDeltaX(height * 1000, ps, focalLength, apertureDia, true);
            
            // Calculate data rate
            this.dataRate = calculateDataRate(Nx, Nv, Ns, bitsPerPixel, vg, delta_x)/1e6;
            if (dataRate<1){
                System.out.println("Data rate is less than 1 Mbps, setting to 1 Mbps");
            }
        }
    }

    public boolean isHasTIR() {
        return hasTIR;
    }

    public void setHasTIR(boolean hasTIR) {
        this.hasTIR = hasTIR;
    }
}
