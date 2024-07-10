package com.github.natche.cyderutils.color;

import com.github.natche.cyderutils.range.RangeUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import java.awt.*;
import java.util.stream.IntStream;

/** An extension class on top of {@link Color} to add utility, mutation, and creation methods. */
public final class CyderColor extends Color {
    /** The length of a shorthand hex string without a leading hash. */
    private static final int shorthandHexLength = 3;

    /** The length of a full hex string without a leading hash. */
    private static final int hexLength = 6;

    /** The base for the hexadecimal number system. */
    private static final int hexBase = 16;

    /** The minimum value for a dimension of a rgb color. */
    private static final int minColor = 0;

    /** The maximum value for a dimension of a rgb color. */
    private static final int maxColor = 255;

    /** The range a dimension of a rgb color must fall within. */
    private static final Range<Integer> colorRange = Range.closed(minColor, maxColor);

    /**
     * Constructs a new CyderColor from the provided value for red, green, and blue.
     *
     * @param c the integer value to use for red, green, and blue
     * @throws IllegalArgumentException if the provided value is not in the range [0, 255]
     */
    public CyderColor(int c) {
        super(checkColorRange(c), checkColorRange(c), checkColorRange(c));
    }

    /**
     * Constructs a new CyderColor from the provided red, green, and blue values.
     *
     * @param red   the color's red value
     * @param green the color's green value
     * @param blue  the color's blue value
     * @throws IllegalArgumentException if any color is not in the range [0, 255]
     */
    public CyderColor(int red, int green, int blue) {
        super(checkColorRange(red), checkColorRange(green), checkColorRange(blue));
    }

    /**
     * Constructs a new CyderColor from the provided red, green, blue, and alpha values.
     *
     * @param red   the color's red value
     * @param green the color's green value
     * @param blue  the color's blue value
     * @param alpha the color's alpha value
     * @throws IllegalArgumentException if any color is not in the range [0, 255]
     */
    public CyderColor(int red, int green, int blue, int alpha) {
        super(checkColorRange(red), checkColorRange(green), checkColorRange(blue), checkColorRange(alpha));
    }

    /**
     * Constructs a new CyderColor from the provided hex string.
     * The hex string may be in short or long format and with or without a leading hash.
     *
     * @param hex the hex string such as "333", "#333", "6A6A6A", "#6A6A6A"
     * @throws NullPointerException     if the provided string is null
     * @throws IllegalArgumentException if the provided hex string is empty
     */
    public CyderColor(String hex) {
        this(parseColorFromHex(Preconditions.checkNotNull(hex)));
    }

    /**
     * Constructs a new CyderColor from the provided Color.
     *
     * @param color the color
     * @throws NullPointerException if the provided color is null
     */
    public CyderColor(Color color) {
        super(Preconditions.checkNotNull(color).getRed(),
                Preconditions.checkNotNull(color).getGreen(),
                Preconditions.checkNotNull(color).getBlue());
    }

    /**
     * Returns a new CyderColor based on this color but with the provided opacity
     *
     * @param opacity the opacity to set
     * @return a new CyderColor with the provided opacity
     * @throws IllegalArgumentException if the provided opacity value is out of the range of [0, 255]
     */
    public CyderColor withOpacity(int opacity) {
        checkColorRange(opacity);

        return new CyderColor(getRed(), getGreen(), getBlue(), opacity);
    }

    /**
     * Returns a new CyderColor object which is the inverse of this color.
     *
     * @return a new CyderColor object which is the inverse of this color
     */
    public CyderColor getInverse() {
        return new CyderColor(maxColor - getRed(), maxColor - getGreen(), maxColor - getBlue());
    }

    /**
     * Returns a new CyderColor object representing the grayscale color.
     *
     * @return a new CyderColor object representing the grayscale color
     */
    public CyderColor getGrayscale() {
        int red = getRed();
        int green = getGreen();
        int blue = getBlue();
        int luminance = (int) Math.round(0.2126 * red + 0.7152 * green + 0.0722 * blue);
        return new CyderColor(luminance);
    }

    /**
     * Merges the provided color with this color and returns a new CyderColor instance.
     *
     * @param color the color to merge with this color
     * @return a new instance representing the merged color
     * @throws NullPointerException if the provided color is null
     */
    public CyderColor merge(Color color) {
        Preconditions.checkNotNull(color);

        int r = color.getRed() + getRed();
        int g = color.getGreen() + getGreen();
        int b = color.getBlue() + getBlue();

        return new CyderColor(r / 2, g / 2, b / 2);
    }

