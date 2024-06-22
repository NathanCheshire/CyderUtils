package com.github.natche.cyderutils.math;

import com.google.common.base.Preconditions;

import java.awt.*;

/**
 * A point class for exposing additional methods than that of {@link Point}.
 */
public final class CyderPoint extends Point {
    /**
     * Constructs a new instance of a CyderPoint from the provided x,y location.
     *
     * @param x the x location of the point
     * @param y the y location of the point
     */
    public CyderPoint(int x, int y) {
        super(x, y);
    }

    /**
     * Constructs a new instance of a CyderPoint from the provided Point.
     *
     * @param point the point
     * @return a new CyderPoint object
     * @throws NullPointerException if the provided point is null
     */
    public static CyderPoint from(Point point) {
        Preconditions.checkNotNull(point);
        return new CyderPoint((int) point.getX(), (int) point.getY());
    }

    /**
     * Returns whether this point lies within the provided rectangle.
     *
     * @param rectangle the rectangle
     * @return whether this point lies within the provided rectangle
     * @throws NullPointerException if the provided rectangle is null
     */
    public boolean inRectangle(Rectangle rectangle) {
        Preconditions.checkNotNull(rectangle);

        return x > rectangle.getX()
                && x < rectangle.x + rectangle.width
                && y > rectangle.getY()
                && y < rectangle.y + rectangle.height;
    }

    /**
     * Returns whether this point is on the provided rectangle.
     *
     * @param rectangle the rectangle
     * @return whether this point is on the provided rectangle
     * @throws NullPointerException if the provided rectangle is null
     */
    public boolean onRectangle(Rectangle rectangle) {
        Preconditions.checkNotNull(rectangle);

        return onTopOfRectangle(rectangle)
                || onBottomOfRectangle(rectangle)
                || onLeftOfRectangle(rectangle)
                || onRightOfRectangle(rectangle);
    }

    /**
     * Returns whether this point is on or in the provided rectangle.
     *
     * @param rectangle this rectangle
     * @return whether this point is on or in the provided rectangle
     * @throws NullPointerException if the provided rectangle is null
     */
    public boolean onOrInRectangle(Rectangle rectangle) {
        Preconditions.checkNotNull(rectangle);

        return onRectangle(rectangle) || inRectangle(rectangle);
    }

    /**
     * Returns whether this point lies on the left of the provided rectangle.
     *
     * @param rectangle the rectangle
     * @return whether this point lies on the left of the provided rectangle
     * @throws NullPointerException if the provided rectangle is null
     */
    public boolean onLeftOfRectangle(Rectangle rectangle) {
        Preconditions.checkNotNull(rectangle);

        return x == rectangle.x
                && y >= rectangle.y
                && y <= rectangle.y + rectangle.height;
    }

    /**
     * Returns whether this point lies on the right of the provided rectangle.
     *
     * @param rectangle the rectangle
     * @return whether this point lies on the right of the provided rectangle
     * @throws NullPointerException if the provided rectangle is null
     */
    public boolean onRightOfRectangle(Rectangle rectangle) {
        Preconditions.checkNotNull(rectangle);

        return x == rectangle.x + rectangle.width
                && y >= rectangle.y
                && y <= rectangle.y + rectangle.height;
    }

    /**
     * Returns whether this point is on top of the provided rectangle.
     *
     * @param rectangle the rectangle
     * @return whether this point is on top of the provided rectangle
     * @throws NullPointerException if the provided rectangle is null
     */
    public boolean onTopOfRectangle(Rectangle rectangle) {
        Preconditions.checkNotNull(rectangle);

        return y == rectangle.y
                && x >= rectangle.x
                && x <= rectangle.x + rectangle.width;
    }

    /**
     * Returns whether this point lies on the bottom of the provided rectangle.
     *
     * @param rectangle the rectangle
     * @return whether this point lies on the bottom of the provided rectangle
     * @throws NullPointerException if the provided rectangle is null
     */
    public boolean onBottomOfRectangle(Rectangle rectangle) {
        Preconditions.checkNotNull(rectangle);

        return y == rectangle.y
                && x >= rectangle.x
                && x <= rectangle.x + rectangle.width;
    }

    /**
     * Rotates this Point by the provided angle amount and returns a new Point.
     *
     * @param angle the angle amount by which to rotate
     * @return a new Point object rotated by the provided angle
     * @throws NullPointerException if the provided angle is null
     */
    public Point rotate(Angle angle) {
        Preconditions.checkNotNull(angle);
        return rotate(angle.getDegrees());
    }

    /**
     * Rotates this Point by the provided angle amount and returns a new Point.
     *
     * @param degrees the angle amount by which to rotate
     * @return a new Point object rotated by the provided angle
     */
    public Point rotate(double degrees) {
        double normalizedDegrees = Angle.normalize360(degrees);
        double radians = Math.toRadians(normalizedDegrees);

        double sin = Math.sin(radians);
        double cos = Math.cos(radians);

        double xRotated = x * cos - y * sin;
        double yRotated = x * sin + y * cos;
        return new Point((int) xRotated, (int) yRotated);
    }

    /**
     * Returns whether this point is near the provided point meaning
     * the distance between the two is less than or equal to the provided max distance.
     *
     * @param point       the other point
     * @param maxDistance the maximum distance
     * @return whether this point is near the provided point
     * @throws NullPointerException     if the provided point is null
     * @throws IllegalArgumentException if the provided maxDistance is less than or equal to zero
     */
    public boolean isNear(CyderPoint point, double maxDistance) {
        Preconditions.checkNotNull(point);
        Preconditions.checkArgument(maxDistance > 0);

        double distance = Math.sqrt(Math.pow(point.x - x, 2) + Math.pow(point.y - y, 2));
        return distance <= maxDistance;
    }

    /**
     * Computes and returns a new CyderPoint representing the midpoint between this point and the provided point.
     *
     * @param point the other point
     * @return the midpoint between this point and the other point
     * @throws NullPointerException if the provided point is null
     */
    public CyderPoint computeMidpoint(CyderPoint point) {
        Preconditions.checkNotNull(point);

        double xDistance = (double) (x - point.x) / 2.0;
        double yDistance = (double) (y - point.y) / 2.0;

        return new CyderPoint((int) (xDistance / 2), (int) (yDistance / 2));
    }

    /**
     * Computes and returns the magnitude of this point.
     *
     * @return the magnitude of this point
     */
    public double magnitude() {
        return Math.sqrt(Math.pow(x, 2) +  Math.pow(y, 2));
    }

    /**
     * Returns the angle of this point with the positive X axis.
     *
     * @return the angle of this point in the range [0, 360)
     */
    public double getAngle() {
        double angleRadians = Math.atan2(y, x);
        double angleDegrees = Math.toDegrees(angleRadians);
        if (angleDegrees < 0) angleDegrees += 360;
        return angleDegrees;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "CyderPoint{"
                + "x=" + getX()
                + ", y=" + getY()
                + "}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = Integer.hashCode(x);
        ret = 31 * ret + Integer.hashCode(y);
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof CyderPoint)) {
            return false;
        }

        CyderPoint other = (CyderPoint) o;
        return x == other.x
                && y == other.y;
    }
}
