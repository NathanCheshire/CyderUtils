package cyder.math;

import com.google.common.collect.Range;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;

/**
 * Utilities related to angles.
 */
@SuppressWarnings("unused") /* Ranges not currently used */
public final class AngleUtil {
    /**
     * The standard range of angle measurements in degree form.
     */
    public static final Range<Double> DEGREE_RANGE = Range.closedOpen(0d, Angle.THREESI);

    /**
     * The range for angles in degree format in the range [0, 180).
     */
    public static final Range<Double> ONE_EIGHTY_DEGREE_RANGE = Range.closedOpen(0d, ONE_EIGHTY_DEGREES);

    /**
     * The standard range of angle measurements in radian form.
     */
    public static final Range<Double> RADIAN_RANGE = Range.closedOpen(0d, 2 * Math.PI);

    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
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
        angle = angle % (int) THREE_SIXTY_DEGREES;
        if (angle < 0) angle += THREE_SIXTY_DEGREES;
        return angle;
    }

    /**
     * Converts the angle in degrees to standard form meaning in the range [0, 360).
     *
     * @param angle the angle in degrees
     * @return the angle in standard form with rotations removed
     */
    public static double normalizeAngle360(double angle) {
        angle = angle % THREE_SIXTY_DEGREES;
        if (angle < 0) angle += THREE_SIXTY_DEGREES;
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

    /**
     * Returns whether the provided angle is north.
     *
     * @param bearing the angle of bearing
     * @return whether the provided angle is north
     */
    public static boolean angleInNorthernHemisphere(double bearing) {
        bearing = normalizeAngle360(bearing);

        return bearing > 0.0 && bearing < AngleUtil.ONE_EIGHTY_DEGREES;
    }

    /**
     * Returns whether the provided angle is south.
     *
     * @param bearing the angle of bearing
     * @return whether the provided angle is south
     */
    public static boolean angleInSouthernHemisphere(double bearing) {
        bearing = normalizeAngle360(bearing);

        return bearing > AngleUtil.ONE_EIGHTY_DEGREES && bearing < AngleUtil.THREE_SIXTY_DEGREES;
    }

    /**
     * Returns whether the provided angle is east.
     *
     * @param bearing the angle of bearing
     * @return whether the provided angle is east
     */
    public static boolean angleIsEast(double bearing) {
        bearing = normalizeAngle360(bearing);

        return bearing == 0.0;
    }

    /**
     * Returns whether the provided angle is west.
     *
     * @param bearing the angle of bearing
     * @return whether the provided angle is west
     */
    public static boolean angleIsWest(double bearing) {
        bearing = normalizeAngle360(bearing);

        return bearing == AngleUtil.ONE_EIGHTY_DEGREES;
    }
}
