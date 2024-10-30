package tatc.architecture.specifications;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import tatc.util.AlwaysListTypeAdapterFactory;

import java.io.Serializable;
import java.util.List;

/**
 * An entity orbiting the Earth in support of mission objectives.
 */

public class Satellite implements Serializable {
    /**
     * Type tag for JSON file
     */
    @SerializedName("@type")
    private final String _type="Satellite";
    /**
     * Satellite id tag for JSON file
     */
    @SerializedName("@id")
    private String _id;
    /**
     * Satellite name
     */
    private final String name;
    /**
     * Satellite acronym
     */
    private final String acronym;
    /**
     * Satellite agency
     */
    private final Agency agency;
    /**
     * Total mass (kg) including any consumable propellant or gases
     */
    private final Double mass;
    /**
     * Total mass (kg) excluding any consumable propellant or gases
     */
    private final Double dryMass;
    /**
     * Total volume (m^3)
     */
    private final Double volume;
    /**
     * Nominal operating power (Watts)
     */
    private final Double power;
    /**
     * List of communication bands available for broadcast (Recognized values include: VHF, UHF, L, S, C, X, Ku,
     * Ka, Laser)
     */
    private final List<String> commBand;
    /**
     * List of instruments carried on board this satellite
     */
    @JsonAdapter(AlwaysListTypeAdapterFactory.class)
    private List<? super Instrument> payload;
    /**
     * Orbital trajectory of this satellite
     */
    private final Orbit orbit;
    /**
     * Spacecraft technology readiness level
     */
    private final Integer techReadinessLevel;
    /**
     * Command performed by ground station
     */
    private final boolean isGroundCommand;
    /**
     * Spacecraft is a spare
     */
    private final boolean isSpare;
    /**
     * Type of propellant (Recognized values include: COLD_GAS, SOLID, LIQUID_MONO_PROP, LIQUID_BI_PROP, HYBRID,
     * ELECTROTHERMAL, ELECTROSTATIC, MONO_PROP)
     */
    private final String propellantType;
    /**
     * Type of spacecraft stabilization (Recognized values include: AXIS_3, SPINNING, GRAVITY_GRADIENT)
     */
    private final String stabilizationType;


    /**
     * Constructs a satellite object
     * @param name the name of the satellite
     * @param acronym the acronym of the satellite
     * @param agency the agency of the satellite
     * @param mass the mass of the satellite in kg
     * @param dryMass the dry-mass of the satellite in kg
     * @param volume the volume of the satellite in m^3
     * @param power the power of the satellite in Watts
     * @param commBand the list of communication bands
     * @param payload the list of payloads
     * @param orbit the orbit of the satellite
     * @param techReadinessLevel the technology readiness level of the satellite
     * @param isGroundCommand boolean to specify if commands are performed by the ground station
     * @param isSpare boolean to specify if the satellite is a spare
     * @param propellantType type of propellant
     * @param stabilizationType the type of spacecraft stabilization
     */
    public Satellite(String name, String acronym, Agency agency, Double mass, Double dryMass, Double volume, Double power, List<String> commBand, List<? super Instrument> payload, Orbit orbit, Integer techReadinessLevel, boolean isGroundCommand, boolean isSpare, String propellantType, String stabilizationType) {
        this.name = name;
        this.acronym = acronym;
        this.agency = agency;
        this.mass = mass;
        this.dryMass = dryMass;
        this.volume = volume;
        this.power = power;
        this.commBand = commBand;
        this.payload = payload;
        this.orbit = orbit;
        this.techReadinessLevel = techReadinessLevel;
        this.isGroundCommand = isGroundCommand;
        this.isSpare = isSpare;
        this.propellantType = propellantType;
        this.stabilizationType = stabilizationType;
    }

    public Satellite() {
        this.name = null;
        this.acronym = null;
        this.agency = null;
        this.mass = null;
        this.dryMass = null;
        this.volume = null;
        this.power = null;
        this.commBand = null;
        this.payload = null;
        this.orbit = null;
        this.techReadinessLevel = null;
        this.isGroundCommand = false;
        this.isSpare = false;
        this.propellantType = null;
        this.stabilizationType = null;
    }

    /**
     * Gets the satellite id
     * @return the satellite id
     */
    public String get_id() {
        return _id;
    }

    /**
     * Sets a new satellite id
     * @param _id the new satellite id
     */
    public void set_id(String _id) { this._id = _id; }

    /**
     * Gets the satellite name
     * @return the satellite name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the satellite acronym
     * @return the satellite acronym
     */
    public String getAcronym() {
        return acronym;
    }

    /**
     * Gets the satellite agency
     * @return the satellite agency
     */
    public Agency getAgency() {
        return agency;
    }

    /**
     * Gets the satellite total mass
     * @return the satellite total mass
     */
    public double getMass() {
        return mass;
    }

    /**
     * Gets the satellite total volume
     * @return the satellite total volume
     */
    public double getVolume() {
        return volume;
    }

    /**
     * Gets the satellite nominal power
     * @return the satellite nominal power
     */
    public double getPower() {
        return power;
    }

    /**
     * Gets the list of communication bands
     * @return the list of communication bands
     */
    public List<String> getCommBand() {
        return commBand;
    }

    /**
     * Gets the satellite payload
     * @return the satellite payload
     */
    public List<? super Instrument> getPayload() {
        return payload;
    }

    /**
     * Gets the satellite orbit
     * @return the satellite orbit
     */
    public Orbit getOrbit() {
        return orbit;
    }

    /**
     * Gets the satellite dry mass
     * @return the satellite dry mass
     */
    public Double getDryMass() {
        return dryMass;
    }

    /**
     * Gets the satellite technology readiness level
     * @return the satellite readiness level
     */
    public Integer getTechReadinessLevel() {
        return techReadinessLevel;
    }

    /**
     * Check if the satellite is commanded by a ground station
     * @return true if the satellite is commanded by a ground station and false otherwise
     */
    public boolean isGroundCommand() {
        return isGroundCommand;
    }

    /**
     * Checks if the satellite is a spare
     * @return true if the satellite is a spare and false otherwise
     */
    public boolean isSpare() {
        return isSpare;
    }

    /**
     * Gets the type of propellant
     * @return the type of propellant
     */
    public String getPropellantType() {
        return propellantType;
    }

    /**
     * Gets the satellite stabilization type
     * @return the satellite stabilization type
     */
    public String getStabilizationType() {
        return stabilizationType;
    }

    public void setPayload(List<? super Instrument> payload){
        this.payload = payload;
    }

}
