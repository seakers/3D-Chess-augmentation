package tatc.architecture.specifications;

import com.google.gson.annotations.SerializedName;

/**
 * A passive optical scanner instrument, extending the Instrument class.
 */
public class PassiveOpticalScanner extends Instrument {
    @SerializedName("@type")
    private final String _type = "Passive Optical Scanner";

    // Additional properties
    private Double focalLength;
    private Double apertureDia;
    private Double detectorWidth;
    private Integer numberOfDetectorsColsCrossTrack;
    private Integer numberOfDetectorsRowsAlongTrack;
    private Integer bitsPerPixel;

    // Parameterized constructor (optional)
    public PassiveOpticalScanner(String name, String acronym, Agency agency, Double mass, Double volume, Double power,
                                 Orientation orientation, FieldOfView fieldOfView, Double dataRate, Integer techReadinessLevel,
                                 String mountType, Integer bitsPerPixel, Double focalLength, Double apertureDia,
                                 Double detectorWidth, Integer numberOfDetectorsColsCrossTrack, Integer numberOfDetectorsRowsAlongTrack) {
                                    super(name, acronym, agency, mass, volume, power, orientation, fieldOfView, dataRate, techReadinessLevel, mountType, bitsPerPixel);
        this.bitsPerPixel = bitsPerPixel;
        this.focalLength = focalLength;
        this.apertureDia = apertureDia;
        this.detectorWidth = detectorWidth;
        this.numberOfDetectorsColsCrossTrack = numberOfDetectorsColsCrossTrack;
        this.numberOfDetectorsRowsAlongTrack = numberOfDetectorsRowsAlongTrack;
    }

    // Getters
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

    public Integer getNumberOfDetectorsRowsAlongTrack() {
        return numberOfDetectorsRowsAlongTrack;
    }

    public Integer getBitsPerPixel() {
        return bitsPerPixel;
    }

    // Setters
    public void setFocalLength(Double focalLength) {
        this.focalLength = focalLength;
    }

    public void setApertureDia(Double apertureDia) {
        this.apertureDia = apertureDia;
    }

    public void setDetectorWidth(Double detectorWidth) {
        this.detectorWidth = detectorWidth;
    }

    public void setNumberOfDetectorsColsCrossTrack(Integer numberOfDetectorsColsCrossTrack) {
        this.numberOfDetectorsColsCrossTrack = numberOfDetectorsColsCrossTrack;
    }

    public void setNumberOfDetectorsRowsAlongTrack(Integer numberOfDetectorsRowsAlongTrack) {
        this.numberOfDetectorsRowsAlongTrack = numberOfDetectorsRowsAlongTrack;
    }

    public void setBitsPerPixel(Integer bitsPerPixel) {
        this.bitsPerPixel = bitsPerPixel;
    }
}
