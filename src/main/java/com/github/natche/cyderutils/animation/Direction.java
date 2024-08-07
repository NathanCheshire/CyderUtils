package com.github.natche.cyderutils.animation;

import com.google.common.base.Preconditions;

/** A cardinal direction. */
public enum Direction {
    LEFT("left"),
    RIGHT("right"),
    TOP("top"),
    BOTTOM("bottom");

    private final String name;

    Direction(String name) {
        this.name = name;
    }

    /**
     * Returns the name of this direction.
     *
     * @return the name of this direction
     */
    public String getName() {
        return name;
    }

    /**
     * Returns whether this direction runs along the horizontal axis.
     *
     * @return whether this direction runs along the horizontal axis
     */
    public boolean isHorizontal() {
        return isHorizontal(this);
    }

    /**
     * Returns whether this direction runs along the vertical axis.
     *
     * @return whether this direction runs along the vertical axis
     */
    public boolean isVertical() {
        return isVertical(this);
    }

    /**
     * Returns whether the direction points horizontally.
     *
     * @param direction the direction
     * @return whether the direction points horizontally
     * @throws NullPointerException if the provided direction is null
     */
    public static boolean isHorizontal(Direction direction) {
        Preconditions.checkNotNull(direction);

        return direction == LEFT || direction == RIGHT;
    }

    /**
     * Returns whether the direction points vertically.
     *
     * @param direction the direction
     * @return whether the direction points vertically
     * @throws NullPointerException if the provided direction is null
     */
    public static boolean isVertical(Direction direction) {
        Preconditions.checkNotNull(direction);

        return direction == TOP || direction == BOTTOM;
    }
}
