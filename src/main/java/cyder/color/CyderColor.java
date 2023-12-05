package cyder.color;

import com.google.common.base.Preconditions;

import java.awt.*;
import java.lang.reflect.Field;

/**
 * An abstraction class on top of {@link Color} to add utility and mutation methods.
 */
public final class CyderColor extends Color {
    private static final int shorthandHexLength = 3;
    private static final int hexLength = 6;
    private static final int hexBase = 16;
    private static final int minColor = 0;
    private static final int maxColor = 255;

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
     * Constructs a new CyderColor from the provided hex string.
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
     * Sets the opacity of this color to the provided opacity.
     *
     * @param opacity the opacity to set
     * @throws IllegalArgumentException if the provided opacity value is out of the range of [0, 255]
     */
    public void setOpacity(int opacity) {
        checkColorRange(opacity);

        try {
            Field redField = Color.class.getDeclaredField("value");
            redField.setAccessible(true);
            Color newColor = new Color(getRed(), getGreen(), getBlue(), opacity);
            int newRgb = newColor.getRGB();
            redField.setInt(this, newRgb);
        } catch (NoSuchFieldException e) {
            throw new CyderColorException(
                    "Failed to find field \"value\" in java.awt.Color class, e=" + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new CyderColorException("Unable to access field \"value\", e=" + e.getMessage());
        }
    }

    /**
     * Returns a new CyderColor object which is the inverse of this color.
     *
     * @return a new CyderColor object which is the inverse of this color
     */
    public CyderColor getInverseColor() {
        return new CyderColor(maxColor - getRed(), maxColor - getGreen(), maxColor - getBlue());
    }

    /**
     * Merges the provided color with this color and returns a new CyderColor instance.
     *
     * @param color the color to merge with this color
     * @return a new instance representing the merged color
     * @throws NullPointerException if the provided color is null
     */
    public CyderColor getMergedColor(CyderColor color) {
        Preconditions.checkNotNull(color);

        int r = color.getRed() + getRed();
        int g = color.getGreen() + getGreen();
        int b = color.getBlue() + getBlue();

        return new CyderColor(r / 2, g / 2, b / 2);
    }

    /**
     * Returns whether the provided color value is within the range [0, 255].
     *
     * @param colorValue the color value
     * @return whether the provided color value is within the range [0, 255]
     * @throws IllegalArgumentException if the value is outside the valid range
     */
    private static int checkColorRange(int colorValue) {
        Preconditions.checkArgument(colorValue >= minColor && colorValue <= maxColor);
        return colorValue;
    }

    /**
     * Parses a hexadecimal color from the provided hex string.
     * Examples include:
     * <ul>
     *     <li>#341</li>
     *     <li>#AFAFAF</li>
     *     <li>000</li>
     * </ul>
     *
     * @param hex the hex string
     * @return a {@link Color} object parsed from the provided hex string
     */
    private static Color parseColorFromHex(String hex) {
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
