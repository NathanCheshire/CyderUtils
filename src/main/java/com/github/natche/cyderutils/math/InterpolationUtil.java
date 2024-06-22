package com.github.natche.cyderutils.math;

import com.google.common.base.Preconditions;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.strings.CyderStrings;

/**
 * Utilities related to interpolation.
 */
public final class InterpolationUtil {
    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private InterpolationUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Transforms a value from one range to another.
     * This method maps the given value, assumed to be within the range defined by [low1, high1],
     * to a new value within the range defined by [low2, high2] using linear interpolation.
     *
     * @param value the value to be remapped
     * @param low1  the lower bound of the original range
     * @param high1 the upper bound of the original range
     * @param low2  the lower bound of the target range
     * @param high2 the upper bound of the target range
     * @return the value mapped to the new range
     * @throws IllegalArgumentException if high1 equals low1
     */
    public static double rangeMap(double value, double low1, double high1, double low2, double high2) {
        Preconditions.checkArgument(high1 != low1);

        double normalizedValue = (value - low1) / (high1 - low1);
        return linearlyInterpolate(low2, high2, normalizedValue);
    }

    /**
     * Performs linear interpolation between two values.
     * The method calculates a value linearly spaced between value1 and value2, based on the given interpolation amount
     * An amount of 0.0 will return value1, and an amount of 1.0 will return value2.
     * <p>
     * For more information on linear interpolation and its mathematical basis, see:
     * <a href="https://en.wikipedia.org/wiki/Linear_interpolation">...</a>
     *
     * @param value1 the start value for interpolation
     * @param value2 the end value for interpolation
     * @param amount the proportion to interpolate between the two values, typically in the range [0.0, 1.0]
     * @return the interpolated value
     */
    private static double linearlyInterpolate(double value1, double value2, double amount) {
        return (value1 * (1 - amount)) + (value2 * amount);
    }
}
