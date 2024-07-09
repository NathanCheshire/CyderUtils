package com.github.natche.cyderutils.bounds;

import com.github.natche.cyderutils.font.CyderFonts;
import com.github.natche.cyderutils.utils.HtmlUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A String container for computing the dimensions required for a container to contain the content
 * of a String inside of it with optional properties of Font, maximum width, and maximum height;
 * By default there is no maximum width or height and the default font is {@link CyderFonts#DEFAULT_FONT_SMALL}
 */
@Immutable
public final class BoundsString {
    /** The pattern used to extract HTML tags from a String. */
    private static final Pattern tagPattern = Pattern.compile("(<[^>]+>)|([^<]+)");

    private static final int HTML_STRING_GROUP = 1;
    private static final int PLAIN_STRING_GROUP = 2;
    private static final int HEIGHT_CHECK_CHAR_COUNT = 10;
    private static final int CHECK_FOR_SPACE_BACKWARDS_LENGTH = 10;

    /** The text of this bounds string. */
    private final String text;

    /** The width of this bounds string. */
    private double width;

    /** The height of this bounds string. */
    private double height;

    /** The font the label showing the content will use. */
    private final Font font;

    /** The maximum allowable width. */
    private final int maxWidth;

    /** The maximum allowable height. */
    private final int maxHeight;

    /** The vertical padding between lines. */
    private final int linePadding;

    /** The font render context based on the provided font. */
    private final FontRenderContext context;

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
        this.linePadding = builder.linePadding;
        this.context = new FontRenderContext(new AffineTransform(), font.isItalic(), true);

        calculateBounds();
    }

    /** Computes the bounds needed for this BoundsString. */
    private void calculateBounds() {
        String heightCheckChars = text.substring(0, Math.min(text.length(), HEIGHT_CHECK_CHAR_COUNT));
        double lineHeight = font.getStringBounds(heightCheckChars, context).getHeight();

        if (HtmlUtil.containsHtmlStyling(text)) {
            calculateBoundsWithHtmlStyling(lineHeight);
        } else {
            calculateBoundsWithoutHtmlStyling(lineHeight);
        }
    }

    private void calculateBoundsWithHtmlStyling(double lineHeight) {
        ImmutableList<StringContainer> parts = splitHtml(text);
    }

    private void calculateBoundsWithoutHtmlStyling(double lineHeight) {
        ArrayList<String> lines = new ArrayList<>();

        StringBuilder currentLine = new StringBuilder();
        for (char c : text.toCharArray()) {
            double currentLineWidth = getLineWidth(currentLine.toString());
            double charWidth = getLineWidth(String.valueOf(c));
            double proposedWidth = currentLineWidth + charWidth;

            if (proposedWidth == maxWidth) {
                currentLine.append(c);
                lines.add(currentLine.toString());
                currentLine = new StringBuilder();
            } else if (proposedWidth < maxWidth) {
                currentLine.append(c);
            } else {
                // check backwards some chars for a space, if we cannot find one,
                // break here since this char will push us over
                int breakChar = -1;
                for (int i = currentLine.length() - 1 ;
                     i > currentLine.length() - CHECK_FOR_SPACE_BACKWARDS_LENGTH ; i--) {
                    if (currentLine.charAt(i) == ' ') {
                        breakChar = i;
                        break;
                    }
                }

                if (breakChar == -1) {
                    // Could not find a space to break at so here will do
                    currentLine.append(c);
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                } else {
                    // Found a space so split and eliminate there
                    String fittingLine = currentLine.substring(0, breakChar);
                    String remainder = currentLine.substring(breakChar + 1, currentLine.length() - 1);
                    lines.add(fittingLine);
                    currentLine = new StringBuilder(remainder);
                }
            }
        }

        // now find max width and then sum heights plus padding
        double necessaryWidth = maxWidth;
        for (String line : lines) {
            necessaryWidth = Math.min(getLineWidth(line), necessaryWidth);
        }
        double necessaryHeight = lines.size() * lineHeight + (lines.size() - 1) * linePadding;

        // todo throw if this is too big? idk

        this.width = necessaryWidth;
        this.height = necessaryHeight;
    }

    /**
     * Returns the needed width for the internal font to render the provided line.
     *
     * @param line the line
     * @return the necessary width
     */
    private double getLineWidth(String line) {
        return font.getStringBounds(line, context).getWidth();
    }

    private static ImmutableList<StringContainer> splitHtml(String input) {
        ImmutableList.Builder<StringContainer> parts = ImmutableList.builder();

        Matcher matcher = tagPattern.matcher(input);
        while (matcher.find()) {
            if (matcher.group(HTML_STRING_GROUP) != null) {
                parts.add(new HtmlString(matcher.group(HTML_STRING_GROUP)));
            }
            if (matcher.group(PLAIN_STRING_GROUP) != null) {
                parts.add(new PlainString(matcher.group(PLAIN_STRING_GROUP)));
            }
        }

        return parts.build();
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
    public double getWidth() {
        return width;
    }

    /**
     * Returns the height for this bounds string.
     *
     * @return the height for this bounds string
     */
    public double getHeight() {
        return height;
    }

    /**
     * Returns the necessary width and height for a container to contain the string content.
     * Note that the internal units use floating points, specifically the {@link Double} data type.
     * The ceiling of each is returned when converting to a {@link Dimension} object. For more
     * precise calculations, see {@link #getWidth()} and {@link #getHeight()}.
     *
     * @return the necessary width and height for a container to contain the string content
     */
    public Dimension getSize() {
        return new Dimension((int) Math.ceil(width), (int) Math.ceil(height));
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int ret = text.hashCode();
        ret = 31 * ret + Double.hashCode(width);
        ret = 31 * ret + Double.hashCode(height);
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
        /** The default line separation height. */
        public static final int DEFAULT_LINE_PADDING = 5;

        private final String content;
        private Font font = CyderFonts.DEFAULT_FONT_SMALL;
        private int maxWidth = Integer.MAX_VALUE;
        private int maxHeight = Integer.MAX_VALUE;
        private int linePadding = DEFAULT_LINE_PADDING;

        /**
         * Constructs a new builder for a BoundsString.
         * The string content may contain some HTML styling, examples include
         * <ul>
         *     <li>HTML opening and closing tags</li>
         *     <li>line breaks</li>
         *     <li>div tags with styling such as color, text align, etc.</li>
         *     <li>JSX will be stripped and will not affect the resulting bounds</li>
         * </ul>
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
         * Sets the y padding between lines of text.
         *
         * @param linePadding the y padding
         * @return this builder
         * @throws IllegalArgumentException if the provided height is less than 0
         */
        public Builder setLinePadding(int linePadding) {
            Preconditions.checkArgument(maxHeight >= 0);
            this.linePadding = linePadding;
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
