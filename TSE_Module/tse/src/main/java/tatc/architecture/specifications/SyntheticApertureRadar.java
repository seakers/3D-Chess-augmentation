package tatc.architecture.specifications;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Class that contains the specifications of a synthetic aperture radar (SAR) type instrument
 */
public class SyntheticApertureRadar extends Instrument implements Serializable {
    /**
     * Type tag for JSON file
     */
    @SerializedName("@type")
    private final String _type="Synthetic Aperture Radar";
    /**
     * Actual pulse width in seconds
     */
    private final Double pulseWidth;
    /**
     * Antenna size in the along-track direction in meters
     */
    private final Double antennaDimensionAlongTrack;
    /**
     * Antenna size in the cross-track direction in meters
     */
    private final Double antennaDimensionCrossTrack;
    /**
     * Aperture efficiency of antenna (0<ηap<1)
     */
    private final Double antennaApertureEfficiency;
    /**
     * Operating radar center frequency in Hz
     */
    private final Double operatingFrequency;
    /**
     * Peak transmit power in Watts
     */
    private final Double peakTransmitPower;
    /**
     * Bandwidth of radar operation in Hz
     */
    private final Double chirpBandwidth;
    /**
     * The minimum pulse-repetition-frequency of operation in Hz
     */
    private final Double minPulseRepetitionFrequency;
    /**
     * The maximum pulse-repetition-frequency of operation in Hz
     */
    private final Double maxPulseRepetitionFrequency;
    /**
     * Nominal scene noise temperature in Kelvin
     */
    private final Double sceneNoiseTemp;
    /**
     * System noise figure for the receiver in dB
     */
    private final Double systemNoiseFigure;
    /**
     * These include a variety of losses primarily over the microwave signal path but doesn’t include the atmosphere in dB
     */
    private final Double radarLosses;
    /**
     * The σNEZ0 threshold for classification as a valid observation in dB
     */
    private final Double thresholdSigmaNEZ0;

    /**
     * Constructs a synthetic aperture radar object
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
     * @param pulseWidth the actual pulse width in seconds
     * @param antennaDimensionAlongTrack the antenna size in the along-track direction in meters
     * @param antennaDimensionCrossTrack the antenna size in the cross-track direction in meters
     * @param antennaApertureEfficiency the aperture efficiency of antenna (from 0 to 1)
     * @param operatingFrequency the operating radar center frequency in Hz
     * @param peakTransmitPower the peak transmit power in Watts
     * @param chirpBandwidth the bandwidth of radar operation in Hz
     * @param minPulseRepetitionFrequency the minimum pulse-repetition-frequency of operation in Hz
     * @param maxPulseRepetitionFrequency the maximum pulse-repetition-frequency of operation in Hz
     * @param sceneNoiseTemp the nominal scene noise temperature in Kelvin
     * @param systemNoiseFigure the system noise figure for the receiver in dB
     * @param radarLosses the path losses in dB
     * @param thresholdSigmaNEZ0 the σNEZ0 threshold for classification as a valid observation in dB
     */
    public SyntheticApertureRadar(String name, String acronym, Agency agency, Double mass, Double volume, Double power, Orientation orientation, FieldOfView fieldOfView, Double dataRate, Integer techReadinessLevel, String mountType, Integer bitsPerPixel,Double pulseWidth, Double antennaDimensionAlongTrack, Double antennaDimensionCrossTrack, Double antennaApertureEfficiency, Double operatingFrequency, Double peakTransmitPower, Double chirpBandwidth, Double minPulseRepetitionFrequency, Double maxPulseRepetitionFrequency, Double sceneNoiseTemp, Double systemNoiseFigure, Double radarLosses, Double thresholdSigmaNEZ0) {
        super(name, acronym, agency, mass, volume, power, orientation, fieldOfView, dataRate, techReadinessLevel, mountType, bitsPerPixel);
        this.pulseWidth = pulseWidth;
        this.antennaDimensionAlongTrack = antennaDimensionAlongTrack;
        this.antennaDimensionCrossTrack = antennaDimensionCrossTrack;
        this.antennaApertureEfficiency = antennaApertureEfficiency;
        this.operatingFrequency = operatingFrequency;
        this.peakTransmitPower = peakTransmitPower;
        this.chirpBandwidth = chirpBandwidth;
        this.minPulseRepetitionFrequency = minPulseRepetitionFrequency;
        this.maxPulseRepetitionFrequency = maxPulseRepetitionFrequency;
        this.sceneNoiseTemp = sceneNoiseTemp;
        this.systemNoiseFigure = systemNoiseFigure;
        this.radarLosses = radarLosses;
        this.thresholdSigmaNEZ0 = thresholdSigmaNEZ0;
    }

    /**
     * Gets the actual pulse width in seconds
     * @return the actual pulse width in seconds
     */
    public Double getPulseWidth() {
        return pulseWidth;
    }

    /**
     * Gets the antenna size in the along-track direction in meters
     * @return the antenna size in the along-track direction in meters
     */
    public Double getAntennaDimensionAlongTrack() {
        return antennaDimensionAlongTrack;
    }

    /**
     * Gets the antenna size in the cross-track direction in meters
     * @return the antenna size in the cross-track direction in meters
     */
    public Double getAntennaDimensionCrossTrack() {
        return antennaDimensionCrossTrack;
    }

    /**
     * Gets the aperture efficiency of antenna
     * @return the aperture efficiency of antenna
     */
    public Double getAntennaApertureEfficiency() {
        return antennaApertureEfficiency;
    }

    /**
     * Gets the operating radar center frequency in Hz
     * @return the operating radar center frequency in Hz
     */
    public Double getOperatingFrequency() {
        return operatingFrequency;
    }

    /**
     * Gets the peak transmit power in Watts
     * @return the peak transmit power in Watts
     */
    public Double getPeakTransmitPower() {
        return peakTransmitPower;
    }

    /**
     * Gets the bandwidth of radar operation in Hz
     * @return the bandwidth of radar operation in Hz
     */
    public Double getChirpBandwidth() {
        return chirpBandwidth;
    }

    /**
     * Gets the minimum pulse-repetition-frequency of operation in Hz
     * @return the minimum pulse-repetition-frequency of operation in Hz
     */
    public Double getMinPulseRepetitionFrequency() {
        return minPulseRepetitionFrequency;
    }

    /**
     * Gets the maximum pulse-repetition-frequency of operation in Hz
     * @return the maximum pulse-repetition-frequency of operation in Hz
     */
    public Double getMaxPulseRepetitionFrequency() {
        return maxPulseRepetitionFrequency;
    }

    /**
     * Gets the nominal scene noise temperature in Kelvin
     * @return the nominal scene noise temperature in Kelvin
     */
    public Double getSceneNoiseTemp() {
        return sceneNoiseTemp;
    }

    /**
     * Gets the system noise figure for the receiver in dB
     * @return the system noise figure for the receiver in dB
     */
    public Double getSystemNoiseFigure() {
        return systemNoiseFigure;
    }

    /**
     * Gets the path losses in dB
     * @return the path losses in dB
     */
    public Double getRadarLosses() {
        return radarLosses;
    }

    /**
     * Gets the σNEZ0 threshold for classification as a valid observation in dB
     * @return the σNEZ0 threshold for classification as a valid observation in dB
     */
    public Double getThresholdSigmaNEZ0() {
        return thresholdSigmaNEZ0;
    }
}