    /**
     * Returns n number of colors to transition between this color and the provided color.
     *
     * @param transitionToColor   the color to transition to
     * @param numTransitionColors the number of transition colors to return
     * @return a list representing transition colors for animating a transition from this color to the provided color
     * @throws NullPointerException     if the provided transitionToColor is null
     * @throws IllegalArgumentException if numTransitionColors is less than or equal to zero,
     *                                  is greater than the color rgb max of 255,
     *                                  or if the provided transitionToColor is equal to this color
     */
    public ImmutableList<CyderColor> getTransitionColors(Color transitionToColor, int numTransitionColors) {
        Preconditions.checkNotNull(transitionToColor);
        Preconditions.checkArgument(numTransitionColors > 0);
        Preconditions.checkArgument(numTransitionColors <= 255);
        Preconditions.checkArgument(!equals(transitionToColor));

        int maxRedSteps = Math.abs(getRed() - transitionToColor.getRed());
        int maxGreenSteps = Math.abs(getGreen() - transitionToColor.getGreen());
        int maxBlueSteps = Math.abs(getBlue() - transitionToColor.getBlue());
        int maxPossibleDiscreteSteps = Math.min(maxRedSteps, maxGreenSteps);
        maxPossibleDiscreteSteps = Math.min(maxPossibleDiscreteSteps, maxBlueSteps);
        Preconditions.checkArgument(numTransitionColors <= maxPossibleDiscreteSteps);

        ImmutableList.Builder<CyderColor> builder = ImmutableList.builder();

        int ourRed = getRed();
        int ourGreen = getGreen();
        int ourBlue = getBlue();
        int otherRed = transitionToColor.getRed();
        int otherGreen = transitionToColor.getGreen();
        int otherBlue = transitionToColor.getBlue();

        float redStep = (otherRed - ourRed) / (float) numTransitionColors;
        float greenStep = (otherGreen - ourGreen) / (float) numTransitionColors;
        float blueStep = (otherBlue - ourBlue) / (float) numTransitionColors;

        IntStream.range(0, numTransitionColors).forEach(i -> {
            int transitionRed = ourRed + (int) (redStep * i);
            int transitionGreen = ourGreen + (int) (greenStep * i);
            int transitionBlue = ourBlue + (int) (blueStep * i);

            transitionRed = RangeUtil.constrainToRange(transitionRed, colorRange);
            transitionGreen = RangeUtil.constrainToRange(transitionGreen, colorRange);
            transitionBlue = RangeUtil.constrainToRange(transitionBlue, colorRange);

            builder.add(new CyderColor(transitionRed, transitionGreen, transitionBlue));
        });

        return builder.build();
    }

    /**
     * Returns whether the provided color value is within the {@link #colorRange}.
     *
     * @param colorValue the color value
     * @return whether the provided color value is within the {@link #colorRange}
     * @throws IllegalArgumentException if the value is outside the valid range
     */
    private static int checkColorRange(int colorValue) {
        Preconditions.checkArgument(colorRange.contains(colorValue));
        return colorValue;
    }

    /**
     * Parses a hexadecimal color from the provided hex string.
     * Examples include:
     * <ul>
     *     <li>#143</li>
     *     <li>#AFAFAF</li>
     *     <li>000</li>
     * </ul>
     * <p>
     * Shorthand notation means the first character is the same for the red value,
     * the second value the same for the green value, and likewise for blue.
     *
     * @param hex the hex string
     * @return a {@link Color} object parsed from the provided hex string
     * @throws NullPointerException     if the provided string is null
     * @throws IllegalArgumentException if the provided string is empty or pure whitespace
     */
    public static Color parseColorFromHex(String hex) {
        Preconditions.checkNotNull(hex);
        Preconditions.checkArgument(!hex.trim().isEmpty());

        hex = hex.replace("#", "");
        Preconditions.checkArgument(hex.length() == shorthandHexLength
                || hex.length() == hexLength);
        if (hex.length() == shorthandHexLength) {
            StringBuilder newHex = new StringBuilder();
            hex.chars().mapToObj(i -> (char) i)
                    .forEach(character -> newHex.append(character).append(character));
            hex = newHex.toString();
        }

        int r = Integer.valueOf(hex.substring(0, 2), hexBase);
        int g = Integer.valueOf(hex.substring(2, 4), hexBase);
        int b = Integer.valueOf(hex.substring(4, 6), hexBase);
        return new Color(r, g, b);
    }
}
