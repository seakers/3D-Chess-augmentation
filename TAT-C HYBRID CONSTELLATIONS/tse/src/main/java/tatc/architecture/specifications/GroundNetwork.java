package tatc.architecture.specifications;

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A network of ground stations providing uplink and downlink connectivity for a mission.
 */
public class GroundNetwork implements Serializable {

    /**
     * Type tag for JSON file
     */
    @SerializedName("@type")
    private final String _type="GroundNetwork";
    /**
     * Ground network name
     */
    private final String name;
    /**
     * Ground network acronym
     */
    private final String acronym;
    /**
     * Ground network agency
     */
    private final Agency agency;
    /**
     * Number of ground stations inside a ground network as an integer, a list of integers or a quantitative range
     */
    private final Object numberStations;
    /**
     * List of ground stations in the ground network
     */
    private final List<GroundStation> groundStations;
    /**
     * Flag to distinguish between mutable or immutable ground networks. Ground networks in the TSR that are already
     * populated with ground stations are immutable. Ground networks in the TSR which only include the numberStations
     * parameter are mutable and can be created through any possible combination of ground stations available in the TSR.
     * Parameter used by the genetic algorithm. It is declared transient to not appear in the arch.json file.
     */
    private transient boolean mutable;
    /**
     * Ground network id used by the genetic algorithm. It is declared transient to not appear in the arch.json file.
     */
    private transient int id;

    /**
     * Constructs a ground network object
     * @param name the ground network name
     * @param acronym the ground network name
     * @param agency the ground network agency
     * @param numberStations the number of ground stations in the ground network
     * @param groundStations the list of ground stations in the ground network.
     */
    public GroundNetwork(String name, String acronym, Agency agency, Object numberStations, List<GroundStation> groundStations) {
        this.name = name;
        this.acronym = acronym;
        this.agency = agency;
        this.numberStations = numberStations;
        this.groundStations = groundStations;
    }

    /**
     * Gets the name of the ground network
     * @return the name of the ground network
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the acronym of the ground network
     * @return the acronym of the ground network
     */
    public String getAcronym() {
        return acronym;
    }

    /**
     * Gets the agency of the ground network
     * @return the agency of the ground network
     */
    public Agency getAgency() {
        return agency;
    }

    /**
     * Gets the number of ground stations in the ground network
     * @return the number of ground stations in the ground network as an integer, a list of integers or a quantitative range
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("unchecked")
    public Object getNumberStations() throws IllegalArgumentException{
        if (numberStations instanceof Integer) {
            return numberStations;
        }else if (numberStations instanceof Double){
            return ((Double) numberStations).intValue();
        }else if (numberStations instanceof List){
            ArrayList<Integer> nstations=new ArrayList<>();
            for(Double d : (List<Double>)numberStations){
                nstations.add(d.intValue());
            }
            return nstations;
        }else if (numberStations instanceof LinkedTreeMap && ((LinkedTreeMap) numberStations).get("@type").equals("QuantitativeRange")){
            return QuantitativeRange.createQuantitativeRangeFromLinkedTreeMap((LinkedTreeMap)numberStations);
        }else {
            throw new IllegalArgumentException("NumberStations has to be either an Integer or a QuantitativeRange in TradespaceSearch.json");
        }
    }

    /**
     * Gets the class of the numberStations attribute
     * @return the class of the numberStations attribute (Integer, List or QuantitativeRange)
     * @throws IllegalArgumentException
     */
    public Class getNumberStationsType() throws IllegalArgumentException{
        if (numberStations instanceof Integer || numberStations instanceof Double){
            return Integer.class;
        }else if (numberStations instanceof List){
            return List.class;
        }else if (numberStations instanceof LinkedTreeMap && ((LinkedTreeMap) numberStations).get("@type").equals("QuantitativeRange")){
            return QuantitativeRange.class;
        }else {
            throw new IllegalArgumentException("NumberStations has to be either an Integer or a QuantitativeRange in TradespaceSearch.json");
        }
    }

    /**
     * Gets the list of ground stations in the ground network
     * @return the list of ground stations in the ground network
     */
    public List<GroundStation> getGroundStations() {
        return groundStations;
    }

    /**
     * Checks if a ground network is mutable or not
     * @return true if ground network is mutable and false otherwise
     */
    public boolean isMutable() {
        return mutable;
    }

    /**
     * Sets a ground network to be mutable or immutable
     * @param bol true if mutable and fals otherwise
     */
    public void setMutable(boolean bol){
        mutable = bol;
    }

    /**
     * Gets the ground network id
     * @return the ground network id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ground network id
     * @param id the ground network id
     */
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroundNetwork that = (GroundNetwork) o;
        return mutable == that.mutable &&
                Objects.equals(_type, that._type) &&
                Objects.equals(name, that.name) &&
                Objects.equals(acronym, that.acronym) &&
                Objects.equals(agency, that.agency) &&
                numberStations == that.numberStations &&
                id == that.id &&
                Objects.equals(groundStations, that.groundStations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_type, name, acronym, agency, numberStations, groundStations, mutable);
    }
}
