package com.github.natche.cyderutils.image;

import com.github.natche.cyderutils.color.CyderColor;
import com.github.natche.cyderutils.constants.CyderRegexPatterns;
import com.github.natche.cyderutils.enumerations.Direction;
import com.github.natche.cyderutils.files.FileUtil;
import com.github.natche.cyderutils.math.Angle;
import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/** An image abstraction for usage throughout Cyder. */
public final class CyderImage {
    private static final Logger logger = LoggerFactory.getLogger(CyderImage.class);

    /** The range that a percentage must fall into. */
    private static final Range<Float> percentageRange = Range.closed(0.0f, 100.0f);

    /** A bitmask for a bit. */
    private static final int EIGHT_BIT_MASK = 0xff;

    /** The amount to shift a number by to obtain the alpha. */
    private static final int ALPHA_SHIFT = 24;

    /** The amount to shift a number by to obtain the red. */
    private static final int RED_SHIFT = 16;

    /** The amount to shift a number by to obtain the green. */
    private static final int GREEN_SHIFT = 8;

    /** The default color counter the dominant color contained in this image hashmap max length. */
    private static final int DEFAULT_COLOR_COUNTER_MAX_LENGTH = 100;

    /** The encapsulated image. */
    private BufferedImage image;

    /** The color counter hashmap's max length. */
    private int colorCounterMaxLength = DEFAULT_COLOR_COUNTER_MAX_LENGTH;

    /**
     * Constructs a new CyderImage from the provided BufferedImage.
     *
     * @param image the image
     * @throws NullPointerException if the provided image is null
     */
    private CyderImage(BufferedImage image) {
        Preconditions.checkNotNull(image);

        this.image = image;
    }

    /**
     * Constructs and returns a new CyderImage, read from the provided URL.
     *
     * @param url the URL to read the image from
     * @return a new CyderImage, read from the provided URL
     * @throws NullPointerException     if the provided URL is null
     * @throws IllegalArgumentException if the provided URL is empty or invalid
     * @throws IOException              if an exception occurs reading the image from the provided URL
     */
    public static CyderImage fromUrl(String url) throws IOException {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.trim().isEmpty());
        Preconditions.checkArgument(CyderRegexPatterns.urlFormationPattern.matcher(url).matches());

