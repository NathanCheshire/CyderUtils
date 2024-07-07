package com.github.natche.cyderutils.bounds;

import com.github.natche.cyderutils.font.CyderFonts;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;

import java.awt.*;

/**
 * A String container for computing the dimensions required for a container to contain the content
 * of a String inside of it with optional properties of Font, maximum width, and maximum height;
 * By default there is no maximum width or height and the default font is {@link CyderFonts#DEFAULT_FONT_SMALL}
 */
@Immutable
public final class BoundsString {
    /** The text of this bounds string. */
    private final String text;

    /** The width of this bounds string. */
    private int width;

    /** The height of this bounds string. */
    private int height;

    /** The font the label showing the content will use. */
    private final Font font;

    /** The maximum allowable width. */
    private final int maxWidth;

    /** The maximum allowable height. */
    private final int maxHeight;

    /**
     * Constructs a new BoundsString object.
     *
     * @param builder the builder
     * @throws NullPointerException if the provided builder is null
     */
    private BoundsString(Builder builder) {
        Preconditions.checkNotNull(builder);

        this.text = builder.content;
        this.font = builder.font;
        this.maxWidth = builder.maxWidth;
        this.maxHeight = builder.maxHeight;

        calculateBounds();
    }

    /** Computes the bounds needed for this BoundsString. */
    private void calculateBounds() {
        // todo
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

    /**
     * Returns the necessary width and height for a container to contain the string content.
     *
     * @return the necessary width and height for a container to contain the string content
     */
    public Dimension getSize() {
        return new Dimension(width, height);
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

    /** A builder for constructing a BoundsString object. */
    public static final class Builder {
        private final String content;
        private Font font = CyderFonts.DEFAULT_FONT_SMALL;
        private int maxWidth = Integer.MAX_VALUE;
        private int maxHeight = Integer.MAX_VALUE;

        /**
         * Constructs a new builder for a BoundsString.
         *
         * @param content the string content
         * @throws NullPointerException if the provided content is null
         */
        public Builder(String content) {
            Preconditions.checkNotNull(content);

            this.content = content;
        }

        /**
         * Sets the font this BoundsString will use when calculating the dimensions.
         *
         * @param font the font
         * @return this builder
         * @throws NullPointerException if the provided font is null
         */
        public Builder setFont(Font font) {
            Preconditions.checkNotNull(font);
            this.font = font;
            return this;
        }

        /**
         * Sets the maximum width of the computed necessary width.
         *
         * @param maxWidth the maximum width of the computed necessary width.
         * @return this builder
         * @throws IllegalArgumentException if the provided max width is less than or equal to zero
         */
        public Builder setMaxWidth(int maxWidth) {
            Preconditions.checkArgument(maxWidth > 0);
            this.maxWidth = maxWidth;
            return this;
        }

        /**
         * Sets the maximum height of the computed necessary height.
         *
         * @param maxHeight the maximum height of the computed necessary height.
         * @return this builder
         * @throws IllegalArgumentException if the provided max height is less than or equal to zero
         */
        public Builder setMaxHeight(int maxHeight) {
            Preconditions.checkArgument(maxHeight > 0);
            this.maxHeight = maxHeight;
            return this;
        }

        /**
         * Constructs a new BoundsString from this builder.
         *
         * @return the new BoundsString object.
         */
        public BoundsString build() {
            return new BoundsString(this);
        }
    }
}
