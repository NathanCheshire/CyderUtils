package cyder.math;

import com.google.common.collect.Range;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;

/**
 * Utilities related to angles.
 */
public final class AngleUtil {
    /**
     * The number of degrees in a circle.
     */
    public static final int DEGREES_IN_CIRCLE = 360;

    /**
     * One hundred eighty degrees.
     */
    public static final int ONE_EIGHTY_DEGREES = 180;

    /**
     * The standard range of angle measurements in degree form.
     */
    public static final Range<Double> DEGREE_RANGE = Range.closedOpen(0d, (double) DEGREES_IN_CIRCLE);

    /**
     * The range for angles in degree format in the range [0, 180).
     */
    public static final Range<Double> ONE_EIGHTY_DEGREE_RANGE = Range.closedOpen(0d, (double) ONE_EIGHTY_DEGREES);

    /**
     * The standard range of angle measurements in radian form.
     */
    public static final Range<Double> RADIAN_RANGE = Range.closedOpen(0d, 2 * Math.PI);

    /**
     * Suppress default constructor.
     */
    private AngleUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Converts the angle in degrees to standard form of being in the range [0, 360).
     *
     * @param angle the angle in degrees
     * @return the angle in standard form with rotations removed
     */
    public static int normalizeAngle360(int angle) {
        angle = angle % DEGREES_IN_CIRCLE;
        if (angle < 0) angle += DEGREES_IN_CIRCLE;
        return angle;
    }

    /**
     * Converts the angle in degrees to standard form meaning in the range [0, 360).
     *
     * @param angle the angle in degrees
     * @return the angle in standard form with rotations removed
     */
    public static double normalizeAngle360(double angle) {
        angle = angle % DEGREES_IN_CIRCLE;
        if (angle < 0) angle += DEGREES_IN_CIRCLE;
        return angle;
    }

    /**
     * Normalizes the provided angle to be in the range [0, 180).
     *
     * @param angle the angle to normalize to the range [0, 180)
     * @return the normalized angle
     */
    public static double normalizeAngle180(double angle) {
        angle = angle % ONE_EIGHTY_DEGREES;
        if (angle < 0) angle += ONE_EIGHTY_DEGREES;
        return angle;
    }
}
