package tatc.architecture.specifications;

import com.google.gson.annotations.SerializedName;

/**
 * A passive optical scanner instrument, extending the Instrument class.
 */
public class PassiveOpticalScanner extends Instrument {
    @SerializedName("@type")
    private final String _type = "Passive Optical Scanner";

    // Additional properties
    private final Double focalLength;
    private final Double apertureDia;
    private final Double detectorWidth;
    private final Integer numberOfDetectorsColsCrossTrack;

    // Constructors, getters, and setters

    public PassiveOpticalScanner(String name, String acronym, Agency agency, Double mass, Double volume, Double power,
                                 Orientation orientation, FieldOfView fieldOfView, Double dataRate, Integer techReadinessLevel,
                                 String mountType, Integer bitsPerPixel, Double focalLength, Double apertureDia,
                                 Double detectorWidth, Integer numberOfDetectorsColsCrossTrack) {
        super(name, acronym, agency, mass, volume, power, orientation, fieldOfView, dataRate, techReadinessLevel, mountType, bitsPerPixel);
        this.focalLength = focalLength;
        this.apertureDia = apertureDia;
        this.detectorWidth = detectorWidth;
        this.numberOfDetectorsColsCrossTrack = numberOfDetectorsColsCrossTrack;
    }

    public Double getFocalLength() {
        return focalLength;
    }

    public Double getApertureDia() {
        return apertureDia;
    }

    public Double getDetectorWidth() {
        return detectorWidth;
    }

    public Integer getNumberOfDetectorsColsCrossTrack() {
        return numberOfDetectorsColsCrossTrack;
    }
}
