package tatc.architecture.specifications;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Specification of fixed and variable quantities for a space mission architecture including both space and ground assets.
 */
public class DesignSpace implements Serializable {

    /**
     * Type tag for JSON file
     */
    @SerializedName("@type")
    private final String _type="DesignSpace";
    /**
     * List of constellation specifications
     */
    private final List<Constellation> spaceSegment;
    /**
     * List of available launch vehicles
     */
    private final List<LaunchVehicle> launchers;
    /**
     * List of available satellites
     */
    private final List<Satellite> satellites;
    /**
     * List of available ground networks / ground network specifications
     */
    private final List<GroundNetwork> groundSegment;
    /**
     * List of available ground stations
     */
    private final List<GroundStation> groundStations;

    /**
     * Constructs a design space object
     * @param spaceSegment the list of constellation specifications
     * @param launchers the list of available launch vehicles
     * @param satellites the list of available satellites
     * @param groundSegment the list of available ground networks / ground network specifications
     * @param groundStations the list of available ground stations
     */
    public DesignSpace(List<Constellation> spaceSegment, List<LaunchVehicle> launchers, List<Satellite> satellites, List<GroundNetwork> groundSegment, List<GroundStation> groundStations) {
        this.spaceSegment = spaceSegment;
        this.launchers = launchers;
        this.satellites = satellites;
        this.groundSegment = groundSegment;
        this.groundStations = groundStations;
    }

    /**
     * Gets the list of constellation specifications
     * @return the list of constellation specifications
     */
    public List<Constellation> getSpaceSegment() {
        return spaceSegment;
    }

    /**
     * Gets the list of available launch vehicles
     * @return the list of available launch vehicles
     */
    public List<LaunchVehicle> getLaunchers() {
        return launchers;
    }

    /**
     * Gets the list of available satellites
     * @return the list of available satellites
     */
    public List<Satellite> getSatellites() {
        return satellites;
    }

    /**
     * Gets the list of available ground networks / ground network specifications
     * @return the list of available ground networks / ground network specifications
     */
    public List<GroundNetwork> getGroundSegment() {
        return groundSegment;
    }

    /**
     * Gets the list of available ground stations
     * @return the list of available ground stations
     */
    public List<GroundStation> getGroundStations() {
        return groundStations;
    }
}
