package tatc.architecture.specifications;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Class that contains field of view specifications
 */
public class FieldOfView implements Serializable {
    /**
     * Type tag for JSON file
     */
    @SerializedName("@type")
    private final String _type="FieldOfView";
    /**
     * Specification of sensor geometry. Recongized values include: CONICAL, RECTANGULAR (default), CUSTOM
     */
    private final String sensorGeometry;
    /**
     * Angle (deg) of full FoV cone (for CONE geometry)
     */
    private final Double fullConeAngle;
    /**
     * Angle (deg) in along-track direction
     */
    private final Double alongTrackFieldOfView;
    /**
     * Angle (deg) in cross-track direction
     */
    private final Double crossTrackFieldOfView;
    /**
     * List of numeric values explaining the field of view angles (deg) at various angular positions defined in
     * customClockAnglesVector
     */
    private final List<Double> customConeAnglesVector;
    /**
     * List of numeric values explaining the angular positions (deg) at which field of view angles in
     * customConeAnglesVector are defined
     */
    private final List<Double>  customClockAnglesVector;
    private final Double  fieldOfRegard;

    /**
     * Constructs a field of view specifications object
     * @param sensorGeometry the sensor geometry (CONICAL, RECTANGULAR or CUSTOM)
     * @param fullConeAngle the angle (deg) of full FoV cone (for CONE geometry)
     * @param alongTrackFieldOfView the angle (deg) in along-track direction
     * @param crossTrackFieldOfView the angle (deg) in cross-track direction
     * @param customConeAnglesVector the numeric values explaining the field of view angles (deg) at various angular
     *                               positions defined in customClockAnglesVector
     * @param customClockAnglesVector the numeric values explaining the angular positions (deg) at which field of view
     *                                angles in customConeAnglesVector are defined
     */
    public FieldOfView(String SensorGeometry, Double fullConeAngle, Double alongTrackFieldOfView, Double crossTrackFieldOfView, List<Double> customConeAnglesVector, List<Double> customClockAnglesVector, Double fieldOfRegard) {
        sensorGeometry = SensorGeometry;
        this.fullConeAngle = fullConeAngle;
        this.alongTrackFieldOfView = alongTrackFieldOfView;
        this.crossTrackFieldOfView = crossTrackFieldOfView;
        this.customConeAnglesVector = customConeAnglesVector;
        this.customClockAnglesVector = customClockAnglesVector;
        this.fieldOfRegard = fieldOfRegard;
    }

    /**
     * Gets the sensor geometry (CONICAL, RECTANGULAR or CUSTOM)
     * @return the sensor geometry (CONICAL, RECTANGULAR or CUSTOM)
     */
    public String getSensorGeometry() {
        return sensorGeometry;
    }

    /**
     * Gets the angle (deg) of full FoV cone (for CONE geometry)
     * @return the angle (deg) of full FoV cone (for CONE geometry)
     */
    public Double getFullConeAngle() {
        return fullConeAngle;
    }

    /**
     * Gets the angle (deg) in along-track direction
     * @return the angle (deg) in along-track direction
     */
    public Double getAlongTrackFieldOfView() {
        return alongTrackFieldOfView;
    }

    /**
     * Gets the angle (deg) in cross-track direction
     * @return the angle (deg) in cross-track direction
     */
    public Double getCrossTrackFieldOfView() {
        return crossTrackFieldOfView;
    }

    /**
     * Gets the numeric values explaining the field of view angles (deg) at various angular positions
     * defined in customClockAnglesVector
     * @return the numeric values explaining the field of view angles (deg) at various angular positions
     * defined in customClockAnglesVector
     */
    public List<Double> getCustomConeAnglesVector() {
        return customConeAnglesVector;
    }

    /**
     * Gets the numeric values explaining the angular positions (deg) at which field of view angles in
     * customConeAnglesVector are defined
     * @return the numeric values explaining the angular positions (deg) at which field of view angles in
     * ustomConeAnglesVector are defined
     */
    public List<Double> getCustomClockAnglesVector() {
        return customClockAnglesVector;
    }
}
