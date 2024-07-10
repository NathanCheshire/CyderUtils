package com.github.natche.cyderutils.bounds;

import com.github.natche.cyderutils.constants.HtmlTags;
import com.github.natche.cyderutils.font.CyderFonts;
import com.github.natche.cyderutils.strings.StringUtil;
import com.github.natche.cyderutils.utils.HtmlUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A String container for computing the dimensions required for a container to contain the text
 * of a String inside of it with optional properties of Font, maximum width, and maximum height;
 * By default there is no maximum width or height and the default font is {@link CyderFonts#DEFAULT_FONT_SMALL}
 */
@Immutable
public final class BoundsString {
    /** The pattern used to extract HTML tags from a String. */
    private static final Pattern tagPattern = Pattern.compile("(<[^>]+>)|([^<]+)");

    /** The index of the group for an {@link HtmlString} received by a {@link Matcher} using {@link #tagPattern}. */
    private static final int HTML_STRING_GROUP = 1;

    /** The index of the group for a {@link PlainString} received by a {@link Matcher} using {@link #tagPattern}. */
    private static final int PLAIN_STRING_GROUP = 2;

    /**
     * The maximum number of characters to check backwards in an attempt
     * to find a space to split a string at when it exceeds the maximum width.
     */
    private static final int CHECK_FOR_SPACE_BACKWARDS_LENGTH = 10;

    /** The text of this bounds string. */
    private final String text;

    /** The HTML styled text after computing the bounds and adjusting the text. */
    private String styledText;

    /** The width of this bounds string. */
    private double width;

    /** The height of this bounds string. */
    private double height;

    /** The font the label showing the text will use. */
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
     * @throws NullPointerException       if the provided builder is null
     * @throws BoundsComputationException if the computed width or height exceeds the maximum specifications
     */
    private BoundsString(Builder builder) {
        Preconditions.checkNotNull(builder);

        this.text = builder.text;
        this.font = builder.font;
        this.maxWidth = builder.maxWidth;
        this.maxHeight = builder.maxHeight;
        this.linePadding = builder.linePadding;
        this.context = new FontRenderContext(new AffineTransform(), font.isItalic(), true);

        calculateBounds();
    }

    /** Computes the bounds needed for this BoundsString. */
    private void calculateBounds() {
        if (HtmlUtil.containsHtmlStyling(text)) {
            calculateBoundsWithHtmlStyling();
        } else {
            calculateBoundsWithoutHtmlStyling();
        }
    }

    /**
     * Computes the bounds necessary to hold the text given the maximum specifications while taking into account
     * HTML styling such as opening and closing tags, break tags, style tags, div tags, etc.
     */
    private void calculateBoundsWithHtmlStyling() {
        ImmutableList<StringContainer> parts = splitHtml(text);
        double lineHeight = parts.stream()
                .filter((container) -> container instanceof PlainString)
                .map((container) -> font.getStringBounds(container.getString(), context).getHeight())
                .collect(ImmutableList.toImmutableList())
                .stream().mapToDouble(Double::doubleValue).max()
                .orElseThrow(() -> new BoundsComputationException("Could not compute max line height"));

        ArrayList<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        for (StringContainer part : parts) {
            String currentLineContent = currentLine.toString();
            String currentPartContent = part.getString();

            if (part instanceof HtmlString) {
                if (currentPartContent.equals(HtmlTags.breakTag)) {
                    lines.add(currentLineContent);
                    currentLine = new StringBuilder();
                } else {
                    currentLine.append(currentPartContent);
                }

                continue;
            }

            for (char c : currentPartContent.toCharArray()) {
                double currentLineWidth = getLineWidthIgnoringHtmlTags(currentLine.toString());
                double charWidth = getLineWidth(String.valueOf(c));
                double proposedWidth = currentLineWidth + charWidth;

                // The character will fit on this line
                if (proposedWidth < maxWidth) {
                    currentLine.append(c);
                    continue;
                }

                // The character will not fit on this line, so we need to find a suitable place to break the string
                int breakIndex = findBreakInsertionIndexIgnoringHtmlTags(currentLineContent);
                String fittingLine = currentLine.substring(0, breakIndex).stripTrailing();
                lines.add(fittingLine);

                String remainder = currentLine.substring(breakIndex).stripLeading();
                currentLine = new StringBuilder(remainder);
                currentLine.append(c);
            }
        }

        lines.add(currentLine.toString());

        double computedWidth = lines.stream()
                .mapToDouble(this::getLineWidthIgnoringHtmlTags).max()
                .orElseThrow(() -> new BoundsComputationException("Could not compute maximum width of lines"));
        double computedHeight = lines.size() * lineHeight + (lines.size() - 1) * linePadding;
        checkComputedBounds(computedWidth, computedHeight, lines);
    }

    /**
     * Computes the bounds necessary to hold the text given the maximum
     * specifications and the assumption that there is no HTML styling present.
     */
    private void calculateBoundsWithoutHtmlStyling() {
        double lineHeight = font.getStringBounds(text, context).getHeight();
        ArrayList<String> lines = new ArrayList<>();

        StringBuilder currentLine = new StringBuilder();
        for (char c : text.toCharArray()) {
            String currentLineContent = currentLine.toString();
            double currentLineWidth = getLineWidth(currentLineContent);
            double charWidth = getLineWidth(String.valueOf(c));
            double proposedWidth = currentLineWidth + charWidth;

            // The character will fit on this line
            if (proposedWidth < maxWidth) {
                currentLine.append(c);
                continue;
            }

            // The character will not fit on this line, so we need to find a suitable place to break the string
            int breakIndex = findBreakInsertionIndex(currentLineContent);
            String fittingLine = currentLine.substring(0, breakIndex).stripTrailing();
            lines.add(fittingLine);

            String remainder = currentLine.substring(breakIndex).stripLeading();
            currentLine = new StringBuilder(remainder);
            currentLine.append(c);
        }

        lines.add(currentLine.toString());

        double computedWidth = lines.stream()
                .mapToDouble(this::getLineWidth).max()
                .orElseThrow(() -> new BoundsComputationException("Could not compute maximum width of lines"));
        double computedHeight = lines.size() * lineHeight + (lines.size() - 1) * linePadding;
        checkComputedBounds(computedWidth, computedHeight, lines);
    }

    /**
     * Checks the computed width and height (bounds) of the internal text and either
     * throws an exception if a dimension exceeds the maximum set value for that dimension
     * or sets the internal state of this instance.
     *
     * @param computedWidth  the computed necessary width
     * @param computedHeight the computed necessary height
     * @param lines          the computed lines
     */
    private void checkComputedBounds(double computedWidth, double computedHeight, List<String> lines) {
        if (computedWidth > maxWidth) {
            throw new BoundsComputationException("Computed width exceeds max width, "
                    + computedWidth + " > " + maxWidth);
        }
        if (computedHeight > maxHeight) {
            throw new BoundsComputationException("Computed height exceeds max height, "
                    + computedHeight + " > " + maxHeight);
        }

        this.width = computedWidth;
        this.height = computedHeight;

        StringBuilder styledTextBuilder = new StringBuilder();
        if (!lines.get(0).startsWith(HtmlTags.openingHtml)) {
            styledTextBuilder.append(HtmlTags.openingHtml);
        }
        styledTextBuilder.append(StringUtil.joinParts(lines, HtmlTags.breakTag));
        if (!styledTextBuilder.toString().endsWith(HtmlTags.closingHtml)) {
            styledTextBuilder.append(HtmlTags.closingHtml);
        }

        this.styledText = styledTextBuilder.toString();
    }

    /**
     * Attempts to find an index to break the provided string into two
     * separate strings at due to it exceeding some specified width.
     *
     * @param string the string to find a point to split the at
     * @return the index to split the string at
     */
    private int findBreakInsertionIndex(String string) {
        int length = string.length();
        int lookBackChars = Math.min(CHECK_FOR_SPACE_BACKWARDS_LENGTH, length);
        int currentIndex = length - 1;

        while (currentIndex > length - lookBackChars) {
            if (string.charAt(currentIndex) == ' ') return currentIndex;
            currentIndex--;
        }

        return length - 1;
    }

    /**
     * Attempts to find an index to break the provided string into two
     * separate strings at due to it exceeding some specified width.
     * This method handles inline HTML tags too.
     *
     * @param string the string to find a point to split at
     * @return the index to split the string at
     */
    private int findBreakInsertionIndexIgnoringHtmlTags(String string) {
        int length = string.length();

        int currentIndex = length - 1;
        boolean inTag = false;
        int checkedChars = 0;
        while (checkedChars < CHECK_FOR_SPACE_BACKWARDS_LENGTH || currentIndex == 0) {
            if (string.charAt(currentIndex) == '>') {
                inTag = true;
            } else if (string.charAt(currentIndex) == '<') {
                inTag = false;
            } else if (string.charAt(currentIndex) == ' ' && !inTag) {
                return currentIndex;
            } else if (!inTag) {
                // A non-html/non-plain text char was checked but was not a space
                checkedChars++;
            }

            currentIndex--;
        }

        return length - 1;
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

    /**
     * Returns the needed width for the internal font to render the provided line ignoring HTML tags.
     * Break tags should not be passed to this method for obvious reasons.
     *
     * @param line the line possibly with styled HTML tags
     * @return the necessary width
     */
    private double getLineWidthIgnoringHtmlTags(String line) {
        ImmutableList<String> plainParts = splitHtml(line).stream()
                .filter((container) -> container instanceof PlainString)
                .map(StringContainer::getString).collect(ImmutableList.toImmutableList());
        String joined = StringUtil.joinParts(plainParts, "");
        return getLineWidth(joined);
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
        return styledText;
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
     * Returns the necessary width and height for a container to contain the string text.
     * Note that the internal units use floating points, specifically the {@link Double} data type.
     * The ceiling of each is returned when converting to a {@link Dimension} object. For more
     * precise calculations, see {@link #getWidth()} and {@link #getHeight()}.
     *
     * @return the necessary width and height for a container to contain the string text
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
        ret = 31 * ret + font.hashCode();
        ret = 31 * ret + Double.hashCode(maxWidth);
        ret = 31 * ret + Double.hashCode(maxHeight);
        ret = 31 * ret + Integer.hashCode(linePadding);
        return ret;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "BoundsString{"
                + "text=\"" + text + "\", "
                + "width=" + width + ", "
                + "height=" + height + ", "
                + "font=" + font + ", "
                + "maxWidth=" + maxWidth + ", "
                + "maxHeight=" + maxHeight + ", "
                + "linePadding=" + linePadding
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
                && height == other.getHeight()
                && font.equals(other.font)
                && maxWidth == other.maxWidth
                && maxHeight == other.maxHeight
                && linePadding == other.linePadding;
    }

    /** A builder for constructing a BoundsString object. */
    public static final class Builder {
        /** The default line separation height. */
        public static final int DEFAULT_LINE_PADDING = 5;

        private final String text;
        private Font font = CyderFonts.DEFAULT_FONT_SMALL;
        private int maxWidth = Integer.MAX_VALUE;
        private int maxHeight = Integer.MAX_VALUE;
        private int linePadding = DEFAULT_LINE_PADDING;

        /**
         * Constructs a new builder for a BoundsString.
         * The string text may contain some HTML styling, examples include
         * <ul>
         *     <li>HTML opening and closing tags</li>
         *     <li>line breaks</li>
         *     <li>div tags with styling such as color, text align, etc.</li>
         *     <li>JSX will be stripped and will not affect the resulting bounds</li>
         * </ul>
         *
         * @param text the string text
         * @throws NullPointerException if the provided text is null
         */
        public Builder(String text) {
            Preconditions.checkNotNull(text);

            this.text = text;
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
            Preconditions.checkArgument(linePadding >= 0);
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
