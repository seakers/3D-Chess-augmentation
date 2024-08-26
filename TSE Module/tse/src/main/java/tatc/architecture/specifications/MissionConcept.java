package tatc.architecture.specifications;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import tatc.util.AlwaysListTypeAdapterFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A top-level functional description of an Earth-observing mission concept independent of the implementing form.
 */

public class MissionConcept implements Serializable {
    /**
     * Type tag for JSON file
     */
    @SerializedName("@type")
    private final String _type="MissionConcept";
    /**
     * Name of the mission
     */
    private final String name;
    /**
     * Acronym of the mission
     */
    private final String acronym;
    /**
     * Mission agency
     */
    private final Agency agency;
    /**
     * Mission start date in the form [-]CCYY-MM-DDThh:mm:ss[Z|(+|-)hh:mm] (see Chapter 5.4 of ISO 8601)
     */
    private final String start;
    /**
     * Mission start date in the form [-]CCYY-MM-DDThh:mm:ss[Z|(+|-)hh:mm] (see Chapter 5.4 of ISO 8601)
     */
    private final String end;
    /**
     * The mission duration in  in days or ISO 8601 duration format
     */
    private final Object duration;
    /**
     * Acceptable drift in altitude as a percentage of the nominal/target altitude for orbtial maintenance.
     * For example: a 0.02 threshold for a nominal 500 kilometer orbit indicates a +/- 10 kilometer drift tolerance.
     */
    private final double altitudeDriftPercentThreshold;
    /**
     * Acceptable drift in the argument of latitude as a percentage of the nominal/target spread in relative mean
     * anomaly for orbital maintenance. For example: a 0.10 threshold for a constellation with eight satellites
     * equally-spaced in mean anomaly (i.e. 45 degrees) indicates a +/- 4.5 degree drift tolerance.
     */
    private final double argumentOfLatitudeDriftPercentThreshold;
    /**
     * List of target regions of interest to accomplish mission objectives
     */
    @JsonAdapter(AlwaysListTypeAdapterFactory.class)
    private final List<Region> target;
    /**
     * List of mission interest objects (Recognized case-insensitive values include: SUN, MOON)
     */
    private final List<String> objects;
    /**
     * List of objectives to consider during the intelligent search (i.e. genetic algorithm optimizer)
     */
    private final List<MissionObjective> objectives;
    /**
     * List of constraints to consider during the intelligent search (i.e. genetic algorithm optimizer)
     */
    private final List<MissionConstraint> constraints;

    /**
     * Constructs a mission concept object
     * @param name the name of the mission
     * @param acronym the acronym of the mission
     * @param agency the agency of the mission
     * @param start the mission start date in ISO 8601 format
     * @param end the mission end date in ISO 8601 format
     * @param duration the mission duration in days or ISO 8601 duration formatt
     * @param altitudeDriftPercentThreshold the acceptable drift in altitude as a percentage of the nominal/target
     *                                      altitude for orbtial maintenance
     * @param argumentOfLatitudeDriftPercentThreshold the acceptable drift in the argument of latitude as a percentage
     *                                                of the nominal/target spread in relative mean anomaly for orbital
     *                                                maintenance
     * @param target the list of target regions of interest
     * @param objects the list of mission interest objects
     * @param objectives the list of objectives to consider during the intelligent search
     * @param constraints the list of constraints to consider during the intelligent search
     */
    public MissionConcept(String name, String acronym, Agency agency, String start, String end, Object duration, double altitudeDriftPercentThreshold, double argumentOfLatitudeDriftPercentThreshold, List<Region> target, List<String> objects, List<MissionObjective> objectives, List<MissionConstraint> constraints) {
        this.name = name;
        this.acronym = acronym;
        this.agency = agency;
        this.start = start;
        this.end = end;
        this.duration = duration;
        this.altitudeDriftPercentThreshold = altitudeDriftPercentThreshold;
        this.argumentOfLatitudeDriftPercentThreshold = argumentOfLatitudeDriftPercentThreshold;
        this.target = target;
        this.objects = objects;
        this.objectives = objectives;
        this.constraints = constraints;
    }

