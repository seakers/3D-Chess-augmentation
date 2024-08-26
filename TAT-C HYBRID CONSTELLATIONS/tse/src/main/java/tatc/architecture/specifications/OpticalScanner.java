package tatc.architecture.specifications;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import tatc.util.AlwaysListTypeAdapterFactory;

import java.io.Serializable;
import java.util.List;

/**
 * Class that contains the specifications of a Passive Optical Scanner
 */
public class OpticalScanner extends Instrument implements Serializable {
    /**
     * Type tag for JSON file
     */
    @SerializedName("@type")
    private final String _type = "Passive Optical Scanner";
    /**
     * Scanning technique used to compose images (Accepted values are "PUSHBROOM", "WHISKBROOM" or "MATRIX_IMAGER")
     */
    private final String ScanTechnique;
    /**
     * Number of detector rows in along-track direction
     */
    private final Integer numberOfDetectorsRowsAlongTrack;
    /**
     * Number of detector columns in cross-track direction
     */
    private final Integer numberOfDetectorsColsCrossTrack;
    /**
     * F-number/ F# of lens
     */
    private final Double Fnum;
    /**
     * Focal length of lens in meters
     */
    private final Double focalLength;
    /**
     * Aperture diameter in meters
     */
    private final Double apertureDia;
    /**
     * Center operating wavelength in meters
     */
    @JsonAdapter(AlwaysListTypeAdapterFactory.class)
    private final List<Double> operatingWavelength;
    /**
     * Bandwidth of operation in meters
     */
    private final Double bandwidth;
    /**
     * Quantum efficiency of the detector element (0<QE<1)
     */
    private final Double quantumEff;
    /**
     * Optical systems efficiency between 0 and 1
     */
    private final Double opticsSysEff;
    /**
     * Number of read out electrons of detector
     */
    private final Integer numOfReadOutE;
    /**
     * Target equivalent black-body temperature in Kelvin
     */
    private final Double targetBlackBodyTemp;
    /**
     * Width of detector element in meters
     */
    private final Double detectorWidth;
    /**
     * Maximum detector exposure time
     */
    private final Double maxDetectorExposureTime;
    /**
     * Threshold signal-to-noise ratio for valid observation
     */
    private final Double snrThreshold;

    /**
     * Constructs a passive optical scanner object
     * @param name the name of the instrument
     * @param acronym the acronym of the instrument
     * @param agency the agency of the instrument
     * @param mass the mass in kg of the instrument
     * @param volume the volume in m^3 of the instrument
     * @param power the power in Watts of the instrument
     * @param orientation the instrument orientation wrt nadir frame
     * @param fieldOfView the instrument field of view specifications
     * @param dataRate the instrument data rate in Mbps during nominal operations
     * @param techReadinessLevel the instrument Technology Readiness Level (TRL)
     * @param mountType the instrument mount type
     * @param bitsPerPixel the bits encoded per pixel of image
     * @param scanTechnique the scanning technique
     * @param numberOfDetectorsRowsAlongTrack the number of detector rows in along-track direction
     * @param numberOfDetectorsColsCrossTrack the number of detector columns in cross-track direction
     * @param fnum the F-number/ F# of lens
     * @param focalLength the focal length of lens in meters
     * @param apertureDia the aperture diameter in meters
     * @param operatingWavelength the center operating wavelength in meters
     * @param bandwidth the bandwidth of operation in meters
     * @param quantumEff the quantum efficiency of the detector element (from 0 to 1)
     * @param opticsSysEff the optical systems efficiency between 0 and 1
     * @param numOfReadOutE the number of read out electrons of detector
     * @param targetBlackBodyTemp the target equivalent black-body temperature in Kelvin
     * @param detectorWidth the width of detector element in meters
     * @param maxDetectorExposureTime the maximum detector exposure time
     * @param snrThreshold the threshold signal-to-noise ratio for valid observation
     */
    public OpticalScanner(String name, String acronym, Agency agency, Double mass, Double volume, Double power, Orientation orientation, FieldOfView fieldOfView, Double dataRate, Integer techReadinessLevel, String mountType, Integer bitsPerPixel,String scanTechnique, Integer numberOfDetectorsRowsAlongTrack, Integer numberOfDetectorsColsCrossTrack, Double fnum, Double focalLength, Double apertureDia, List<Double> operatingWavelength, Double bandwidth, Double quantumEff, Double opticsSysEff, Integer numOfReadOutE, Double targetBlackBodyTemp, Double detectorWidth, Double maxDetectorExposureTime, Double snrThreshold) {
        super(name, acronym, agency, mass, volume, power, orientation, fieldOfView, dataRate, techReadinessLevel, mountType, bitsPerPixel);
        this.ScanTechnique = scanTechnique;
        this.numberOfDetectorsRowsAlongTrack = numberOfDetectorsRowsAlongTrack;
        this.numberOfDetectorsColsCrossTrack = numberOfDetectorsColsCrossTrack;
        this.Fnum = fnum;
        this.focalLength = focalLength;
        this.apertureDia = apertureDia;
        this.operatingWavelength = operatingWavelength;
        this.bandwidth = bandwidth;
        this.quantumEff = quantumEff;
        this.opticsSysEff = opticsSysEff;
        this.numOfReadOutE = numOfReadOutE;
        this.targetBlackBodyTemp = targetBlackBodyTemp;
        this.detectorWidth = detectorWidth;
        this.maxDetectorExposureTime = maxDetectorExposureTime;
        this.snrThreshold = snrThreshold;
    }

