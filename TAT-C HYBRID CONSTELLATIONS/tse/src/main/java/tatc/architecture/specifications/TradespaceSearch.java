package tatc.architecture.specifications;

import com.google.gson.annotations.SerializedName;
import org.hipparchus.util.FastMath;
import tatc.architecture.variable.Decision;
import tatc.util.Combinatorics;
import tatc.util.Utilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Class containing a set of constraints and parameters to bound and define a tradespace search.
 */
public class TradespaceSearch implements Serializable {
    /**
     * Type tag for JSON file
     */
    @SerializedName("@type")
    private final String _type="TradespaceSearch";
    /**
     * Mission concept
     */
    private final MissionConcept mission;
    /**
     * Design space
     */
    private final DesignSpace designSpace;
    /**
     * Analysis settings
     */
    private final AnalysisSettings settings;

    /**
     * Constructs a tradespace search object
     * @param mission the mission concept
     * @param designSpace the design space
     * @param settings the analysis settings
     */
    public TradespaceSearch(MissionConcept mission, DesignSpace designSpace, AnalysisSettings settings) {
        this.mission = mission;
        this.designSpace = designSpace;
        this.settings = settings;
    }

    /**
     * Gets the mission concept
     * @return the mission concept
     */
    public MissionConcept getMission() {
        return mission;
    }

    /**
     * Gets the design space
     * @return the design space
     */
    public DesignSpace getDesignSpace() {
        return designSpace;
    }

    /**
     * Gets the analysis settings
     * @return the analysis settings
     */
    public AnalysisSettings getSettings() {
        return settings;
    }

    /**
     * Gets the number of "EXISTING" constellations in the design space
     * @return the number of "EXISTING" constellations in the design space
     */
    public int getNumberExistingConstellations(){
        int existingConstellationCount=0;
        for (Constellation c : this.getDesignSpace().getSpaceSegment()){
            if (c.getConstellationType().equalsIgnoreCase("EXISTING")){
                existingConstellationCount++;
            }
        }
        return existingConstellationCount;
    }

