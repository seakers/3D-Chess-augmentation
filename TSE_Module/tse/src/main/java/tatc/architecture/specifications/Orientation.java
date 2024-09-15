package tatc.architecture.specifications;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Class that contains information about the orientation of the instrument with respect to the satellite body frame
 */
public class Orientation implements Serializable {
    /**
     * Type tag for JSON file
     */
    @SerializedName("@type")
    private final String _type="Orientation";
    /**
     * Convention used to specify the orientation. Recognized values include: XYZ (x-axis, y-axis, z-axis rotation),
     * SIDE_LOOK (only specify side look (y-axis) angle).
     */
    private final String OrientationConvention;
    /**
     * Rotation angle (deg) about x-axis
     */
    private final Double xRotation;
    /**
     * Rotation angle (deg) about y-axis
     */
    private final Double yRotation;
    /**
     * Rotation angle (deg) about z-axis
     */
    private final Double zRotation;
    /**
     * Rotation angle (deg) about spacecraft side (y-axis)
     */
    private final Double sideLookAngle;

    /**
     * Constructs an orientation object
     * @param orientationConvention the convention used to specify the orientation. Recognized values include: XYZ
     *                              (x-axis, y-axis, z-axis rotation), SIDE_LOOK (only specify side look (y-axis) angle)
     * @param xRotation the rotation angle (deg) about x-axis
     * @param yRotation the rotation angle (deg) about y-axis
     * @param zRotation the rotation angle (deg) about z-axis
     * @param sideLookAngle the rotation angle (deg) about spacecraft side (y-axis)
     */
    public Orientation(String orientationConvention, Double xRotation, Double yRotation, Double zRotation, Double sideLookAngle) {
        OrientationConvention = orientationConvention;
        this.xRotation = xRotation;
        this.yRotation = yRotation;
        this.zRotation = zRotation;
        this.sideLookAngle = sideLookAngle;
    }

    /**
     * Gets the convention used to specify the orientation. Recognized values include: XYZ
     * (x-axis, y-axis, z-axis rotation), SIDE_LOOK (only specify side look (y-axis) angle)
     * @return the convention used to specify the orientation. Recognized values include: XYZ
     * (x-axis, y-axis, z-axis rotation), SIDE_LOOK (only specify side look (y-axis) angle)
     */
    public String getOrientationConvention() {
        return OrientationConvention;
    }

    /**
     * Gets the rotation angle (deg) about x-axis
     * @return the rotation angle (deg) about x-axis
     */
    public Double getxRotation() {
        return xRotation;
    }

    /**
     * Gets the rotation angle (deg) about y-axis
     * @return the rotation angle (deg) about y-axis
     */
    public Double getyRotation() {
        return yRotation;
    }

    /**
     * Gets the rotation angle (deg) about z-axis
     * @return the rotation angle (deg) about z-axis
     */
    public Double getzRotation() {
        return zRotation;
    }

    /**
     * Gets the rotation angle (deg) about spacecraft side (y-axis)
     * @return the rotation angle (deg) about spacecraft side (y-axis)
     */
    public Double getSideLookAngle() {
        return sideLookAngle;
    }
}
