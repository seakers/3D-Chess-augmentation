package tatc.architecture.specifications;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * An organizational entity responsible for operating a space mission or asset.
 */
public class Agency implements Serializable {

    /**
     * Type tag for JSON file
     */
    @SerializedName("@type")
    private final String _type="Agency";
    /**
     * Agency name
     */
    private final String name;
    /**
     * Agency acronym
     */
    private final String acronym;
    /**
     * Agency type (e.g. ACADEMIC, COMMERCIAL, GOVERNMENT, PRIVATE, etc.)
     */
    private final String agencyType;

    /**
     * Constructs an agency object
     * @param name the agency name
     * @param acronym the agency acronym
     * @param agencyType the agency type
     */
    public Agency(String name, String acronym, String agencyType) {
        this.name = name;
        this.acronym = acronym;
        this.agencyType = agencyType;
    }

    /**
     * Gets the agency name
     * @return the agency name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets agency acronym
     * @return the agency acronym
     */
    public String getAcronym() {
        return acronym;
    }

    /**
     * Gets the agency type
     * @return the agency type
     */
    public String getAgencyType() {
        return agencyType;
    }
}
