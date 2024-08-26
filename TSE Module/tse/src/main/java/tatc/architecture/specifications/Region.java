package tatc.architecture.specifications;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * A region or point designated by bounding latitudes and longitudes.
 */

public class Region implements Serializable {

    /**
     * Type tag for JSON file
     */
    @SerializedName("@type")
    private final String _type="Region";
    /**
     * Latitude bounds in deg of the region of interest
     */
    private final QuantitativeValue latitude;
    /**
     * Longitude bounds in deg of the region of interest
     */
    private final QuantitativeValue longitude;

    /**
     * Weight or importance of this region, ranging between 0 to inf
     */
    private final double targetWeight;

    /**
     * Constructs a region of interest object
     * @param latitude the latitude bounds in deg
     * @param longitude the longitude bounds in deg
     * @param targetWeight the weight or importance of this region, ranging between 0 to inf
     */
    public Region(QuantitativeValue latitude, QuantitativeValue longitude, double targetWeight) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.targetWeight = targetWeight;
    }

    /**
     * Gets the latitude bounds in deg of the region of interest
     * @return the latitude bounds in deg of the region of interest
     */
    public QuantitativeValue getLatitude() {
        return latitude;
    }

    /**
     * Gets the longitude bounds in deg of the region of interest
     * @return the longitude bounds in deg of the region of interest
     */
    public QuantitativeValue getLongitude() {
        return longitude;
    }

    /**
     * Gets the weight or importance of this region
     * @return the weight or importance of this region
     */
    public double getTargetWeight() {
        return targetWeight;
    }
}
