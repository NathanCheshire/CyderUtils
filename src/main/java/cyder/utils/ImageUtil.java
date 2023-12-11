package cyder.utils;

import com.google.common.base.Preconditions;
import cyder.enumerations.Direction;
import cyder.image.CyderImage;
import cyder.process.Program;
import cyder.threads.CyderThreadFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Static utility methods revolving around Image manipulation.
 */
public final class ImageUtil {
    /**
     * The name of the thread which blurs an image.
     */
    private static final String GAUSSIAN_IMAGE_BLURER_THREAD_NAME = "Gaussian Image Blurer Thread";

    /**
     * Returns an image gradient following the provided parameters.
     *
     * @param width        the width of the resulting image
     * @param height       the height of the resulting image
     * @param shadeColor   the color to mix/shade in when merging the left and right colors
     * @param primaryRight the primary color for the left
     * @param primaryLeft  the primary color for the left
     * @return an image gradient
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public static BufferedImage getImageGradient(int width, int height,
                                                 Color shadeColor, Color primaryRight, Color primaryLeft) {
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);
        Preconditions.checkNotNull(shadeColor);
        Preconditions.checkNotNull(primaryLeft);
        Preconditions.checkNotNull(primaryRight);

        BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = ret.createGraphics();

        GradientPaint primary = new GradientPaint(0f, 0f, primaryLeft, height, 0f, primaryRight);
        GradientPaint shade = new GradientPaint(0f, 0f, new Color(shadeColor.getRed(),
                shadeColor.getGreen(), shadeColor.getBlue(), 0), 0f, 600, shadeColor);
        g2.setPaint(primary);
        g2.fillRect(0, 0, width, height);
        g2.setPaint(shade);
        g2.fillRect(0, 0, width, height);

        g2.dispose();

        return ret;
    }

    /**
     * Returns the provided image file after applying a gaussian blur to it.
     *
     * @param imageFile the image file to blur and output a blurred copy in the same directory
     * @param radius    the radius of the Gaussian blur
     * @return the provided image file after applying a gaussian blur
     */
    public static Future<Optional<File>> gaussianBlur(File imageFile, int radius) {
        Preconditions.checkNotNull(imageFile);
        Preconditions.checkArgument(imageFile.exists());
        Preconditions.checkArgument(radius > 2);
        Preconditions.checkArgument(radius % 2 != 0);
        Preconditions.checkState(Program.PYTHON.isInstalled());

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory(GAUSSIAN_IMAGE_BLURER_THREAD_NAME)).submit(() -> {
            // todo need to do this logic on our own now
            return Optional.empty();
        });
    }
}
