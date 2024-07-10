package com.github.natche.cyderutils.range;

import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.google.common.collect.Range;

/** Utilities related to {@link com.google.common.collect.Range}s */
public class RangeUtil {
    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private RangeUtil() {
        throw new IllegalMethodException("Instances of RangeUtil are not allowed");
    }

    /**
     * Constrains the given integer to the specified range.
     *
     * @param number the number to constrain
     * @param range the range to constrain to
     * @return the constrained number
     */
    public static int constrainToRange(int number, Range<Integer> range) {
        return Math.min(Math.max(number, range.lowerEndpoint()), range.upperEndpoint());
    }

    /**
     * Constrains the given long to the specified range.
     *
     * @param number the number to constrain
     * @param range the range to constrain to
     * @return the constrained number
     */
    public static long constrainToRange(long number, Range<Long> range) {
        return Math.min(Math.max(number, range.lowerEndpoint()), range.upperEndpoint());
    }

    /**
     * Constrains the given float to the specified range.
     *
     * @param number the number to constrain
     * @param range the range to constrain to
     * @return the constrained number
     */
    public static float constrainToRange(float number, Range<Float> range) {
        return Math.min(Math.max(number, range.lowerEndpoint()), range.upperEndpoint());
    }

    /**
     * Constrains the given double to the specified range.
     *
     * @param number the number to constrain
     * @param range the range to constrain to
     * @return the constrained number
     */
    public static double constrainToRange(double number, Range<Double> range) {
        return Math.min(Math.max(number, range.lowerEndpoint()), range.upperEndpoint());
    }
}
