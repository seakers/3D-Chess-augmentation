package tatc.architecture.specifications;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Instantiation of a space mission composed of a set of constellations and a ground network.
 */
public class Architecture implements Serializable {

    /**
     * Type tag for JSON file
     */
    @SerializedName("@type")
    private final String _type="Architecture";
    /**
     * Architecture id tag for JSON file
     */
    @SerializedName("@id")
    private final String _id;
    /**
     * List of constellations included in the architecture
     */
    private final List<Constellation> spaceSegment;
    /**
     * List of ground networks included in the architecture (as of now, this list will be always of size 1 since
     * there is only one ground network per architecture)
     */
    private final List<GroundNetwork> groundSegment;

    /**
     * Constructs an architecture object
     * @param id the architecture id
     * @param constellations the list of constellations in the architecture
     * @param groundNetworks the list of ground networks in the architecture
     */
    public Architecture(String id, List<Constellation>  constellations, List<GroundNetwork> groundNetworks) {
        this._id=id;
        this.spaceSegment = constellations;
        this.groundSegment = groundNetworks;
    }

    /**
     * Gets the constellations inside the architecture
     * @return the constellations inside the architecture
     */
    public List<Constellation> getSpaceSegment() {
        return spaceSegment;
    }

    /**
     * Gets the ground network inside the architecture
     * @return the ground network inside the architecture
     */
    public List<GroundNetwork> getGroundSegment() {
        return groundSegment;
    }

    /**
     * Gets the architecture id
     * @return the architecture id
     */
    public String get_id() {
        return _id;
    }
}
