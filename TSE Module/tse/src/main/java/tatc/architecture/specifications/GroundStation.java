package tatc.architecture.specifications;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * A surface facility providing uplink or downlink communication services to satellites.
 */
public class GroundStation implements Serializable {

    /**
     * Type tag for JSON file
     */
    @SerializedName("@type")
    private final String _type="GroundStation";
    /**
     * Ground station id tag for JSON file
     */
    @SerializedName("@id")
    private String _id;
    /**
     * Ground station name
     */
    private final String name;
    /**
     * Ground station acronym
     */
    private final String acronym;
    /**
     * Ground station agency
     */
    private final Agency agency;
    /**
     * Ground station latitude in deg
     */
    private final Double latitude;
    /**
     * Ground station longitud in deg
     */
    private final Double longitude;
    /**
     * Ground station elevation angle in deg
     */
    private final Double elevation;
    /**
     * List of operating bands of the ground station (UHF, S, X, Ka, Ku)
     */
    private final List<String> commBand;

    /**
     * Constructs a ground station object
     * @param _id the ground station id
     * @param name the ground station name
     * @param acronym the ground station acronym
     * @param agency the ground station agency
     * @param latitude the ground station latitude in deg
     * @param longitude the ground station longitude in deg
     * @param elevation the ground station elevation angle in deg
     * @param commBand the list of operating bans of the ground station
     */
    public GroundStation(String _id, String name, String acronym, Agency agency, double latitude, double longitude, double elevation, List<String> commBand) {
        this._id = _id;
        this.name = name;
        this.acronym = acronym;
        this.agency = agency;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.commBand = commBand;
    }

    /**
     * Gets the ground station id
     * @return the ground station id
     */
    public String get_id() {
        return _id;
    }

    /**
     * Gets the ground station name
     * @return the ground station name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the ground station acronym
     * @return the ground station acronym
     */
    public String getAcronym() {
        return acronym;
    }

    /**
     * Gets the ground station agency
     * @return the ground station agency
     */
    public Agency getAgency() {
        return agency;
    }

    /**
     * Gets the ground station latitude in deg
     * @return the ground station latitude in deg
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Gets the ground station longitude in deg
     * @return the ground station longitude in deg
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Gets the ground station elevation angle in deg
     * @return the ground station elevation angle in deg
     */
    public double getElevation() {
        return elevation;
    }

    /**
     * Gets the list of operating bands of the ground station
     * @return the list of operating bands of the ground station
     */
    public List<String> getCommBand() {
        return commBand;
    }

    /**
     * Sets the constellation id
     * @param _id the new constellation id
     */
    public void set_id(String _id) {
        this._id = _id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroundStation that = (GroundStation) o;
        return Objects.equals(_type, that._type) &&
                Objects.equals(name, that.name) &&
                Objects.equals(acronym, that.acronym) &&
                Objects.equals(agency, that.agency) &&
                Objects.equals(latitude, that.latitude) &&
                Objects.equals(longitude, that.longitude) &&
                Objects.equals(elevation, that.elevation) &&
                Objects.equals(commBand, that.commBand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_type, name, acronym, agency, latitude, longitude, elevation, commBand);
    }
}
