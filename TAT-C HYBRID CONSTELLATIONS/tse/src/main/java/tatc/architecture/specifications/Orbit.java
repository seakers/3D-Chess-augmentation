package tatc.architecture.specifications;


import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

import java.io.Serializable;
import java.util.List;

/**
 * Representation of an orbital trajectory about the Earth as a planetary body. Specific types include: Geosynchronous
 * (remains stationary above one location on the ground), sun synchronous (crosses the equator at the same local solar
 * time each orbit), Keplerian (specific orbital trajectory described by six orbital elements initialized at a
 * particular epoch), and drifting (a generic orbital trajectory which is not synchronized with any reference datum).
 */
public class Orbit implements Serializable {
    /**
     * Constellation id tag for JSON file
     */
    @SerializedName("@type")
    private final String _type="Orbit";
    /**
     * Type of orbit (Recognized values include KEPLERIAN, CIRCULAR, SUN_SYNCHRONOUS)
     */
    private final String orbitType;
    /**
     * Average distance (km) above mean sea level as a double, a list of doubles or a quantitative range
     */
    private final Object altitude;
    /**
     * Average of the distances (km) of periapsis (closest approach) and apoapsis (furthest extent) between the center
     * of masses of a planetary body and a satellite
     */
    private final Double semimajorAxis;
    /**
     * Angle (decimal degrees) of an obrital trajectory with respect to the equatorial plane as a double, String("SS0"),
     * a list of doubles/strings or a quantitative range
     */
    private final Object inclination;
    /**
     * Nondimensional measure of deviation from a circular orbit. Ranges from 0.0 for a circular orbit to 1.0 for a
     * parabolic escape orbit or > 1 for hyperbolic escape orbits
     */
    private final Double eccentricity;
    /**
     * Angle (decimal degrees) between the ascending node (location at which the orbit crosses the equatorial plane
     * moving north) and the periapsis (point of closest extent)
     */
    private final Double periapsisArgument;
    /**
     * Angle (decimal degrees) between the ascending node (location at which the orbit crosses the equatorial plane
     * moving north) and the frame's vernal point (vector between the Sun and the Earth on the vernal equinox)
     */
    private final Object rightAscensionAscendingNode;
    /**
     * Angle (decimal degrees) between the satellite and its argument of periapsis.
     */
    private final Double trueAnomaly;
    /**
     * The initial or reference point in time (ISO 8601) for a set of Keplerian orbital elements
     */
    private final String epoch;
    /**
     * Local time (ISO 8601) as measured by the angle of the sun when crossing the equator in the
     * northerly (ascending) direction as a String or list of Strings.
     */
    private final Object localSolarTimeAscendingNode;

    /**
     * Constructs an orbital trajectory about the Earth
     * @param orbitType the orbit type (KEPLERIAN, CIRCULAR, SUN_SYNCHRONOUS)
     * @param altitude the orbit altitude in km
     * @param semimajorAxis the orbis semi-major axis in km
     * @param inclination the orbit inclination in deg
     * @param eccentricity the orbit eccentricity
     * @param periapsisArgument the periapsis argument in deg
     * @param rightAscensionAscendingNode the RAAN in deg
     * @param trueAnomaly the true anomaly in deg
     * @param epoch the initial or reference point in time (ISO 8601) for a set of Keplerian orbital elements
     * @param localSolarTimeAscendingNode the local solar time of the ascending node in ISO 8601 format
     */
    public Orbit(String orbitType, Object altitude, double semimajorAxis, Object inclination, double eccentricity, double periapsisArgument, Object rightAscensionAscendingNode, double trueAnomaly, String epoch, Object localSolarTimeAscendingNode) {
        this.orbitType = orbitType;
        this.altitude = altitude;
        this.semimajorAxis = semimajorAxis;
        this.inclination = inclination;
        this.eccentricity = eccentricity;
        this.periapsisArgument = periapsisArgument;
        this.rightAscensionAscendingNode = rightAscensionAscendingNode;
        this.trueAnomaly = trueAnomaly;
        this.epoch = epoch;
        this.localSolarTimeAscendingNode = localSolarTimeAscendingNode;
    }

    /**
     * Gets the orbit type
     * @return the orbit type
     */
    public String getOrbitType() {
        return orbitType;
    }

    /**
     * Gets the orbit altitude
     * @return the orbit altitude as a double, a list of doubles or a quantitative range
     * @throws IllegalArgumentException
     */
    public Object getAltitude() throws IllegalArgumentException{
        if (altitude instanceof Double) {
            return altitude;
        }else if (altitude instanceof List){
            return altitude;
        }else if (altitude instanceof LinkedTreeMap && ((LinkedTreeMap) altitude).get("@type").equals("QuantitativeRange")){
            return QuantitativeRange.createQuantitativeRangeFromLinkedTreeMap((LinkedTreeMap)altitude);
        }else {
            throw new IllegalArgumentException("Altitude has to be either a Double or a QuantitativeRange in TradespaceSearch.json");
        }
    }

    /**
     * Gets the class of the altitude attribute
     * @return the class of the altitude attribute (Double, List or QuantitativeRange)
     * @throws IllegalArgumentException
     */
    public Class getAltitudeType() throws IllegalArgumentException{
        if (altitude instanceof Double){
            return Double.class;
        }else if (altitude instanceof List){
            return List.class;
        }else if (altitude instanceof LinkedTreeMap && ((LinkedTreeMap) altitude).get("@type").equals("QuantitativeRange")){
            return QuantitativeRange.class;
        }else {
            throw new IllegalArgumentException("Altitude has to be either a Double or a QuantitativeRange in TradespaceSearch.json");
        }
    }

