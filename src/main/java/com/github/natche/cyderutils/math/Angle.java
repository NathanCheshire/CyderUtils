package com.github.natche.cyderutils.math;

import java.util.Arrays;

/**
 * Represents standard Cartesian angles commonly used in trigonometry and geometry.
 * This enum includes angles on the axes, angles associated with "thirty, sixty, ninety"
 * right triangles (30°, 60°, 90°), and angles associated with "one, one, square root of two"
 * right isosceles triangles (45°, 90°, 45°). It also encompasses their inversions
 * (angles in different quadrants with the same sine/cosine values but different signs),
 * reflections (angles reflected across the x-axis or y-axis), and inverted reflections
 * (combination of inversion and reflection).
 * <p>
 * This facilitates easy access to angle values in both degrees and radians, and can be
 * particularly useful in scenarios requiring precise trigonometric calculations
 * or geometric constructions.
 */
public enum Angle {
    ZERO(0, 0),
    THIRTY(30, Math.PI / 6),
    FORTY_FIVE(45, Math.PI / 4),
    SIXTY(60, Math.PI / 3),
    NINETY(90, Math.PI / 2),
    ONE_TWENTY(120, 2 * Math.PI / 3),
    ONE_THIRTY_FIVE(135, 3 * Math.PI / 4),
    ONE_FIFTY(150, 5 * Math.PI / 6),
    ONE_EIGHTY(180, Math.PI),
    TWO_TEN(210, 7 * Math.PI / 6),
    TWO_TWENTY_FIVE(225, 5 * Math.PI / 4),
    TWO_FORTY(240, 4 * Math.PI / 3),
    TWO_SEVENTY(270, 3 * Math.PI / 2),
    THREE_HUNDRED(300, 5 * Math.PI / 3),
    THREE_FIFTEEN(315, 7 * Math.PI / 4),
    THREE_THIRTY(330, 11 * Math.PI / 6),
    THREE_SIXTY(360, 2 * Math.PI);

    private static final double GET_BY_DEGREES_TOLERANCE = 0.0001;

    private final int degrees;
    private final double radians;

    Angle(int degrees, double radians) {
        this.degrees = degrees;
        this.radians = radians;
    }

    /**
     * Normalizes the provided angle to be within the range of [0, 180).
     *
     * @param angle the angle to normalize
     * @return the normalized angle
     */
    public static double normalize180(double angle) {
        angle = normalize360(angle);
        if (angle > Angle.ONE_EIGHTY.getDegrees()) angle -= Angle.THREE_SIXTY.getDegrees();
        return angle;
    }

    /**
     * Normalizes the provided angle to be within the range of [0, 360).
     *
     * @param angle the angle to normalize
     * @return the normalized angle
     */
    public static double normalize360(double angle) {
        angle = angle % Angle.THREE_SIXTY.getDegrees();
        if (angle < Angle.ZERO.getDegrees()) angle += Angle.THREE_SIXTY.getDegrees();
        return angle;
    }

    /**
     * Returns the degrees measurement for this angle.
     *
     * @return the degrees measurement for this angle
     */
    public int getDegrees() {
        return degrees;
    }

    /**
     * Returns the radians measurement for this angle.
     *
     * @return the radians measurement for this angle
     */
    public double getRadians() {
        return radians;
    }

    /**
     * Returns the angle opposite to this one.
     *
     * @return the angle opposite to this one
     */
    public Angle opposite() {
        int oppositeDegrees = (this.degrees + Angle.ONE_EIGHTY.getDegrees()) % Angle.THREE_SIXTY.getDegrees();
        return getByDegrees(oppositeDegrees);
    }

    /**
     * Returns the complementary angle to this one.
     *
     * @return the complementary angle to this one
     */
    public Angle complementary() {
        int complementaryDegrees = Angle.NINETY.getDegrees() - this.degrees;
        return getByDegrees(complementaryDegrees);
    }

    /**
     * Returns the supplementary angle to this angle.
     *
     * @return the supplementary angle to this angle
     */
    public Angle supplementary() {
        int supplementaryDegrees = Angle.ONE_EIGHTY.getDegrees() - this.degrees;
        return getByDegrees(supplementaryDegrees);
    }

    /**
     * Calculates the difference between this angle and the provided angle.
     *
     * @param angle the other angle
     * @return the difference between this angle and the provided angle
     */
    public int difference(Angle angle) {
        return Math.abs(this.degrees - angle.degrees);
    }

    /**
     * Returns the cartesian quadrant this angle falls in.
     * Note that {@link #ZERO}, {@link #NINETY}, {@link #ONE_EIGHTY},
     * and {@link #TWO_SEVENTY} are all are in quadrant "0"
     *
     * @return the cartesian quadrant this angle falls in
     */
    public int quadrant() {
        if (this.degrees > Angle.ZERO.getDegrees() && this.degrees < Angle.NINETY.getDegrees()) return 1;
        if (this.degrees > Angle.NINETY.getDegrees() && this.degrees < Angle.ONE_EIGHTY.getDegrees()) return 2;
        if (this.degrees > Angle.ONE_EIGHTY.getDegrees() && this.degrees < Angle.TWO_SEVENTY.getDegrees()) return 3;
        if (this.degrees > Angle.TWO_SEVENTY.getDegrees() && this.degrees < Angle.THREE_SIXTY.getDegrees()) return 4;
        return 0;
    }

    /**
     * Returns whether this angle is in the northern hemisphere meaning
     * greater than an angle of {@link #ZERO} and less than an angle of {@link #ONE_EIGHTY}.
     *
     * @return whether this angle is in the northern hemisphere
     */
    public boolean isInNorthernHemisphere() {
        return degrees > Angle.ZERO.getDegrees() && degrees < Angle.ONE_EIGHTY.getDegrees();
    }

    /**
     * Returns whether this angle is in the southern hemisphere meaning
     * greater than an angle of {@link #ONE_EIGHTY} and less than an angle of {@link #THREE_SIXTY}.
     *
     * @return whether this angle is in the southern hemisphere
     */
    public boolean isInSouthernHemisphere() {
        return degrees > Angle.ONE_EIGHTY.getDegrees() && degrees < Angle.THREE_SIXTY.getDegrees();
    }

    /**
     * Returns the angle object which corresponds to the provided degrees.
     *
     * @param degrees the degrees amount
     * @return the angle object which corresponds to the provided degrees
     */
    private Angle getByDegrees(double degrees) {
        return Arrays.stream(Angle.values())
                .filter(angle -> Math.abs(angle.degrees - degrees) < GET_BY_DEGREES_TOLERANCE)
                .findFirst()
                .orElse(null);
    }
}
