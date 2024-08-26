package tatc.architecture.specifications;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;
import tatc.util.AlwaysListTypeAdapterFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a set of satellites orbiting in a coordinated motion. Specific types include Homogeneous
 * Walker (Delta), Heterogeneous Walker (Delta), Ad hoc and Train.
 */
public class Constellation implements Serializable {
    /**
     * Type tag for JSON file
     */
    @SerializedName("@type")
    private final String _type="Constellation";
    /**
     * Constellation id tag for JSON file
     */
    @SerializedName("@id")
    private String _id;
    /**
     * Constellation type (DELTA_HOMOGENEOUS, DELTA_HETEROGENEOUS, TRAIN, AD_HOC OR EXISTING)
     */
    private final String constellationType;
    /**
     * Number of satellites in the constellation as an integer, a list of integers or a quantitative range
     */
    private final Object numberSatellites;
    /**
     * Number of planes in the constellation as an integer, a list of integers or a quantitative range. Null when not
     * specified in the TSR
     */
    private final Object numberPlanes;
    /**
     * Relative spacing parameter (f) for DELTA constellations as an integer, a list of integers or a quantitative range.
     * Null when not specified in the TSR. (values from 0 to numberPlanes-1)
     */
    private final Object relativeSpacing;
    /**
     * List of orbit specifications in the constellation
     */
    @JsonAdapter(AlwaysListTypeAdapterFactory.class)
    private final List<Orbit> orbit;
    /**
     * The duration of time between satellites as a string or a list of strings (for train constellation or string of pearls) in ISO 8601
     * duration format
     */
    private final Object satelliteInterval;
    /**
     * List of satellites in the constellation
     */
    private final List<Satellite> satellites;
    /**
     * Toggle if this entity utilizes secondary payloads
     */
    private final Boolean secondaryPayload;

    /**
     * Constructs a constellation object
     * @param constellationType the constellation type (DELTA_HOMOGENEOUS, DELTA_HETEROGENEOUS, TRAIN or AD_HOC)
     * @param numberSatellites the number of satellites
     * @param numberPlanes the number of planes
     * @param relativeSpacing the relative spacing for DELTA constellations
     * @param orbit the orbits of the constellations
     * @param satelliteInterval the duration of time between satellites for train constellation in ISO 8601 duration format
     * @param satellites the satellites in the constellation
     * @param secondaryPayload true if this entity utilizes secondary payloads
     */
    public Constellation(String constellationType, Object numberSatellites, Object numberPlanes, Object relativeSpacing,
                         List<Orbit> orbit, Object satelliteInterval, List<Satellite> satellites, Boolean secondaryPayload) {
        this.constellationType = constellationType;
        this.numberSatellites = numberSatellites;
        this.numberPlanes = numberPlanes;
        this.relativeSpacing = relativeSpacing;
        this.orbit = orbit;
        this.satelliteInterval = satelliteInterval;
        this.satellites = satellites;
        this.secondaryPayload = secondaryPayload;
    }

    /**
     * Gets the constellation id
     * @return the constellation id
     */
    public String get_id() {
        return _id;
    }

    /**
     * Gets the constellation type (DELTA_HOMOGENEOUS, DELTA_HETEROGENEOUS, TRAIN or AD_HOC)
     * @return the constellation type (DELTA_HOMOGENEOUS, DELTA_HETEROGENEOUS, TRAIN or AD_HOC)
     */
    public String getConstellationType() {
        return constellationType;
    }

    /**
     * Gets the number of satellites in the constellation
     * @return the number of satellites in the constellation as an integer, a list of integers or a quantitative range
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("unchecked")
    public Object getNumberSatellites() throws IllegalArgumentException{
        if (numberSatellites instanceof Integer) {
            return numberSatellites;
        }else if (numberSatellites instanceof Double) {
            return ((Double) numberSatellites).intValue();
        }else if (numberSatellites instanceof List){
            ArrayList<Integer> nsat=new ArrayList<>();
            for(Double d : (List<Double>)numberSatellites){
                nsat.add(d.intValue());
            }
            return nsat;
        }else if (numberSatellites instanceof LinkedTreeMap && ((LinkedTreeMap) numberSatellites).get("@type").equals("QuantitativeRange")){
            return QuantitativeRange.createQuantitativeRangeFromLinkedTreeMap((LinkedTreeMap)numberSatellites);
        }else {
            throw new IllegalArgumentException("NumberSatellites has to be either an Integer or a QuantitativeRange in TradespaceSearch.json");
        }
    }

    /**
     * Gets the class of the numberSatellites attribute
     * @return the class of the numberSatellites attribute (Integer, List or QuantitativeRange)
     * @throws IllegalArgumentException
     */
    public Class getNumberSatellitesType() throws IllegalArgumentException{
        if (numberSatellites instanceof Integer || numberSatellites instanceof Double){
            return Integer.class;
        }else if (numberSatellites instanceof List){
            return List.class;
        }else if (numberSatellites instanceof LinkedTreeMap && ((LinkedTreeMap) numberSatellites).get("@type").equals("QuantitativeRange")){
            return QuantitativeRange.class;
        }else {
            throw new IllegalArgumentException("NumberSatellites has to be either an Integer or a QuantitativeRange in TradespaceSearch.json");
        }
    }

