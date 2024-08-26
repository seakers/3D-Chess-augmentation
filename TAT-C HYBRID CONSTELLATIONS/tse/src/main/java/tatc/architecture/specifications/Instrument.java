package tatc.architecture.specifications;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

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
    private final String name;
    /**
     * Instrument acronym
     */
    private final String acronym;
    /**
     * Instrument agency
     */
    private final Agency agency;
    /**
     * Instrument mass in kg
     */
    private final Double mass;
    /**
     * Instrument volume in m^3
     */
    private final Double volume;
    /**
     * Instrument nominal operating power in Watts
     */
    private final Double power;
    /**
     * Orientation of the instrument wrt Nadir-frame
     */
    private final Orientation orientation;
    /**
     * Instrument field of view specification
     */
    private final FieldOfView fieldOfView;
    /**
     * Instrument data rate during nominal operation in Mbps
     */
    private final Double dataRate;
    /**
     * Technology Readiness Level (TRL)
     */
    private final Integer techReadinessLevel;
    /**
     * Instrument mount type (Accepted values include BODY, MAST and PROBE)
     */
    private final String mountType;
    /**
     * Bits encoded per pixel of image
     */
    private final Integer bitsPerPixel;

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
}