        URL imageUrl = new URL(url);
        BufferedImage img = ImageIO.read(imageUrl);
        return new CyderImage(img);
    }

    /**
     * Constructs and returns a new CyderImage, read from the provided file.
     *
     * @param file the file to read the image from
     * @return a new CyderImage, read from the provided file
     * @throws NullPointerException     if the provided file is null
     * @throws IllegalArgumentException if the provided file does not exist or is not a file
     * @throws IOException              if an exception occurs reading the image from the provided file
     */
    public static CyderImage fromFile(File file) throws IOException {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(file.isFile());

        BufferedImage img = ImageIO.read(file);
        return new CyderImage(img);
    }

    /**
     * Constructs and returns a new CyderImage from the provided color.
     *
     * @param color  the color of the image
     * @param width  the width of the image
     * @param height the height of the image
     * @return a new CyderImage from the provided color
     * @throws NullPointerException     if the provided color is null
     * @throws IllegalArgumentException if the width or height is less than one
     */
    public static CyderImage fromColor(Color color, int width, int height) {
        Preconditions.checkNotNull(color);
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setPaint(color);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();
        return new CyderImage(image);
    }

    /**
     * Constructs and returns a new CyderImage from the provided colors for a gradient base image.
     *
     * @param shadeColor   the shade color
     * @param primaryRight the primary right color
     * @param primaryLeft  the primary left color
     * @param width        the width for the image
     * @param height       the height for the image
     * @return a new CyderImage
     * @throws NullPointerException     if any color is null
     * @throws IllegalArgumentException if any dimension is less than or equal to zero
     */
    public static CyderImage fromGradient(Color shadeColor, Color primaryRight, Color primaryLeft,
                                          int width, int height) {
        Preconditions.checkNotNull(shadeColor);
        Preconditions.checkNotNull(primaryRight);
        Preconditions.checkNotNull(primaryLeft);
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);

        BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = ret.createGraphics();

        @SuppressWarnings("SuspiciousNameCombination") // Understood
        GradientPaint primary = new GradientPaint(0f, 0f, primaryLeft, height, 0f, primaryRight);
        GradientPaint shade = new GradientPaint(0f, 0f, new Color(shadeColor.getRed(),
                shadeColor.getGreen(), shadeColor.getBlue(), 0), 0f, 600, shadeColor);
        g2.setPaint(primary);
        g2.fillRect(0, 0, width, height);
        g2.setPaint(shade);
        g2.fillRect(0, 0, width, height);
        g2.dispose();

        return new CyderImage(ret);
    }

    /**
     * Constructs and returns a new CyderImage from the provided BufferedImage.
     *
     * @param image the BufferedImage to copy for this CyderImage
     * @return a new CyderImage from the provided BufferedImage
     * @throws NullPointerException if the provided BufferedImage is null
     */
    public static CyderImage fromBufferedImage(BufferedImage image) {
        Preconditions.checkNotNull(image);

        return new CyderImage(copy(image));
    }

    /**
     * Constructs and returns a new CyderImage from the provided ImageIcon.
     *
     * @param icon the ImageIcon to copy for this CyderImage
     * @return a new CyderImage from the provided ImageIcon
     * @throws NullPointerException if the provided ImageIcon is null
     */
    public static CyderImage fromImageIcon(ImageIcon icon) {
        Preconditions.checkNotNull(icon);

        BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = image.createGraphics();
        icon.paintIcon(null, g, 0, 0);
        g.dispose();

        return new CyderImage(image);
    }

    /**
     * Constructs and returns a new CyderImage from the provided Component
     * via invoking {@link BufferedImage#getGraphics()} and painting the component onto the new image.
     *
     * @param component the component to capture
     * @return a new CyderImage from the provided component
     * @throws NullPointerException if the provided component is null
     */
    public static CyderImage fromComponent(Component component) {
        Preconditions.checkNotNull(component);

        BufferedImage image = new BufferedImage(
                component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_RGB);
        component.paint(image.getGraphics());

        return new CyderImage(image);
    }

    /**
     * Returns a new BufferedImage, copied from the internal image.
     *
     * @return a new BufferedImage, copied from the internal image
     */
    public BufferedImage getBufferedImage() {
        return copy(image);
    }

    /**
     * Copies the provided buffered image and returns a new image.
     *
     * @param image the image to copy
     * @return a new copied buffered image
     * @throws NullPointerException if the provided image is null
     */
    public static BufferedImage copy(BufferedImage image) {
        Preconditions.checkNotNull(image);

        BufferedImage copy = new BufferedImage(image.getWidth(),
                image.getHeight(),
                image.getType());
        Graphics2D graphics = copy.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        return copy;
    }

    /**
     * Returns a new ImageIcon, copied from the internal image.
     *
     * @return a new ImageIcon, copied from the internal image
     */
    public ImageIcon getImageIcon() {
        return new ImageIcon(image);
    }

    /**
     * Crops this image to the provided (x, y) as the new top-left coordinate with the provided width and height.
     *
     * @param x      the top left x coordinate
     * @param y      the top left y coordinate
     * @param width  the width of the image
     * @param height the height of the image
     * @throws IllegalArgumentException if the x or y values are less than zero,
     *                                  or the x or y values are greater than the width or height respectively,
     *                                  or the requested width plus the requested x exceeds the image bounds,
     *                                  or the requested height plus the requested y exceeds the image bounds
     */
    public void crop(int x, int y, int width, int height) {
        Preconditions.checkArgument(x >= 0);
        Preconditions.checkArgument(y >= 0);
        Preconditions.checkArgument(x + width < image.getWidth());
        Preconditions.checkArgument(y + height < image.getHeight());

        if (x == 0 && y == 0
                && width == image.getWidth()
                && height == image.getHeight()) return;

        if (x + width > image.getWidth()) {
            x = 0;
            width = image.getWidth();
        }

        if (y + height > image.getHeight()) {
            y = 0;
            height = image.getHeight();
        }

        image = image.getSubimage(x, y, width, height);
    }

    /**
     * Crops the image to the provided width and height from an (x, y) of (0, 0).
     *
     * @param width  the width of the image
     * @param height the height of the image
     * @throws IllegalArgumentException if the provided width or height is less than zero
     */
    public void crop(int width, int height) {
        Preconditions.checkArgument(width >= 0);
        Preconditions.checkArgument(height >= 0);

        crop(0, 0, width, height);
    }

    /**
     * Rotates this image to the provided direction.
     *
     * @param direction the direction to rotate this image to
     * @throws NullPointerException if the provided direction is null
     */
    public void rotate(Direction direction) {
        Preconditions.checkNotNull(direction);

        switch (direction) {
            case TOP -> rotate(Angle.ZERO.getDegrees());
            case RIGHT -> rotate(Angle.NINETY.getDegrees());
            case BOTTOM -> rotate(Angle.ONE_EIGHTY.getDegrees());
            case LEFT -> rotate(Angle.TWO_SEVENTY.getDegrees());
        }
        ;
    }

    /**
     * Rotates this image by the provided degree amount.
     *
     * @param degrees the degrees to rotate the internal image by, this angle will be normalized between [0, 360)
     */
    public void rotate(double degrees) {
        degrees = Angle.normalize360(degrees);
        double rads = Math.toRadians(degrees);

        double sin = Math.abs(Math.sin(rads));
        double cos = Math.abs(Math.cos(rads));

        int width = image.getWidth();
        int height = image.getHeight();

        int newWidth = (int) Math.floor(width * cos + height * sin);
        int newHeight = (int) Math.floor(height * cos + width * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - width) / 2.0, (newHeight - height) / 2.0);

        at.rotate(rads, width / 2.0, height / 2.0);
        g2d.setTransform(at);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        image = rotated;
    }

    /** Crops this image to the maximum square size. */
    @SuppressWarnings("SuspiciousNameCombination") /* Cropping logic */
    public void cropToMaximumSquare() {
        int width = image.getWidth();
        int height = image.getHeight();

        if (width < height) {
            crop(0, (height - width) / 2, width, width);
        } else if (height < width) {
            crop((width - height) / 2, 0, height, height);
        } else {
            crop(0, 0, width, height);
        }

        int sideLength = Math.min(width, height);
        resizeImage(sideLength, sideLength);
    }

    /**
     * Resizes this image to the requested dimensions.
     *
     * @param width  the width to resize this image to
     * @param height the height to resize this image to
     * @throws IllegalArgumentException if the provided width or height is less than one
     */
    public void resizeImage(int width, int height) {
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);

        Image transferImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        image = new BufferedImage(transferImage.getWidth(null),
                transferImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.drawImage(transferImage, 0, 0, null);
        graphics.dispose();
    }

    /**
     * Returns whether this image is of a landscape orientation.
     *
     * @return whether this image is of a landscape orientation
     */
    public boolean isLandscape() {
        return image.getWidth() > image.getHeight();
    }

    /**
     * Returns whether this image is of a portrait orientation.
     *
     * @return whether this image is of a portrait orientation
     */
    public boolean isPortrait() {
        return image.getHeight() > image.getWidth();
    }

    /**
     * Returns whether this image is a square.
     *
     * @return whether this image is a square
     */
    public boolean isSquare() {
        return image.getWidth() == image.getHeight();
    }

    /**
     * Sets the color counter max length.
     *
     * @param colorCounterMaxLength the color counter max length value
     * @throws IllegalArgumentException if the provided value is less than {@link #DEFAULT_COLOR_COUNTER_MAX_LENGTH}
     */
    public void setColorCounterMaxLength(int colorCounterMaxLength) {
        Preconditions.checkArgument(colorCounterMaxLength >= DEFAULT_COLOR_COUNTER_MAX_LENGTH);
        this.colorCounterMaxLength = colorCounterMaxLength;
    }

    /**
     * Returns the dominant color contained in this image.
     *
     * @return the dominant color contained in this image
     */
    public CyderColor getDominantColor() {
        Map<Integer, Integer> colorCounter = new HashMap<>(colorCounterMaxLength);

        for (int x = 0 ; x < image.getWidth() ; x++) {
            for (int y = 0 ; y < image.getHeight() ; y++) {
                int currentRGB = image.getRGB(x, y);
                int count = colorCounter.getOrDefault(currentRGB, 0);
                colorCounter.put(currentRGB, count + 1);
            }
        }

        int dominantRgb = colorCounter
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow(() -> new CyderImageException("Failed to compute dominant color"))
                .getKey();

        return new CyderColor(new Color(dominantRgb));
    }

    /**
     * Returns the grayscale text color which should be used when overlaying
     * text on this image
     *
     * @return the grayscale text color to use
     */
    public CyderColor getSuitableOverlayTextColor() {
        return getDominantColorInverse().getGrayscale();
    }

    /**
     * Returns the inverse of the dominant color of this image.
     *
     * @return the inverse of the dominant color of this image
     */
    public CyderColor getDominantColorInverse() {
        return getDominantColor().getInverseColor();
    }

    /**
     * Pixelates this internal image.
     *
     * @param pixelSize the number of old pixels to represent a single new pixel
     * @throws IllegalArgumentException if the provided pixel size is less than one
     *                                  or greater than the minimum image dimension
     */
    public void pixelateImage(int pixelSize) {
        Preconditions.checkArgument(pixelSize > 1);
        Preconditions.checkArgument(pixelSize <= image.getWidth());
        Preconditions.checkArgument(pixelSize <= image.getHeight());

        BufferedImage pixelateImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

        for (int y = 0 ; y < image.getHeight() ; y += pixelSize) {
            for (int x = 0 ; x < image.getWidth() ; x += pixelSize) {
                CyderImage cyderImage = fromBufferedImage(image);
                cyderImage.crop(x, y, pixelSize, pixelSize);
                Color dominantColor = cyderImage.getDominantColor();

                for (int yd = y ; (yd < y + pixelSize) && (yd < pixelateImage.getHeight()) ; yd++) {
                    for (int xd = x ; (xd < x + pixelSize) && (xd < pixelateImage.getWidth()) ; xd++) {
                        pixelateImage.setRGB(xd, yd, dominantColor.getRGB());
                    }
                }

            }
        }
    }

    /**
     * Ensures the internal image fits within the provided length x length.
     *
     * @param length the length each dimension of this image should fit within
     * @return whether the image was resized if it did not fit within length x length
     * @throws IllegalArgumentException if the provided length is less than or equal to zero
     */
    @CanIgnoreReturnValue
    public boolean ensureFitsInBounds(int length) {
        Preconditions.checkArgument(length > 0);
        return ensureFitsInBounds(length, length);
    }

    /**
     * Ensures the internal image fits within the provided width x height.
     *
     * @param width  the width the image should fit within
     * @param height the height the image should fit within
     * @return whether the image was resized if it did not fit within width x height
     * @throws IllegalArgumentException if the provided width or height is less than or equal to zero
     */
    public boolean ensureFitsInBounds(int width, int height) {
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);
        return ensureFitsInBounds(new Dimension(width, height));
    }

    /**
     * Ensures the internal image fits within the provided bounds.
     *
     * @param dimension the dimension bounds the internal image should fit within
     * @return whether the image was resized if it did not fit in bounds
     * @throws NullPointerException     if the provided dimension is null
     * @throws IllegalArgumentException if the provided dimension has a length less than 0
     */
    @CanIgnoreReturnValue
    public boolean ensureFitsInBounds(Dimension dimension) {
        Preconditions.checkNotNull(dimension);
        Preconditions.checkArgument(dimension.getWidth() >= 0);
        Preconditions.checkArgument(dimension.getHeight() >= 0);

        int width = (int) dimension.getWidth();
        int height = (int) dimension.getHeight();

        boolean resized = false;
        if (isLandscape() && image.getWidth() > dimension.getWidth()) {
            float aspectRatio = image.getHeight() / (float) image.getWidth();
            height = (int) (dimension.getHeight() * aspectRatio);
            resizeImage(width, height);
            resized = true;
        } else if (isPortrait() && image.getHeight() > dimension.getHeight()) {
            float aspectRatio = image.getWidth() / (float) image.getHeight();
            width = (int) (dimension.getWidth() * aspectRatio);
            resizeImage(width, height);
            resized = true;
        } else if (isSquare() && image.getWidth() > dimension.getWidth()) {
            int len = (int) dimension.getWidth();
            resizeImage(len, len);
            resized = true;
        }

        return resized;
    }

    /** Converts this image converted to grayscale. */
    public void grayscaleImage() {
        BufferedImage bi = getBufferedImage();
        int width = bi.getWidth();
        int height = bi.getHeight();

        BufferedImage ret = new BufferedImage(width, height, bi.getType());

        for (int i = 0 ; i < width ; i++) {
            for (int j = 0 ; j < height ; j++) {
                int pixelData = bi.getRGB(i, j);

                int alpha = (pixelData >> ALPHA_SHIFT) & EIGHT_BIT_MASK;
                int red = (pixelData >> RED_SHIFT) & EIGHT_BIT_MASK;
                int green = (pixelData >> GREEN_SHIFT) & EIGHT_BIT_MASK;
                int blue = pixelData & EIGHT_BIT_MASK;
                int avg = (red + green + blue) / 3;

                pixelData = (alpha << ALPHA_SHIFT) | (avg << RED_SHIFT) | (avg << GREEN_SHIFT) | avg;

                ret.setRGB(i, j, pixelData);
            }
        }

        image = ret;
    }

    /**
     * Returns whether this image is grayscale.
     *
     * @return whether this image is grayscale
     */
    public boolean isGrayscale() {
        Image icon = getImageIcon().getImage();

        int w = icon.getWidth(null);
        int h = icon.getHeight(null);
        int[] pixels = new int[w * h];

        PixelGrabber pg = new PixelGrabber(icon, 0, 0, w, h, pixels, 0, w);

        try {
            pg.grabPixels();
        } catch (InterruptedException ignored) {}

        AtomicBoolean allGrayscale = new AtomicBoolean(true);

        Arrays.stream(pixels).forEach(pixel -> {
            Color color = new Color(pixel);
            int red = color.getRed();
            int green = color.getGreen();
            int blue = color.getBlue();

            if (red != green || red != blue) {
                allGrayscale.set(false);
            }
        });

        return allGrayscale.get();
    }

    /**
     * Returns the width of this image.
     *
     * @return the width of this image
     */
    public int getWidth() {
        return image.getWidth();
    }

    /**
     * Returns the height of this image.
     *
     * @return the height of this image
     */
    public int getHeight() {
        return image.getHeight();
    }

    /**
     * Returns whether the pixels of the provided image and this image are equal.
     *
     * @param compareImage the image to compare to this image
     * @param maxTolerance the maximum differing tolerance acceptable to return true
     * @return whether the pixels of the provided image and this image are equal
     * @throws NullPointerException     if the provided image is null
     * @throws IllegalArgumentException if the provided maxTolerance is not within the range [0, 100]
     */
    public boolean compareToPixelsIn(CyderImage compareImage, float maxTolerance) {
        Preconditions.checkNotNull(compareImage);
        Preconditions.checkArgument(percentageRange.contains(maxTolerance));

        if (compareImage.getWidth() != getWidth()) return false;
        if (compareImage.getHeight() != getHeight()) return false;

        int numDifferences = 0;
        for (int y = 0 ; y < getHeight() ; y++) {
            for (int x = 0 ; x < getWidth() ; x++) {
                int ourPixel = image.getRGB(x, y);
                int theirPixel = compareImage.image.getRGB(x, y);
                if (ourPixel != theirPixel) numDifferences++;
            }
        }

        int totalPixels = getWidth() * getHeight();
        float differencePercent = numDifferences / (float) totalPixels;
        logger.debug(() -> "Different percentage: " + differencePercent + "%");
        return differencePercent <= maxTolerance;
    }

    /**
     * Combines the provided ImageIcon into this image by placing
     * it relative to the existing one.
     * <p>
     * The two images must be of the same size in order to merge them into one image.
     *
     * @param placementImage the new image, image to be placed to the direction of the internal image
     * @param direction      the direction to place the newImage relative to the oldImage
     */
    public void combineImage(CyderImage placementImage, Direction direction) {
        Preconditions.checkNotNull(placementImage);
        Preconditions.checkNotNull(direction);
        Preconditions.checkArgument(getWidth() == placementImage.getWidth());
        Preconditions.checkArgument(getHeight() == placementImage.getHeight());

        try {
            int otherWidth = placementImage.getWidth();
            int otherHeight = placementImage.getHeight();
            BufferedImage other = placementImage.getBufferedImage();

            int width;
            int height;
            BufferedImage combined;
            Graphics2D g2;

            switch (direction) {
                case LEFT -> {
                    width = 2 * otherWidth;
                    height = otherHeight;
                    combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    g2 = combined.createGraphics();
                    g2.drawImage(image, null, width / 2, 0);
                    g2.drawImage(other, null, 0, 0);
                    g2.dispose();
                }
                case RIGHT -> {
                    width = 2 * otherWidth;
                    height = otherHeight;
                    combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    g2 = combined.createGraphics();
                    g2.drawImage(image, null, 0, 0);
                    g2.drawImage(other, null, width / 2, 0);
                    g2.dispose();
                }
                case TOP -> {
                    width = otherWidth;
                    height = 2 * otherHeight;
                    combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    g2 = combined.createGraphics();
                    g2.drawImage(image, null, 0, height / 2);
                    g2.drawImage(other, null, 0, 0);
                    g2.dispose();
                }
                case BOTTOM -> {
                    width = otherWidth;
                    height = 2 * otherHeight;
                    combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    g2 = combined.createGraphics();
                    g2.drawImage(image, null, 0, 0);
                    g2.drawImage(other, null, 0, height / 2);
                    g2.dispose();
                }
                default -> throw new IllegalArgumentException("Invalid direction: " + direction);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Blurs the internal image using Gaussian blur as the algorithm.
     *
     * @param radius the radius of the Gaussian blur, must be an odd number greater than 1
     * @throws IllegalArgumentException if the provided radius 1 or less or if the radius is not an odd number
     */
    public void blur(int radius) {
        Preconditions.checkArgument(radius > 2);
        Preconditions.checkArgument(radius % 2 != 0);

        int size = radius * radius;
        float[] data = new float[size];

        float sigma = radius / 3.0f;
        float twoSigmaSquare = 2.0f * sigma * sigma;
        float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
        float total = 0.0f;

        int index = 0;
        for (int x = -radius / 2 ; x <= radius / 2 ; x++) {
            for (int y = -radius / 2 ; y <= radius / 2 ; y++) {
                float distance = x * x + y * y;
                data[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
                total += data[index];
                index++;
            }
        }

        for (int i = 0 ; i < data.length ; i++) {
            data[i] /= total;
        }

        Kernel kernel = new Kernel(radius, radius, data);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        image = op.filter(image, null);
    }

    /**
     * Saves this image to the provided path.
     *
     * @param path the path to save the image to
     * @return whether the file was saved
     * @throws NullPointerException     if the provided path is null
     * @throws IllegalArgumentException if the provided path is empty or only whitespace
     */
    @CanIgnoreReturnValue
    public boolean saveTo(String path) {
        Preconditions.checkNotNull(path);
        Preconditions.checkArgument(!path.trim().isEmpty());

        return saveTo(new File(path));
    }

    /**
     * Saves this image to the provided file.
     *
     * @param file the file to save the image to
     * @return whether the file was saved
     * @throws NullPointerException     if the provided file is null
     * @throws IllegalArgumentException if the provided file does not exist or is a directory
     * @throws CyderImageException      if the parent directory of the provided file
     *                                  does not exist and fails to be created
     */
    @CanIgnoreReturnValue
    public boolean saveTo(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(!file.exists());
        Preconditions.checkArgument(!file.isDirectory());

        try {
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs())
                    throw new CyderImageException("Failed to create parent directory: " + parentDir);
            }

            String extension = FileUtil.getExtensionWithoutPeriod(file);
            ImageIO.write(getBufferedImage(), extension, file);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Returns whether the provided object equals this CyderImage instance.
     * Note this method also compares the direct pixels for an exact match.
     *
     * @param o the object to compare against this CyderImage instance
     * @return whether the provided object equals this CyderImage instance
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof CyderImage)) {
            return false;
        }

        CyderImage other = (CyderImage) o;
        return compareToPixelsIn(other, 0.0f)
                && other.getWidth() == getWidth()
                && other.getHeight() == getHeight()
                && other.colorCounterMaxLength == colorCounterMaxLength;
    }

    /**
     * Returns a hashcode representation of this CyderImage.
     *
     * @return a hashcode representation of this CyderImage
     */
    @Override
    public int hashCode() {
        int ret = Integer.hashCode(image.getWidth());
        ret = 31 * ret + Integer.hashCode(image.getHeight());
        ret = 31 * ret + Integer.hashCode(colorCounterMaxLength);
        return ret;
    }

    /**
     * Returns a string representation of this CyderImage.
     *
     * @return a string representation of this CyderImage
     */
    @Override
    public String toString() {
        return "CyderImage{"
                + "width=" + image.getWidth()
                + ", height=" + image.getHeight()
                + ", colorCounterMaxLength=" + colorCounterMaxLength
                + "}";
    }
}