    /**
     * Gets the scanning technique
     * @return the scanning technique
     */
    public String getScanTechnique() {
        return ScanTechnique;
    }

    /**
     * Gets the number of detector rows in along-track direction
     * @return the number of detector rows in along-track direction
     */
    public Integer getNumberOfDetectorsRowsAlongTrack() {
        return numberOfDetectorsRowsAlongTrack;
    }

    /**
     * Gets the number of detector columns in cross-track direction
     * @return the number of detector columns in cross-track direction
     */
    public Integer getNumberOfDetectorsColsCrossTrack() {
        return numberOfDetectorsColsCrossTrack;
    }

    /**
     * Gets the F-number/ F# of lens
     * @return the F-number/ F# of lens
     */
    public Double getFnum() {
        return Fnum;
    }

    /**
     * Gets the focal length of lens in meters
     * @return the focal length of lens in meters
     */
    public Double getFocalLength() {
        return focalLength;
    }

    /**
     * Gets the aperture diameter in meters
     * @return the aperture diameter in meters
     */
    public Double getApertureDia() {
        return apertureDia;
    }

    /**
     * Gets the center operating wavelength in meters
     * @return the center operating wavelength in meters
     */
    public List<Double> getOperatingWavelength() {
        return operatingWavelength;
    }

    /**
     * Gets the bandwidth of operation in meters
     * @return the bandwidth of operation in meters
     */
    public Double getBandwidth() {
        return bandwidth;
    }

    /**
     * Gets the quantum efficiency of the detector element
     * @return the quantum efficiency of the detector element
     */
    public Double getQuantumEff() {
        return quantumEff;
    }

    /**
     * Gets the optical systems efficiency between 0 and 1
     * @return the optical systems efficiency between 0 and 1
     */
    public Double getOpticsSysEff() {
        return opticsSysEff;
    }

    /**
     * Gets the number of read out electrons of detector
     * @return the number of read out electrons of detector
     */
    public Integer getNumOfReadOutE() {
        return numOfReadOutE;
    }

    /**
     * Gets the target equivalent black-body temperature in Kelvin
     * @return the target equivalent black-body temperature in Kelvin
     */
    public Double getTargetBlackBodyTemp() {
        return targetBlackBodyTemp;
    }

    /**
     * Gets the width of detector element in meters
     * @return the width of detector element in meters
     */
    public Double getDetectorWidth() {
        return detectorWidth;
    }

    /**
     * Gets the maximum detector exposure time
     * @return the maximum detector exposure time
     */
    public Double getMaxDetectorExposureTime() {
        return maxDetectorExposureTime;
    }

    /**
     * Gets the threshold signal-to-noise ratio for valid observation
     * @return the threshold signal-to-noise ratio for valid observation
     */
    public Double getSnrThreshold() {
        return snrThreshold;
    }
}