    /**
     * Gets the name of the mission
     * @return the name of the mission
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the acronym of the mission
     * @return the acronym of the mission
     */
    public String getAcronym() {
        return acronym;
    }

    /**
     * Gets the agency of the mission
     * @return the agency of the mission
     */
    public Agency getAgency() {
        return agency;
    }

    /**
     * Gets the mission start date in ISO 8601 format
     * @return the mission start date in ISO 8601 format
     */
    public String getStart() {
        return start;
    }

    /**
     * Gets the mission end date in ISO 8601 format
     * @return the mission end date in ISO 8601 format
     */
    public String getEnd() {
        return end;
    }

    /**
     * Gets the mission duration in days or ISO 8601 duration format
     * @return the mission duration as a double (days) or a string (ISO 8601 duration format)
     */
    public Object getDuration() throws IllegalArgumentException{
        if (duration instanceof Double){
            return duration;
        }else if (duration instanceof String){
            return duration;
        }else {
            throw new IllegalArgumentException("Duration has to be either a Double or a String in TradespaceSearch.json");
        }
    }

    /**
     * Gets the class of the duration attribute
     * @return the class of the duration attribute (Double or String)
     */
    public Class getDurationType() throws IllegalArgumentException{
        if (duration instanceof String){
            return String.class;
        }else if (duration instanceof Double){
            return Double.class;
        }else {
            throw new IllegalArgumentException("Duration has to be either a Double or a String in TradespaceSearch.json");
        }
    }

    /**
     * Gets the acceptable drift in altitude as a percentage of the nominal/target altitude for orbtial maintenance
     * @return the acceptable drift in altitude as a percentage of the nominal/target altitude for orbtial maintenance
     */
    public double getAltitudeDriftPercentThreshold() {
        return altitudeDriftPercentThreshold;
    }

    /**
     * Gets the acceptable drift in the argument of latitude as a percentage of the nominal/target spread in relative
     * mean anomaly for orbital maintenance
     * @return the acceptable drift in the argument of latitude as a percentage of the nominal/target spread in relative
     * mean anomaly for orbital maintenance
     */
    public double getArgumentOfLatitudeDriftPercentThreshold() {
        return argumentOfLatitudeDriftPercentThreshold;
    }

    /**
     * Gets the list of target regions of interest
     * @return the list of target regions of interest
     */
    public List<Region> getTarget() {
        return target;
    }

    /**
     * Gets the list of mission interest objects
     * @return the list of mission interest objects
     */
    public List<String> getObjects() {
        return objects;
    }

    /**
     * Gets the list of objectives to consider during the intelligent search
     * @return the list of objectives to consider during the intelligent search
     */
    public List<MissionObjective> getObjectives() {
        return objectives;
    }

    /**
     * Gets the list of constraints to consider during the intelligent search
     * @return the list of constraints to consider during the intelligent search
     */
    public List<MissionConstraint> getConstraints() {
        if (constraints == null){
            return new ArrayList<>();
        }else{
            return constraints;
        }
    }

    /**
     * Gets the list of hard constraints to consider during the intelligent search
     * @return the list of hard constraints to consider during the intelligent search
     */
    public List<MissionConstraint> getHardConstraints() {
        if (constraints == null){
            return new ArrayList<>();
        }else{
            List<MissionConstraint> hardConstraints = new ArrayList<>();
            for(MissionConstraint c : this.constraints){
                if (c.isHard()){
                    hardConstraints.add(c);
                }
            }
            return hardConstraints;
        }
    }

    /**
     * Gets the list of soft constraints to consider during the intelligent search
     * @return the list of soft constraints to consider during the intelligent search
     */
    public List<MissionConstraint> getSoftConstraints() {
        if (constraints == null){
            return new ArrayList<>();
        }else{
            List<MissionConstraint> hardConstraints = new ArrayList<>();
            for(MissionConstraint c : this.constraints){
                if (!c.isHard()){
                    hardConstraints.add(c);
                }
            }
            return hardConstraints;
        }
    }

    /**
     * Checks if there are any soft constraints to consider during the search
     * @return the list of constraints to consider during the intelligent search
     */
    public int areThereSoftConstraints(){
        if (getSoftConstraints().size()==0){
            return 0;
        }else {
            return 1;
        }
    }
}