    /**
     * This method transforms the design space into a set decisions (identified by a key) and their corresponding
     * allowed values.
     * For DELTA_HOMOGENEOUS and DELTA HETEROGENEOUS, there are the following decision keys: 'altitude#', 'inclination#',
     * 'numberSatellites#', 'numberPlanes#' , 'relativeSpacing#' and 'satellite#'.
     * For TRAIN constellations, there are the following decision keys: 'altitude#', 'numberSatellites#', 'LTAN#' and
     * 'satelliteInterval#' and 'satellite#'.
     * For AD_HOC constellations there are the following decision keys: 'numberSatellites#' and 'satellite#'.
     * For all the constellation decisions, # in 'decision#' is the index of the specific constellation in the design
     * space in the TSR.json.
     * There is an additional decision with key 'groundNetwork' to account for the ground network selection.
     * @return a map of decision keys and decision objects (which contain the allowed values for that specific decision)
     */
    @SuppressWarnings({"Duplicates","unchecked"})
    public HashMap<String,Decision<?>> TradespaceSearch2Decisions(){

        HashMap<String,Decision<?>> decisions = new HashMap<>();

        int constellationCount=0;
        for (Constellation c : this.getDesignSpace().getSpaceSegment()){
            if (!c.getConstellationType().equalsIgnoreCase("EXISTING")){
                Orbit orbitSpecification = c.getOrbit().get(0);
                switch (c.getConstellationType()) {
                    case "DELTA_HOMOGENEOUS":

                        if (orbitSpecification.getAltitudeType()==List.class){
                            decisions.put(String.format("HomoAltitude%d",constellationCount),new Decision<>("Integer",(List<Double>) orbitSpecification.getAltitude()));
                        }else if (orbitSpecification.getAltitudeType()==QuantitativeRange.class){
                            decisions.put(String.format("HomoAltitude%d",constellationCount),new Decision<>("Integer", ((QuantitativeRange)orbitSpecification.getAltitude()).discretize()));
                        }else {
                            // Not a decision
                            List<Double> altitude = new ArrayList<>();
                            altitude.add((Double) orbitSpecification.getAltitude());
                            decisions.put(String.format("HomoAltitude%d",constellationCount),new Decision<>("Integer", altitude));
                        }

                        if (orbitSpecification.getInclinationType()==List.class){
                            decisions.put(String.format("HomoInclination%d",constellationCount),new Decision<>("Integer",(List<Object>) orbitSpecification.getInclination()));
                        }else if (orbitSpecification.getInclinationType()==QuantitativeRange.class){
                            decisions.put(String.format("HomoInclination%d",constellationCount),new Decision<>("Integer", ((QuantitativeRange)orbitSpecification.getInclination()).discretize()));
                        }else {
                            // Not a decision
                            List<Object> inclination = new ArrayList<>();
                            inclination.add(orbitSpecification.getInclination());
                            decisions.put(String.format("HomoInclination%d",constellationCount),new Decision<>("Integer", inclination));
                        }

                        if (c.getNumberSatellitesType()==List.class){
                            decisions.put(String.format("HomoNumberSatellites%d",constellationCount),new Decision<>("Integer",(List<Integer>) c.getNumberSatellites()));
                        }else if (c.getNumberSatellitesType()==QuantitativeRange.class){
                            List <Double> numberSatellitesQuantitativeRange = ((QuantitativeRange)c.getNumberSatellites()).discretize();
                            List <Integer> numberSatellites = new ArrayList<>();
                            for (Double d : numberSatellitesQuantitativeRange){
                                numberSatellites.add(d.intValue());
                            }
                            decisions.put(String.format("HomoNumberSatellites%d",constellationCount),new Decision<>("Integer", numberSatellites));
                        }else {
                            // Not a decision
                            List<Integer> nsat = new ArrayList<>();
                            nsat.add((Integer) c.getNumberSatellites());
                            decisions.put(String.format("HomoNumberSatellites%d",constellationCount),new Decision<>("Integer", nsat));
                        }

                        if (c.getNumberPlanesType()==List.class){
                            decisions.put(String.format("HomoNumberPlanes%d",constellationCount),new Decision<>("Integer",(List<Integer>) c.getNumberPlanes()));
                        }else if (c.getNumberPlanesType()==QuantitativeRange.class){
                            List <Double> numberPlanesQuantitativeRange = ((QuantitativeRange)c.getNumberPlanes()).discretize();
                            List <Integer> numberPlanes = new ArrayList<>();
                            for (Double d : numberPlanesQuantitativeRange){
                                numberPlanes.add(d.intValue());
                            }
                            decisions.put(String.format("HomoNumberPlanes%d",constellationCount),new Decision<>("Integer", numberPlanes));
                        }else if (c.getNumberPlanesType()==null){
                            decisions.put(String.format("HomoNumberPlanes%d",constellationCount),new Decision<>("Integer", null));
                        }else {
                            // Not a decision
                            List<Integer> nplanes = new ArrayList<>();
                            nplanes.add((Integer) c.getNumberPlanes());
                            decisions.put(String.format("HomoNumberPlanes%d",constellationCount),new Decision<>("Integer", nplanes));
                        }

                        if (c.getRelativeSpacingType()==List.class){
                            decisions.put(String.format("HomoRelativeSpacing%d",constellationCount),new Decision<>("Integer",(List<Integer>) c.getRelativeSpacing()));
                        }else if (c.getRelativeSpacingType()==QuantitativeRange.class) {
                            List <Double> relSpacingQuantitativeRange = ((QuantitativeRange)c.getRelativeSpacing()).discretize();
                            List <Integer> relSpacing = new ArrayList<>();
                            for (Double d : relSpacingQuantitativeRange){
                                relSpacing.add(d.intValue());
                            }
                            decisions.put(String.format("HomoRelativeSpacing%d",constellationCount),new Decision<>("Integer", relSpacing));
                        }else if (c.getRelativeSpacingType()==null){
                            decisions.put(String.format("HomoRelativeSpacing%d",constellationCount),new Decision<>("Integer", null));
                        }else {
                            // Not a decision
                            List<Integer> relspac = new ArrayList<>();
                            relspac.add((Integer) c.getRelativeSpacing());
                            decisions.put(String.format("HomoRelativeSpacing%d",constellationCount),new Decision<>("Integer", relspac));
                        }
                        if (!c.getSatellites().isEmpty()){
                            decisions.put(String.format("HomoSatellite%d",constellationCount),new Decision<>("Integer", c.getSatellites()));
                        }else{
                            decisions.put(String.format("HomoSatellite%d",constellationCount),new Decision<>("Integer", this.getDesignSpace().getSatellites()));
                        }
                        constellationCount++;
                        break;

                    case "DELTA_HETEROGENEOUS":
                        if (orbitSpecification.getAltitudeType()==List.class){
                            decisions.put(String.format("HeteroAltitude%d",constellationCount),new Decision<>("Integer",(List<Double>) orbitSpecification.getAltitude()));
                        }else if (orbitSpecification.getAltitudeType()==QuantitativeRange.class){
                            decisions.put(String.format("HeteroAltitude%d",constellationCount),new Decision<>("Integer", ((QuantitativeRange)orbitSpecification.getAltitude()).discretize()));
                        }else {
                            // Not a decision
                            List<Double> altitude = new ArrayList<>();
                            altitude.add((Double) orbitSpecification.getAltitude());
                            decisions.put(String.format("HeteroAltitude%d",constellationCount),new Decision<>("Integer", altitude));
                        }

                        if (orbitSpecification.getInclinationType()==List.class){
                            decisions.put(String.format("HeteroInclination%d",constellationCount),new Decision<>("Integer",(List<Object>) orbitSpecification.getInclination()));
                        }else if (orbitSpecification.getInclinationType()==QuantitativeRange.class){
                            decisions.put(String.format("HeteroInclination%d",constellationCount),new Decision<>("Integer", ((QuantitativeRange)orbitSpecification.getInclination()).discretize()));
                        }else {
                            // Not a decision
                            List<Object> inclination = new ArrayList<>();
                            inclination.add(orbitSpecification.getInclination());
                            decisions.put(String.format("HeteroInclination%d",constellationCount),new Decision<>("Integer", inclination));
                        }

                        if (c.getNumberSatellitesType()==List.class){
                            decisions.put(String.format("HeteroNumberSatellites%d",constellationCount),new Decision<>("Integer",(List<Integer>) c.getNumberSatellites()));
                        }else if (c.getNumberSatellitesType()==QuantitativeRange.class){
                            List <Double> numberSatellitesQuantitativeRange = ((QuantitativeRange)c.getNumberSatellites()).discretize();
                            List <Integer> numberSatellites = new ArrayList<>();
                            for (Double d : numberSatellitesQuantitativeRange){
                                numberSatellites.add(d.intValue());
                            }
                            decisions.put(String.format("HeteroNumberSatellites%d",constellationCount),new Decision<>("Integer", numberSatellites));
                        }else {
                            // Not a decision
                            List<Integer> nsat = new ArrayList<>();
                            nsat.add((Integer) c.getNumberSatellites());
                            decisions.put(String.format("HeteroNumberSatellites%d",constellationCount),new Decision<>("Integer", nsat));
                        }

                        if (c.getNumberPlanesType()==List.class){
                            decisions.put(String.format("HeteroNumberPlanes%d",constellationCount),new Decision<>("Integer",(List<Integer>) c.getNumberPlanes()));
                        }else if (c.getNumberPlanesType()==QuantitativeRange.class){
                            List <Double> numberPlanesQuantitativeRange = ((QuantitativeRange)c.getNumberPlanes()).discretize();
                            List <Integer> numberPlanes = new ArrayList<>();
                            for (Double d : numberPlanesQuantitativeRange){
                                numberPlanes.add(d.intValue());
                            }
                            decisions.put(String.format("HeteroNumberPlanes%d",constellationCount),new Decision<>("Integer", numberPlanes));
                        }else if (c.getNumberPlanesType()==null){
                            decisions.put(String.format("HeteroNumberPlanes%d",constellationCount),new Decision<>("Integer", null));
                        }else {
                            // Not a decision
                            List<Integer> nplanes = new ArrayList<>();
                            nplanes.add((Integer) c.getNumberPlanes());
                            decisions.put(String.format("HeteroNumberPlanes%d",constellationCount),new Decision<>("Integer", nplanes));
                        }

                        if (c.getRelativeSpacingType()==List.class){
                            decisions.put(String.format("HeteroRelativeSpacing%d",constellationCount),new Decision<>("Integer",(List<Integer>) c.getRelativeSpacing()));
                        }else if (c.getRelativeSpacingType()==QuantitativeRange.class) {
                            List <Double> relSpacingQuantitativeRange = ((QuantitativeRange)c.getRelativeSpacing()).discretize();
                            List <Integer> relSpacing = new ArrayList<>();
                            for (Double d : relSpacingQuantitativeRange){
                                relSpacing.add(d.intValue());
                            }
                            decisions.put(String.format("HeteroRelativeSpacing%d",constellationCount),new Decision<>("Integer", relSpacing));
                        }else if (c.getRelativeSpacingType()==null){
                            decisions.put(String.format("HeteroRelativeSpacing%d",constellationCount),new Decision<>("Integer", null));
                        }else {
                            // Not a decision
                            List<Integer> relspac = new ArrayList<>();
                            relspac.add((Integer) c.getRelativeSpacing());
                            decisions.put(String.format("HeteroRelativeSpacing%d",constellationCount),new Decision<>("Integer", relspac));
                        }
                        if (!c.getSatellites().isEmpty()){
                            decisions.put(String.format("HeteroSatellite%d",constellationCount),new Decision<>("Integer", c.getSatellites()));
                        }else{
                            decisions.put(String.format("HeteroSatellite%d",constellationCount),new Decision<>("Integer", this.getDesignSpace().getSatellites()));
                        }
                        constellationCount++;
                        break;
                    case "TRAIN":

                        if (orbitSpecification.getAltitudeType()==List.class){
                            decisions.put(String.format("TrainAltitude%d",constellationCount),new Decision<>("Integer",(List<Double>) orbitSpecification.getAltitude()));
                        }else if (orbitSpecification.getAltitudeType()==QuantitativeRange.class){
                            decisions.put(String.format("TrainAltitude%d",constellationCount),new Decision<>("Integer", ((QuantitativeRange)orbitSpecification.getAltitude()).discretize()));
                        }else {
                            // Not a decision
                            List<Double> altitude = new ArrayList<>();
                            altitude.add((Double) orbitSpecification.getAltitude());
                            decisions.put(String.format("TrainAltitude%d",constellationCount),new Decision<>("Integer", altitude));
                        }

                        if (c.getNumberSatellitesType()==List.class){
                            decisions.put(String.format("TrainNumberSatellites%d",constellationCount),new Decision<>("Integer",(List<Integer>) c.getNumberSatellites()));
                        }else if (c.getNumberSatellitesType()==QuantitativeRange.class){
                            List <Double> numberSatellitesQuantitativeRange = ((QuantitativeRange)c.getNumberSatellites()).discretize();
                            List <Integer> numberSatellites = new ArrayList<>();
                            for (Double d : numberSatellitesQuantitativeRange){
                                numberSatellites.add(d.intValue());
                            }
                            decisions.put(String.format("TrainNumberSatellites%d",constellationCount),new Decision<>("Integer", numberSatellites));
                        }else {
                            // Not a decision
                            List<Integer> nsat = new ArrayList<>();
                            nsat.add((Integer) c.getNumberSatellites());
                            decisions.put(String.format("TrainNumberSatellites%d",constellationCount),new Decision<>("Integer", nsat));
                        }

                        if (orbitSpecification.getLocalSolarTimeAscendingNodeType()==List.class){
                            decisions.put(String.format("TrainLTANs%d",constellationCount),new Decision<>("Integer",(List<String>) orbitSpecification.getLocalSolarTimeAscendingNode()));
                        }else if (orbitSpecification.getLocalSolarTimeAscendingNodeType()==String.class) {
                            // Not a decision
                            List<String> LTANs = new ArrayList<>();
                            LTANs.add((String) orbitSpecification.getLocalSolarTimeAscendingNode());
                            decisions.put(String.format("TrainLTANs%d",constellationCount),new Decision<>("Integer", LTANs));
                        }else {
                            throw new IllegalArgumentException("SatelliteInterval has to be either a String or a list of Strings in TradespaceSearch.json");
                        }

                        if (c.getSatelliteIntervalType()==List.class){
                            decisions.put(String.format("TrainSatelliteInterval%d",constellationCount),new Decision<>("Integer",(List<String>) c.getSatelliteInterval()));
                        }else if (c.getSatelliteIntervalType()==String.class) {
                            // Not a decision
                            List<String> satelliteInterval = new ArrayList<>();
                            satelliteInterval.add((String) c.getSatelliteInterval());
                            decisions.put(String.format("TrainSatelliteInterval%d",constellationCount),new Decision<>("Integer", satelliteInterval));
                        }else {
                            throw new IllegalArgumentException("SatelliteInterval has to be either a String or a list of Strings in TradespaceSearch.json");
                        }
                        if (!c.getSatellites().isEmpty()){
                            decisions.put(String.format("TrainSatellite%d",constellationCount),new Decision<>("Integer", c.getSatellites()));
                        }else{
                            decisions.put(String.format("TrainSatellite%d",constellationCount),new Decision<>("Integer", this.getDesignSpace().getSatellites()));
                        }
                        constellationCount++;
                        break;

                    case "PRECESSING":
                        //TODO: To be implemented
                        break;
                    case "AD_HOC":

                        if (c.getNumberSatellitesType()==List.class){
                            decisions.put(String.format("AdhocNumberSatellites%d",constellationCount),new Decision<>("Integer",(List<Integer>) c.getNumberSatellites()));
                        }else if (c.getNumberSatellitesType()==QuantitativeRange.class){
                            List <Double> numberSatellitesQuantitativeRange = ((QuantitativeRange)c.getNumberSatellites()).discretize();
                            List <Integer> numberSatellites = new ArrayList<>();
                            for (Double d : numberSatellitesQuantitativeRange){
                                numberSatellites.add(d.intValue());
                            }
                            decisions.put(String.format("AdhocNumberSatellites%d",constellationCount),new Decision<>("Integer", numberSatellites));
                        }else {
                            // Not a decision
                            List<Integer> nsat = new ArrayList<>();
                            nsat.add((Integer) c.getNumberSatellites());
                            decisions.put(String.format("AdhocNumberSatellites%d",constellationCount),new Decision<>("Integer", nsat));
                        }

                        if (!c.getSatellites().isEmpty()){
                            decisions.put(String.format("AdhocSatellite%d",constellationCount),new Decision<>("Integer", c.getSatellites()));
                        }else{
                            decisions.put(String.format("AdhocSatellite%d",constellationCount),new Decision<>("Integer", this.getDesignSpace().getSatellites()));
                        }
                        constellationCount++;
                        break;

                    case "STRING_OF_PEARLS":

                        if (orbitSpecification.getAltitudeType()==List.class){
                            decisions.put(String.format("PearlAltitude%d",constellationCount),new Decision<>("Integer",(List<Double>) orbitSpecification.getAltitude()));
                        }else if (orbitSpecification.getAltitudeType()==QuantitativeRange.class){
                            decisions.put(String.format("PearlAltitude%d",constellationCount),new Decision<>("Integer", ((QuantitativeRange)orbitSpecification.getAltitude()).discretize()));
                        }else {
                            // Not a decision
                            List<Double> altitude = new ArrayList<>();
                            altitude.add((Double) orbitSpecification.getAltitude());
                            decisions.put(String.format("PearlAltitude%d",constellationCount),new Decision<>("Integer", altitude));
                        }

                        if (orbitSpecification.getInclinationType()==List.class){
                            decisions.put(String.format("PearlInclination%d",constellationCount),new Decision<>("Integer",(List<Object>) orbitSpecification.getInclination()));
                        }else if (orbitSpecification.getInclinationType()==QuantitativeRange.class){
                            decisions.put(String.format("PearlInclination%d",constellationCount),new Decision<>("Integer", ((QuantitativeRange)orbitSpecification.getInclination()).discretize()));
                        }else {
                            // Not a decision
                            List<Object> inclination = new ArrayList<>();
                            inclination.add(orbitSpecification.getInclination());
                            decisions.put(String.format("PearlInclination%d",constellationCount),new Decision<>("Integer", inclination));
                        }

                        if (orbitSpecification.getRightAscensionAscendingNodeType()==List.class){
                            decisions.put(String.format("PearlRAAN%d",constellationCount),new Decision<>("Integer",(List<Double>) orbitSpecification.getRightAscensionAscendingNode()));
                        }else if (orbitSpecification.getRightAscensionAscendingNodeType()==QuantitativeRange.class){
                            decisions.put(String.format("PearlRAAN%d",constellationCount),new Decision<>("Integer", ((QuantitativeRange)orbitSpecification.getRightAscensionAscendingNode()).discretize()));
                        }else {
                            // Not a decision
                            List<Double> raan = new ArrayList<>();
                            raan.add((Double) orbitSpecification.getRightAscensionAscendingNode());
                            decisions.put(String.format("PearlRAAN%d",constellationCount),new Decision<>("Integer", raan));
                        }

                        if (c.getNumberSatellitesType()==List.class){
                            decisions.put(String.format("PearlNumberSatellites%d",constellationCount),new Decision<>("Integer",(List<Integer>) c.getNumberSatellites()));
                        }else if (c.getNumberSatellitesType()==QuantitativeRange.class){
                            List <Double> numberSatellitesQuantitativeRange = ((QuantitativeRange)c.getNumberSatellites()).discretize();
                            List <Integer> numberSatellites = new ArrayList<>();
                            for (Double d : numberSatellitesQuantitativeRange){
                                numberSatellites.add(d.intValue());
                            }
                            decisions.put(String.format("PearlNumberSatellites%d",constellationCount),new Decision<>("Integer", numberSatellites));
                        }else {
                            // Not a decision
                            List<Integer> nsat = new ArrayList<>();
                            nsat.add((Integer) c.getNumberSatellites());
                            decisions.put(String.format("PearlNumberSatellites%d",constellationCount),new Decision<>("Integer", nsat));
                        }

                        if (c.getSatelliteIntervalType()==List.class){
                            decisions.put(String.format("PearlSatelliteInterval%d",constellationCount),new Decision<>("Integer",(List<String>) c.getSatelliteInterval()));
                        }else if (c.getSatelliteIntervalType()==String.class) {
                            // Not a decision
                            List<String> satelliteInterval = new ArrayList<>();
                            satelliteInterval.add((String) c.getSatelliteInterval());
                            decisions.put(String.format("PearlSatelliteInterval%d",constellationCount),new Decision<>("Integer", satelliteInterval));
                        }else {
                            throw new IllegalArgumentException("SatelliteInterval has to be either a String or a list of Strings in TradespaceSearch.json");
                        }

                        if (!c.getSatellites().isEmpty()){
                            decisions.put(String.format("PearlSatellite%d",constellationCount),new Decision<>("Integer", c.getSatellites()));
                        }else{
                            decisions.put(String.format("PearlSatellite%d",constellationCount),new Decision<>("Integer", this.getDesignSpace().getSatellites()));
                        }
                        constellationCount++;
                        break;

                    case "GENERAL":
                        Double hmax = -1.0;
                        if (orbitSpecification.getAltitudeType()==List.class){
                            List<Double> altitudeBounds = new ArrayList<>();
                            List<Double> altitudes = (List<Double>) orbitSpecification.getAltitude();
                            altitudeBounds.add((Collections.min(altitudes)+Utilities.EARTH_RADIUS_KM)*1000);
                            altitudeBounds.add((Collections.max(altitudes)+Utilities.EARTH_RADIUS_KM)*1000);
                            decisions.put(String.format("altitudeBounds%d",constellationCount),new Decision<>("Integer",altitudeBounds));
                            hmax = Collections.max(altitudes);
                        }else if (orbitSpecification.getAltitudeType()==QuantitativeRange.class){
                            List<Double> altitudeBounds = new ArrayList<>();
                            List<Double> altitudes = ((QuantitativeRange)orbitSpecification.getAltitude()).discretize();
                            altitudeBounds.add((Collections.min(altitudes)+Utilities.EARTH_RADIUS_KM)*1000);
                            altitudeBounds.add((Collections.max(altitudes)+Utilities.EARTH_RADIUS_KM)*1000);
                            decisions.put(String.format("altitudeBounds%d",constellationCount),new Decision<>("Integer", altitudeBounds));
                            hmax = Collections.max(altitudes);
                        }else {
                            // Not a decision
                            List<Double> altitudeBounds = new ArrayList<>();
                            altitudeBounds.add(((Double) orbitSpecification.getAltitude()+Utilities.EARTH_RADIUS_KM)*1000);
                            altitudeBounds.add(((Double) orbitSpecification.getAltitude()+Utilities.EARTH_RADIUS_KM)*1000);
                            decisions.put(String.format("altitudeBounds%d",constellationCount),new Decision<>("Integer", altitudeBounds));
                            hmax = (Double) orbitSpecification.getAltitude();
                        }

                        if (orbitSpecification.getInclinationType()==List.class){
                            List<Double> inclinationBounds = new ArrayList<>();
                            List<Object> inclinationObjects = (List<Object>) orbitSpecification.getInclination();
                            List<Double> inclinations = new ArrayList<>();
                            for (Object inc : inclinationObjects){
                                if (inc instanceof Double){
                                    inclinations.add(FastMath.toRadians((Double) inc));
                                }else if (inc instanceof String && inc.equals("SSO")){
                                    inclinations.add(Utilities.incSSO(hmax*1000));
                                }
                            }
                            inclinationBounds.add(Collections.min(inclinations));
                            inclinationBounds.add(Collections.max(inclinations));
                            decisions.put(String.format("inclinationBounds%d",constellationCount),new Decision<>("Integer",inclinationBounds));
                        }else if (orbitSpecification.getInclinationType()==QuantitativeRange.class){
                            List<Double> inclinationBounds = new ArrayList<>();
                            List<Double> inclinations = ((QuantitativeRange)orbitSpecification.getAltitude()).discretize();
                            inclinationBounds.add(FastMath.toRadians(Collections.min(inclinations)));
                            inclinationBounds.add(FastMath.toRadians(Collections.max(inclinations)));
                            decisions.put(String.format("inclinationBounds%d",constellationCount),new Decision<>("Integer", inclinationBounds));
                        }else {
                            // Not a decision
                            List<Double> inclinationBounds = new ArrayList<>();
                            Object inc = orbitSpecification.getInclination();
                            if (inc instanceof Double){
                                inclinationBounds.add(FastMath.toRadians((Double) inc));
                                inclinationBounds.add(FastMath.toRadians((Double) inc));
                            }else if (inc instanceof String && inc.equals("SSO")){
                                inclinationBounds.add(Utilities.incSSO(hmax*1000));
                                inclinationBounds.add(Utilities.incSSO(hmax*1000));
                            }
                            decisions.put(String.format("inclinationBounds%d",constellationCount),new Decision<>("Integer", inclinationBounds));
                        }

                        if (c.getNumberSatellitesType()==List.class){
                            List<Integer> nSatBounds = new ArrayList<>();
                            List<Integer> nSats = (List<Integer>) c.getNumberSatellites();
                            nSatBounds.add(Collections.min(nSats));
                            nSatBounds.add(Collections.max(nSats));
                            decisions.put(String.format("nSatBounds%d",constellationCount),new Decision<>("Integer",nSatBounds));
                        }else if (c.getNumberSatellitesType()==QuantitativeRange.class){
                            List<Integer> nSatBounds = new ArrayList<>();
                            List <Double> numberSatellitesQuantitativeRange = ((QuantitativeRange)c.getNumberSatellites()).discretize();
                            List <Integer> nSats = new ArrayList<>();
                            for (Double d : numberSatellitesQuantitativeRange){
                                nSats.add(d.intValue());
                            }
                            nSatBounds.add(Collections.min(nSats));
                            nSatBounds.add(Collections.max(nSats));
                            decisions.put(String.format("nSatBounds%d",constellationCount),new Decision<>("Integer", nSatBounds));
                        }else {
                            // Not a decision
                            List<Integer> nSatBounds = new ArrayList<>();
                            nSatBounds.add((Integer) c.getNumberSatellites());
                            nSatBounds.add((Integer) c.getNumberSatellites());
                            decisions.put(String.format("nSatBounds%d",constellationCount),new Decision<>("Integer", nSatBounds));
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Constellation type has to be either DELTA_HOMOGENEOUS, DELTA_HETEROGENOUS, TRAIN OR AD_HOC.");
                }
            }else{
                constellationCount++;
            }
        }

        List<GroundNetwork> groundNetworks = new ArrayList<>();
        int groundNetworkCount=0;
        for (GroundNetwork gn : this.getDesignSpace().getGroundSegment()){
            if (gn.getGroundStations()!=null){
                List<Integer> ngs = new ArrayList<>();
                ngs.add((Integer) gn.getNumberStations());
                decisions.put(String.format("numberGroundStations%d",groundNetworkCount),new Decision<>("Integer",ngs));
                gn.setMutable(false);
                gn.setId(groundNetworkCount);
                groundNetworks.add(gn);
            }else{
                List<GroundStation> listAvailableGroundStations=this.getDesignSpace().getGroundStations();
                List<GroundStation> listValidGroundStations=new ArrayList<>();
                for (GroundStation gs : listAvailableGroundStations){
                    if ((gn.getAgency()==null)||(gs.getAgency()!=null && gs.getAgency().getAgencyType().equalsIgnoreCase(gn.getAgency().getAgencyType()))){
                        listValidGroundStations.add(gs);
                    }
                }
                if (gn.getNumberStationsType()==List.class){
                    List<Integer> numberGroundStations = (List) gn.getNumberStations();
                    decisions.put(String.format("numberGroundStations%d",groundNetworkCount),new Decision<>("Integer",numberGroundStations));

                    for (Integer ngs : numberGroundStations){
                        List<List<GroundStation>> combinations = Combinatorics.combination(listValidGroundStations,ngs);
                        for (List<GroundStation> gs : combinations){
                            GroundNetwork groundNetwork = new GroundNetwork(gn.getName(), gn.getAcronym(), gn.getAgency(), gs.size(), gs);
                            groundNetwork.setMutable(true);
                            groundNetwork.setId(groundNetworkCount);
                            groundNetworks.add(groundNetwork);
                        }
                    }
                }else if (gn.getNumberStationsType()==QuantitativeRange.class){
                    QuantitativeRange numberGroundStationsQuantitativeRange = (QuantitativeRange) gn.getNumberStations();
                    List<Double> numberGroundStations = numberGroundStationsQuantitativeRange.discretize();
                    List <Integer> numberGroundStationsInt = new ArrayList<>();
                    for (Double d : numberGroundStations){
                        numberGroundStationsInt.add(d.intValue());
                    }
                    decisions.put(String.format("numberGroundStations%d",groundNetworkCount),new Decision<>("Integer", numberGroundStationsInt));
                    for (Double ngs : numberGroundStations){
                        List<List<GroundStation>> combinations = Combinatorics.combination(listValidGroundStations,ngs.intValue());
                        for (List<GroundStation> gs : combinations){
                            GroundNetwork groundNetwork = new GroundNetwork(gn.getName(), gn.getAcronym(), gn.getAgency(), gs.size(), gs);
                            groundNetwork.setMutable(true);
                            groundNetwork.setId(groundNetworkCount);
                            groundNetworks.add(groundNetwork);
                        }
                    }
                }else {
                    List<Integer> ngs = new ArrayList<>();
                    ngs.add((Integer) gn.getNumberStations());
                    decisions.put(String.format("numberGroundStations%d",groundNetworkCount),new Decision<>("Integer", ngs));
                    List<List<GroundStation>> combinations = Combinatorics.combination(listValidGroundStations,(int) gn.getNumberStations());
                    for (List<GroundStation> gs : combinations){
                        GroundNetwork groundNetwork = new GroundNetwork(gn.getName(), gn.getAcronym(), gn.getAgency(), gs.size(), gs);
                        groundNetwork.setMutable(true);
                        groundNetwork.setId(groundNetworkCount);
                        groundNetworks.add(groundNetwork);
                    }
                }
            }
            groundNetworkCount++;
        }

        decisions.put("groundNetwork",new Decision<>("Integer", groundNetworks));

        return decisions;
    }

    /**
     * This method reads the objectives in the TSR and returns a list of compound objectives. The ones that are not
     * compound are created as compound objectives with 0 childs. If the search strategy is FF, this method returns an
     * empty list
     * @return the list of compound objectives
     */
    public List<CompoundObjective> processObjectives(){
        List<CompoundObjective> compoundObjectives = new ArrayList<>();
        if (!this.getSettings().getSearchStrategy().equalsIgnoreCase("FF")){
            for (MissionObjective objParent : this.getMission().getObjectives()){
                if (objParent.getParent()==null){
                    CompoundObjective compoundObjective = new CompoundObjective(objParent);
                    for (MissionObjective objChild : this.getMission().getObjectives()){
                        if (objParent.getName().equalsIgnoreCase(objChild.getParent())){
                            compoundObjective.addChild(objChild);
                        }
                    }
                    compoundObjectives.add(compoundObjective);
                }
            }
        }
        return compoundObjectives;
    }
}
