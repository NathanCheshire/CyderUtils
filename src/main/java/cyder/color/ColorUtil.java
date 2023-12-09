package cyder.color;

import com.google.common.collect.Range;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import cyder.utils.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utilities to color operations and conversions.
 */
public final class ColorUtil {
    /**
     * The maximum length the hashmap for the get dominant color method can grow.
     * This ensures we are only checking the top 100 colors of an image when determining the dominant color.
     */
    private static final int maxDominantColorCounterHashMapLength = 100;

    /**
     * The range for opacity values for Java's {@link Color} objects.
     */
    public static final Range<Integer> opacityRange = Range.closed(0, 255);

    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private ColorUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns the dominant color of the provided BufferedImage.
     *
     * @param image the image to find the dominant color of
     * @return the dominant color of the provided image
     */
    public static Color getDominantColor(BufferedImage image) {
        checkNotNull(image);

        Map<Integer, Integer> colorCounter = new HashMap<>(maxDominantColorCounterHashMapLength);

        for (int x = 0 ; x < image.getWidth() ; x++) {
            for (int y = 0 ; y < image.getHeight() ; y++) {
                int currentRGB = image.getRGB(x, y);
                int count = colorCounter.getOrDefault(currentRGB, 0);
                colorCounter.put(currentRGB, count + 1);
            }
        }

        return getDominantColor(colorCounter);
    }

    /**
     * Calculates the opposite dominant color of the provided image.
     *
     * @param image the provided image to calculate the dominant color inverse of
     * @return the opposite of the dominant color of the provided image
     */
    public static Color getDominantColorInverse(ImageIcon image) {
        checkNotNull(image);

        return new CyderColor(getDominantColor(ImageUtil.toBufferedImage(image))).getInverseColor();
    }

    /**
     * Returns the gray-scale text color which should be used when overlaying
     * text on the provided buffered image.
     *
     * @param bi the buffered image
     * @return the gray-scale text color to use
     */
    public static Color getSuitableOverlayTextColor(BufferedImage bi) {
        checkNotNull(bi);

        // todo return getInverseColor(getDominantGrayscaleColor(bi));
        return null;
    }

    /**
     * Returns the dominant color of the provided buffered image gray-scaled.
     *
     * @param bi the buffered image
     * @return the closest gray-scale color the provided buffered image's dominant color
     */
    public static Color getDominantGrayscaleColor(BufferedImage bi) {
        checkNotNull(bi);

        Color dominant = getDominantColor(bi);
        int avg = (dominant.getRed() + dominant.getGreen() + dominant.getBlue()) / 3;
        return new Color(avg, avg, avg);
    }

    /**
     * Finds the dominant color of the provided color counter.
     * Used for calculating the dominant color of an image.
     *
     * @param colorCounter the color counter object to use to calculate the dominant rgb value
     * @return the dominant color
     */
    @SuppressWarnings({"ComparatorMethodParameterNotUsed", "OptionalGetWithoutIsPresent"})
    private static Color getDominantColor(Map<Integer, Integer> colorCounter) {
        checkNotNull(colorCounter);

        int dominantRGB = colorCounter.entrySet().stream()
                .max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1)
                .get()
                .getKey();

        return new Color(dominantRGB);
    }
}