    /**
     * Gets the semi-major axis
     * @return the semi-major axis
     */
    public double getSemimajorAxis() {
        return semimajorAxis;
    }

    /**
     * Gets the orbit inclination
     * @return the orbit inclination as a double, String("SS0"), a list of doubles/strings or a quantitative range
     * @throws IllegalArgumentException
     */
    public Object getInclination() throws IllegalArgumentException{
        if (inclination instanceof Double){
            return inclination;
        }else if (inclination instanceof String){
            return inclination;
        }else if (inclination instanceof List){
            return inclination;
        }else if (inclination instanceof LinkedTreeMap && ((LinkedTreeMap) inclination).get("@type").equals("QuantitativeRange")){
            return QuantitativeRange.createQuantitativeRangeFromLinkedTreeMap((LinkedTreeMap)inclination);
        }else if (inclination == null && getOrbitType().equalsIgnoreCase("SUN_SYNCHRONOUS")){
            return "SSO";
        }else {
            throw new IllegalArgumentException("Inclination has to be either a Double, a String or a QuantitativeRange in TradespaceSearch.json");
        }
    }

    /**
     * Gets the class of the inclination attribute
     * @return the class of the inclination attribute (Double, String, List or QuantitativeRange)
     * @throws IllegalArgumentException
     */
    public Class getInclinationType() throws IllegalArgumentException{
        if (inclination instanceof Double){
            return Double.class;
        }else if (inclination instanceof String){
            return String.class;
        }else if (inclination instanceof List){
            return List.class;
        }else if (inclination instanceof LinkedTreeMap && ((LinkedTreeMap) inclination).get("@type").equals("QuantitativeRange")){
            return QuantitativeRange.class;
        }else if (inclination == null && getOrbitType().equalsIgnoreCase("SUN_SYNCHRONOUS")){
            return String.class;
        }else {
            throw new IllegalArgumentException("Inclination has to be either a Double, a String or a QuantitativeRange in TradespaceSearch.json");
        }
    }

    /**
     * Gets the orbit eccentricity
     * @return the orbit eccentricity
     */
    public double getEccentricity() {
        return eccentricity;
    }

    /**
     * Gets the periapsis argument
     * @return the periapsis argument
     */
    public double getPeriapsisArgument() {
        return periapsisArgument;
    }

    /**
     * Gets the orbit right ascension of the ascending node
     * @return the orbit right ascension of the ascending node as a double, a list of doubles or a quantitative range
     * @throws IllegalArgumentException
     */
    public Object getRightAscensionAscendingNode() throws IllegalArgumentException{
        if (rightAscensionAscendingNode instanceof Double) {
            return rightAscensionAscendingNode;
        }else if (rightAscensionAscendingNode instanceof List){
            return rightAscensionAscendingNode;
        }else if (rightAscensionAscendingNode instanceof LinkedTreeMap && ((LinkedTreeMap) altitude).get("@type").equals("QuantitativeRange")){
            return QuantitativeRange.createQuantitativeRangeFromLinkedTreeMap((LinkedTreeMap)altitude);
        }else {
            throw new IllegalArgumentException("RAAN has to be either a Double or a QuantitativeRange in TradespaceSearch.json");
        }
    }

    /**
     * Gets the class of the orbit right ascension of the ascending node attribute
     * @return the class of the orbit right ascension of the ascending node attribute (Double, List or QuantitativeRange)
     * @throws IllegalArgumentException
     */
    public Class getRightAscensionAscendingNodeType() throws IllegalArgumentException{
        if (rightAscensionAscendingNode instanceof Double){
            return Double.class;
        }else if (rightAscensionAscendingNode instanceof List){
            return List.class;
        }else if (rightAscensionAscendingNode instanceof LinkedTreeMap && ((LinkedTreeMap) altitude).get("@type").equals("QuantitativeRange")){
            return QuantitativeRange.class;
        }else {
            throw new IllegalArgumentException("RAAN has to be either a Double or a QuantitativeRange in TradespaceSearch.json");
        }
    }

    /**
     * Gets the orbit true anomaly
     * @return the orbit true anomaly
     */
    public double getTrueAnomaly() {
        return trueAnomaly;
    }

    /**
     * Gets the orbit epoch
     * @return the orbit epoch
     */
    public String getEpoch() {
        return epoch;
    }

    /**
     * Gets Local time (ISO 8601) as measured by the angle of the sun when crossing the equator in the northerly
     * (ascending) direction in ISO 8601 duration format
     * @return the local time of the ascending node as a string or a list of strings
     * @throws IllegalArgumentException
     */
    public Object getLocalSolarTimeAscendingNode() throws IllegalArgumentException{
        if (localSolarTimeAscendingNode instanceof String){
            return localSolarTimeAscendingNode;
        }else if (localSolarTimeAscendingNode instanceof List){
            return localSolarTimeAscendingNode;
        }else {
            throw new IllegalArgumentException("localSolarTimeAscendingNode has to be either a String or a List of Strings in TradespaceSearch.json");
        }
    }

    /**
     * Gets the class of the localSolarTimeAscendingNode attribute for a train constellation
     * @return the class of the localSolarTimeAscendingNode attribute (String or List)
     * @throws IllegalArgumentException
     */
    public Class getLocalSolarTimeAscendingNodeType() throws IllegalArgumentException{
        if (localSolarTimeAscendingNode instanceof String){
            return String.class;
        }else if (localSolarTimeAscendingNode instanceof List){
            return List.class;
        }else {
            throw new IllegalArgumentException("localSolarTimeAscendingNode has to be either a String or a List of Strings in TradespaceSearch.json");
        }
    }
}
