package tatc.architecture.specifications;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * An entity that delivers satellites to orbit.
 */

public class LaunchVehicle implements Serializable {
    /**
     * Type tag for JSON file
     */
    @SerializedName("@type")
    private final String _type="LaunchVehicle";
    /**
     * Launch vehicle name
     */
    private final String name;
    /**
     * Launch vehicle acronym
     */
    private final String acronym;
    /**
     * Launch vehicle agency
     */
    private final Agency agency;
    /**
     * Maximum payload mass in kg
     */
    private final Double payloadMass;
    /**
     * Maximum payload volume in m^3
     */
    private final Double payloadVolume;
    /**
     * Launch vehicle dry mass (without any consumable propellants or gases) in kg
     */
    private final Double dryMass;
    /**
     * Maximum propellant mass in kg
     */
    private final Double propellantMass;
    /**
     * Launch vehicle specific impulse (Isp) in s
     */
    private final Double specificImpulse;
    /**
     * Maximum mass in kg delivered by the launcher to Low Earth Orbit
     */
    private final Double massToLEO;
    /**
     * Launch vehicle reliability (probability of success)
     */
    private final Double reliability;
    /**
     * Launch vehicle cost
     */
    private final Double cost;
    /**
     * Average time interval between launches in days or ISO 8601 duration format
     */
    private final Object meanTimeBetweenLaunches;

    /**
     * Constructs a launch vehicle object
     * @param name the launch vehicle name
     * @param acronym the launch vehicle acronym
     * @param agency the launch vehicle agency
     * @param payloadMass the maximum payload mass in kg
     * @param payloadVolume the maximum payload volume in m^3
     * @param dryMass the launch vehicle dry mass
     * @param propellantMass the maximum propellant mass in kg
     * @param specificImpulse the launch vehicle specific impulse in s
     * @param massToLEO the maximum mass in kg delivered to Low Earth Orbit
     * @param reliability the launch vehicle reliability
     * @param cost the launch vehicle cost
     * @param meanTimeBetweenLaunches the average time interval between launches in days or ISO 8601 duration format
     */
    public LaunchVehicle(String name, String acronym, Agency agency, double payloadMass, double payloadVolume, double dryMass, double propellantMass, double specificImpulse, double massToLEO, double reliability, double cost, Object meanTimeBetweenLaunches) {
        this.name = name;
        this.acronym = acronym;
        this.agency = agency;
        this.payloadMass = payloadMass;
        this.payloadVolume = payloadVolume;
        this.dryMass = dryMass;
        this.propellantMass = propellantMass;
        this.specificImpulse = specificImpulse;
        this.massToLEO = massToLEO;
        this.reliability = reliability;
        this.cost = cost;
        this.meanTimeBetweenLaunches = meanTimeBetweenLaunches;
    }

    /**
     * Gets the launch vehicle name
     * @return the launch vehicle name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the launch vehicle acronym
     * @return the launch vehicle acronym
     */
    public String getAcronym() {
        return acronym;
    }

    /**
     * Gets the launch vehicle agency
     * @return the launch vehicle agency
     */
    public Agency getAgency() {
        return agency;
    }

    /**
     * Gets the launch vehicle maximum payload mass in kg
     * @return the launch vehicle maximum payload mass in kg
     */
    public double getPayloadMass() {
        return payloadMass;
    }

    /**
     * Gets the launch vehicle maximum payload volume in m^3
     * @return the launch vehicle maximum payload volume in m^3
     */
    public double getPayloadVolume() {
        return payloadVolume;
    }

    /**
     * Gets the launch vehicle dry mass
     * @return the launch vehicle dry mass
     */
    public double getDryMass() {
        return dryMass;
    }

    /**
     * Gets the launch vehicle maximum propellant mass in kg
     * @return the launch vehicle maximum propellant mass in kg
     */
    public double getPropellantMass() {
        return propellantMass;
    }

    /**
     * Gets the launch vehicle specific impulse in s
     * @return the launch vehicle specific impulse in s
     */
    public double getSpecificImpulse() {
        return specificImpulse;
    }

    /**
     * Gets the launch vehicle maximum mass in kg delivered to Low Earth Orbit
     * @return the launch vehicle maximum mass in kg delivered to Low Earth Orbit
     */
    public double getMassToLEO() {
        return massToLEO;
    }

    /**
     * Gets the launch vehicle reliability
     * @return the launch vehicle reliability
     */
    public double getReliability() {
        return reliability;
    }

    /**
     * Gets the launch vehicle cost
     * @return the launch vehicle cost
     */
    public double getCost() {
        return cost;
    }

    /**
     * Gets the average time interval between launches in days or ISO 8601 duration format
     * @return the average time interval between launches as a double (days) or a string (ISO 8601 duration format)
     * @throws IllegalArgumentException
     */
    public Object getMeanTimeBetweenLaunches() throws IllegalArgumentException{
        if (meanTimeBetweenLaunches instanceof Double){
            return meanTimeBetweenLaunches;
        }else if (meanTimeBetweenLaunches instanceof String){
            return meanTimeBetweenLaunches;
        }else {
            throw new IllegalArgumentException("MeanTimeBetweenLaunches has to be either a double or a String in TradespaceSearch.json");
        }
    }

    /**
     * Gets the class of the MeanTimeBetweenLaunches attribute
     * @return the class of the numberSatellites attribute (Double or String)
     * @throws IllegalArgumentException
     */
    public Class getMeanTimeBetweenLaunchesType() throws IllegalArgumentException {
        if (meanTimeBetweenLaunches instanceof String) {
            return String.class;
        } else if (meanTimeBetweenLaunches instanceof Double) {
            return Double.class;
        } else {
            throw new IllegalArgumentException("MeanTimeBetweenLaunches has to be either a double or a String in TradespaceSearch.json");
        }
    }
}
