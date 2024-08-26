package tatc.architecture.specifications;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Class that contains the specifications of a basic sensor instrument type
 */

public class BasicSensor extends Instrument implements Serializable {
    /**
     * Type tag for JSON file
     */
    @SerializedName("@type")
    private final String _type = "Basic Sensor";

    /**
     * Constructs a basic sensor object
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
     */
    public BasicSensor(String name, String acronym, Agency agency, Double mass, Double volume, Double power, Orientation orientation, FieldOfView fieldOfView, Double dataRate, Integer techReadinessLevel, String mountType, Integer bitsPerPixel) {
        super(name, acronym, agency, mass, volume, power, orientation, fieldOfView, dataRate, techReadinessLevel, mountType, bitsPerPixel);
    }
}
