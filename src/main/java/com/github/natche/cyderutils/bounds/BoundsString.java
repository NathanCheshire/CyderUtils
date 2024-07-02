package com.github.natche.cyderutils.bounds;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.strings.CyderStrings;

/**
 * A class for associating a size necessary to contain a provided string without overflow.
 * This text may or may not contain HTML formatting.
 * Instances of this class are immutable and thus thread-safe.
 */
@Immutable
public final class BoundsString {
    /** The text of this bounds string. */
    private final String text;

    /** The width of this bounds string. */
    private final int width;

    /** The height of this bounds string. */
    private final int height;

    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private BoundsString() {
        throw new IllegalMethodException(CyderStrings.ILLEGAL_CONSTRUCTOR);
    }

    /**
     * Constructs a new BoundsString object.
     *
     * @param text   the text
     * @param width  the width for the text
     * @param height the height for the text
     * @throws NullPointerException     if the provided text is null
     * @throws IllegalArgumentException if the provided width or height is less than zero
     */
    public BoundsString(String text, int width, int height) {
        Preconditions.checkNotNull(text);
        Preconditions.checkArgument(width >= 0);
        Preconditions.checkArgument(height >= 0);

        this.text = text;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the text for this bounds string.
     *
     * @return the text for this bounds string
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the width for this bounds string.
     *
     * @return the width for this bounds string
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height for this bounds string.
     *
     * @return the height for this bounds string
     */
    public int getHeight() {
        return height;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int ret = text.hashCode();
        ret = 31 * ret + Integer.hashCode(width);
        ret = 31 * ret + Integer.hashCode(height);
        return ret;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "BoundsString{"
                + "text=\"" + text + "\", "
                + "width=" + width + ", "
                + "height=" + height
                + "}";
    }

    /** {@inheritDoc} */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof BoundsString)) {
            return false;
        }

        BoundsString other = (BoundsString) o;
        return text.equals(other.getText())
                && width == other.getWidth()
                && height == other.getHeight();
    }
}
