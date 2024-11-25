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
                // Overwrite with values from instrumentParams if present
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
    // ... handle other fields similarly
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

}