    /**
     * Gets the number of planes in the constellation
     * @return the number of planes in the constellation as an integer, a list of integers or a quantitative range. Null
     * when not specified in the TSR
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("unchecked")
    public Object getNumberPlanes() throws IllegalArgumentException{
        if (numberPlanes instanceof Integer) {
            return numberPlanes;
        }else if (numberPlanes instanceof Double) {
            return ((Double) numberPlanes).intValue();
        }else if (numberPlanes instanceof List){
            ArrayList<Integer> nplanes=new ArrayList<>();
            for(Double d : (List<Double>)numberPlanes){
                nplanes.add(d.intValue());
            }
            return nplanes;
        }else if (numberPlanes instanceof LinkedTreeMap && ((LinkedTreeMap) numberPlanes).get("@type").equals("QuantitativeRange")){
            return QuantitativeRange.createQuantitativeRangeFromLinkedTreeMap((LinkedTreeMap)numberPlanes);
        }else if (numberPlanes == null){
            return null;
        }else {
            throw new IllegalArgumentException("NumberPlanes has to be either an Integer or a QuantitativeRange in TradespaceSearch.json");
        }
    }

    /**
     * Gets the class of the numberPlanes attribute
     * @return the class of the numberPlanes attribute (Integer, List, QuantitativeRange or null when not specified in
     * the TSR)
     * @throws IllegalArgumentException
     */
    public Class getNumberPlanesType() throws IllegalArgumentException{
        if (numberPlanes instanceof Integer || numberPlanes instanceof Double){
            return Integer.class;
        }else if (numberPlanes instanceof List){
            return List.class;
        }else if (numberPlanes instanceof LinkedTreeMap && ((LinkedTreeMap) numberPlanes).get("@type").equals("QuantitativeRange")){
            return QuantitativeRange.class;
        }else if (numberPlanes == null){
            return null;
        }else {
            throw new IllegalArgumentException("NumberPlanes has to be either an Integer or a QuantitativeRange in TradespaceSearch.json");
        }
    }

    /**
     * Gets the relative spacing parameter in DELTA constellations
     * @return the relative spacing parameter as an integer, a list of integers or a quantitative range. Null
     * when not specified in the TSR
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("unchecked")
    public Object getRelativeSpacing() throws IllegalArgumentException{
        if (relativeSpacing instanceof Double) {
            return ((Double) relativeSpacing).intValue();
        }else if (relativeSpacing instanceof List){
            ArrayList<Integer> relSpacing=new ArrayList<>();
            for(Double d : (List<Double>)relativeSpacing){
                relSpacing.add(d.intValue());
            }
            return relSpacing;
        }else if (relativeSpacing instanceof LinkedTreeMap && ((LinkedTreeMap) relativeSpacing).get("@type").equals("QuantitativeRange")) {
            return QuantitativeRange.createQuantitativeRangeFromLinkedTreeMap((LinkedTreeMap) relativeSpacing);
        }else if (relativeSpacing == null){
            return null;
        }else {
            throw new IllegalArgumentException("RelativeSpacing has to be either an Integer or a QuantitativeRange in TradespaceSearch.json");
        }
    }

    /**
     * Gets the class of the relativeSpacing attribute
     * @return the class of the numberPlanes attribute (Integer, List, QuantitativeRange or null when not specified in
     * the TSR)
     * @throws IllegalArgumentException
     */
    public Class getRelativeSpacingType() throws IllegalArgumentException{
        if (relativeSpacing instanceof Integer || relativeSpacing instanceof Double){
            return Integer.class;
        }else if (relativeSpacing instanceof List){
            return List.class;
        }else if (relativeSpacing instanceof LinkedTreeMap && ((LinkedTreeMap) relativeSpacing).get("@type").equals("QuantitativeRange")){
            return QuantitativeRange.class;
        }else if (relativeSpacing == null){
            return null;
        }else {
            throw new IllegalArgumentException("RelativeSpacing has to be either an Integer or a QuantitativeRange in TradespaceSearch.json");
        }
    }

    /**
     * Gets the list of orbit specifications
     * @return the list of orbit specifications
     */
    public List<Orbit> getOrbit() {
        return orbit;
    }

    /**
     * Gets the duration of time between satellites for train and string of pearls constellations in ISO 8601 duration format
     * @return the satellite interval parameter for train constellations as a string or a list of strings
     * @throws IllegalArgumentException
     */
    public Object getSatelliteInterval() throws IllegalArgumentException{
        if (satelliteInterval instanceof List){
            return satelliteInterval;
        }else if (satelliteInterval instanceof String){
            return satelliteInterval;
        }else {
            throw new IllegalArgumentException("SatelliteInterval has to be either a double or a String in TradespaceSearch.json");
        }
    }

    /**
     * Gets the class of the satelliteInterval attribute for a train and string of pearls constellations
     * @return the class of the satelliteInterval attribute (String or List)
     * @throws IllegalArgumentException
     */
    public Class getSatelliteIntervalType() throws IllegalArgumentException{
        if (satelliteInterval instanceof String){
            return String.class;
        }else if (satelliteInterval instanceof List){
            return List.class;
        }else {
            throw new IllegalArgumentException("SatelliteInterval has to be either a String or a list of Strings in TradespaceSearch.json");
        }
    }

    /**
     * Gets the list of satellites in the constellation
     * @return the satellites in the constellations
     */
    public List<Satellite> getSatellites() {
        if (satellites == null){
            return new ArrayList<>();
        }
        return satellites;
    }

    /**
     * Checks if the constellation uses secondary payloads
     * @return true if the constellation uses secondary payloads and false otherwise (or null if not specified)
     * @throws IllegalArgumentException
     */
    public Boolean isSecondaryPayload() throws IllegalArgumentException{
        if (secondaryPayload == null){
            return null;
        }else {
            return secondaryPayload;
        }
    }

    /**
     * Sets the constellation id
     * @param _id the new constellation id
     */
    public void set_id(String _id) {
        this._id = _id;
    }
}
