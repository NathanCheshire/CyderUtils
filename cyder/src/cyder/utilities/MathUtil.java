package cyder.utilities;

import cyder.constants.CyderStrings;

import java.awt.*;

/**
 * General mathematical functions and methods.
 */
public class MathUtil {
    /**
     * Restrict class instnatiation.
     */
    private MathUtil() {
        throw new IllegalStateException(CyderStrings.attemptedInstantiation);
    }

    /**
     * Finds the greatest common divisor of the provided integers
     *
     * @param a the first integer
     * @param b the second integer
     * @return the greatest common divisor of the provided integers
     */
    public static int gcd(int a, int b) {
        if (a < b)
            return gcd(b, a);
        if (a % b == 0)
            return b;
        else
            return gcd(b, a % b);
    }

    /**
     * Finds the least common multiple of the provided integers
     *
     * @param a the first integer
     * @param b the second interger
     * @return the least common multiple of the provided integers
     */
    public static int lcm(int a, int b) {
        return ((a * b) / gcd(a, b));
    }

    /**
     * Finds the lcm of the provided array.
     *
     * @param arr the array to find the lcm of
     * @return the lcm of the provided array
     */
    public static int lcmArray(int[] arr) {
        return lcmArrayInner(arr, 0, arr.length);
    }

    /**
     * Helper method for finding the lcm of an Array
     *
     * @param arr the array to find the lcm of
     * @param start the starting index for the array
     * @param end the ending index for the array
     * @return the lcm of the provided array
     */
    private static int lcmArrayInner(int[] arr, int start, int end) {
        if ((end - start) == 1)
            return lcm(arr[start], arr[end - 1]);
        else
            return lcm(arr[start], lcmArrayInner(arr, start + 1, end));
    }

    //Precise method, which guarantees v = v1 when t = 1.
    // This method is monotonic only when v0 * v1 < 0.
    // Lerping between same values might not produce the same value

    /**
     * Linearly interpolates between v0 and v1 for the provided t value in the range [0,1]
     *
     * @param v0 the first value
     * @param v1 the second value
     * @param t the t value in the range [0,1]
     * @return the linearly interpolated value
     */
    public static float lerp(float v0, float v1, float t) {
        return (1 - t) * v0 + t * v1;
    }

    /**
     * Rotates the provided point by deg degrees in euclidean space.
     *
     * @param point the point to ratate
     * @param deg the degrees to rotate the point by, counter-clockwise
     * @return the new point
     */
    public static Point rotatePoint(Point point, double deg) {
        double rad = Math.toRadians(deg);
        return new Point((int) (point.x * Math.cos(rad) - point.y * Math.sin(rad)),
                         (int) (point.x * Math.sin(rad) + point.y * Math.cos(rad)));
    }

    /**
     * Determines if the rectangles intersect i.e. overlap each other.
     *
     * @param r1 the first rectangle
     * @param r2 the second rectangle
     * @return whether or not the rectangles intersect each other
     */
    public static boolean overlaps(Rectangle r1, Rectangle r2) {
        return r2.x < r1.x + r1.width && r2.x + r2.width > r1.x && r2.y < r1.y + r1.height && r2.y + r2.height > r1.y;
    }
}