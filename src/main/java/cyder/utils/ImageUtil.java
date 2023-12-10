package cyder.utils;

import com.google.common.base.Preconditions;
import cyder.enumerations.Direction;
import cyder.image.CyderImage;
import cyder.network.NetworkUtil;
import cyder.process.Program;
import cyder.threads.CyderThreadFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Static utility methods revolving around Image manipulation.
 */
@SuppressWarnings("unused") /* jpg formats */
public final class ImageUtil {
    /**
     * The name of the thread which blurs an image.
     */
    private static final String GAUSSIAN_IMAGE_BLURER_THREAD_NAME = "Gaussian Image Blurer Thread";

    /**
     * The title of the draw buffered image frame.
     */
    private static final String defaultDrawBufferedImageTitle = "BufferedImage";

    /**
     * The title of the draw image icon frame.
     */
    private static final String defaultDrawImageIconTitle = "ImageIcon";

    /**
     * Returns an ImageIcon of the requested color.
     *
     * @param color  the color of the requested image
     * @param width  the width of the requested image
     * @param height the height of the requested image
     * @return the image of the requested color and dimensions
     */
    public static ImageIcon imageIconFromColor(Color color, int width, int height) {
        Preconditions.checkNotNull(color);
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);

        BufferedImage im = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = im.createGraphics();
        g.setPaint(color);
        g.fillRect(0, 0, width, height);
        return new ImageIcon(im);
    }

    /**
     * Returns the buffered image converted to an ImageIcon.
     *
     * @param image a buffered image to convert
     * @return the image icon after conversion
     */
    public static ImageIcon toImageIcon(BufferedImage image) {
        Preconditions.checkNotNull(image);

        return new ImageIcon(image);
    }

    /**
     * Converts the provided ImageIcon to a BufferedImage.
     *
     * @param icon the image icon to convert
     * @return the buffered image after converting
     */
    public static BufferedImage toBufferedImage(ImageIcon icon) {
        Preconditions.checkNotNull(icon);

        BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.createGraphics();
        icon.paintIcon(null, g, 0, 0);
        g.dispose();
        return bi;
    }

    /**
     * Resizes the provided buffered image.
     *
     * @param image  the original buffered image to resize
     * @param type   the image type
     * @param width  the width of the new image
     * @param height the height of the new image
     * @return the resized buffered image
     */
    public static BufferedImage resizeImage(BufferedImage image,
                                            int type, int width, int height) {
        Preconditions.checkNotNull(image);
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);

        BufferedImage resizedImage = new BufferedImage(width, height, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();

        return resizedImage;
    }

    /**
     * Resizes the provided ImageIcon to have the requested
     * dimensions using bi-linear interpolation.
     *
     * @param image the image to resize
     * @param w     the width of the new image
     * @param h     the height of the new image
     * @return the resized image
     */
    public static ImageIcon resizeImage(ImageIcon image, int w, int h) {
        Preconditions.checkNotNull(image);
        Preconditions.checkArgument(w > 0);
        Preconditions.checkArgument(h > 0);

        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(image.getImage(), 0, 0, w, h, null);
        g2.dispose();

        return new ImageIcon(resizedImg);
    }

    /**
     * Combines the provided ImageIcons into one image by placing
     * one relative to the other and taking into account
     * the possible rotation direction provided.
     * <p>
     * The two images must be of the same size in order to merge them into one image.
     *
     * @param newImage  the new image (image to be placed to the direction of the old image)
     * @param oldImage  the old image (image to be placed center)
     * @param direction the direction to place the newImage relative to the oldImage
     * @return the combined image
     */
    public static ImageIcon combineImages(ImageIcon oldImage, ImageIcon newImage, Direction direction) {
        Preconditions.checkNotNull(oldImage);
        Preconditions.checkNotNull(newImage);
        Preconditions.checkNotNull(direction);
        Preconditions.checkArgument(oldImage.getIconWidth() == newImage.getIconWidth());
        Preconditions.checkArgument(oldImage.getIconHeight() == newImage.getIconHeight());

        ImageIcon ret = null;

        try {
            BufferedImage bi1 = toBufferedImage(oldImage);
            BufferedImage bi2 = toBufferedImage(newImage);

            int width;
            int height;
            BufferedImage combined;
            Graphics2D g2;

            switch (direction) {
                case LEFT -> {
                    width = 2 * newImage.getIconWidth();
                    height = newImage.getIconHeight();
                    combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    g2 = combined.createGraphics();
                    g2.drawImage(bi1, null, width / 2, 0);
                    g2.drawImage(bi2, null, 0, 0);
                    g2.dispose();
                }
                case RIGHT -> {
                    width = 2 * newImage.getIconWidth();
                    height = newImage.getIconHeight();
                    combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    g2 = combined.createGraphics();
                    g2.drawImage(bi1, null, 0, 0);
                    g2.drawImage(bi2, null, width / 2, 0);
                    g2.dispose();
                }
                case TOP -> {
                    width = newImage.getIconWidth();
                    height = 2 * newImage.getIconHeight();
                    combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    g2 = combined.createGraphics();
                    g2.drawImage(bi1, null, 0, height / 2);
                    g2.drawImage(bi2, null, 0, 0);
                    g2.dispose();
                }
                case BOTTOM -> {
                    width = newImage.getIconWidth();
                    height = 2 * newImage.getIconHeight();
                    combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    g2 = combined.createGraphics();
                    g2.drawImage(bi1, null, 0, 0);
                    g2.drawImage(bi2, null, 0, height / 2);
                    g2.dispose();
                }
                default -> throw new IllegalArgumentException("Invalid direction: " + direction);
            }

            ret = new ImageIcon(combined);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

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
     * Returns whether the image at the provided path is a gray scale image.
     * This is determined if the for all pixels, the red, green, and blue bits are equal.
     *
     * @param file the path to the image file
     * @return whether the image is gray scale
     * @throws IOException if the provided file could not be read
     */
    public static boolean isGrayscale(File file) throws IOException {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());

        return isGrayscale(read(file));
    }

    /**
     * Returns whether the provided image is a gray scale image.
     * This is determined if the for all pixels, the red, green, and blue bits are equal.
     *
     * @param bi the image to determine grayscale from
     * @return whether the image is gray scale
     */
    public static boolean isGrayscale(BufferedImage bi) {
        Image icon = new ImageIcon(bi).getImage();

        int w = icon.getWidth(null);
        int h = icon.getHeight(null);
        int[] pixels = new int[w * h];

        PixelGrabber pg = new PixelGrabber(icon, 0, 0, w, h, pixels, 0, w);

        try {
            pg.grabPixels();
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean allGrayscale = true;

        for (int pixel : pixels) {
            Color color = new Color(pixel);
            if (color.getRed() != color.getGreen() || color.getRed() != color.getBlue()) {
                allGrayscale = false;
                break;
            }
        }

        return allGrayscale;
    }

    /**
     * Returns the provided image converted to grayscale.
     *
     * @param bi the image to convert to grayscale
     * @return the image converted to grayscale
     */
    public static BufferedImage grayscaleImage(BufferedImage bi) {
        Preconditions.checkNotNull(bi);

        int width = bi.getWidth();
        int height = bi.getHeight();

        BufferedImage ret = new BufferedImage(width, height, bi.getType());

        for (int i = 0 ; i < width ; i++) {
            for (int j = 0 ; j < height ; j++) {
                int p = bi.getRGB(i, j);

                int a = (p >> 24) & 0xff;

                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = p & 0xff;

                int avg = (r + g + b) / 3;

                p = (a << 24) | (avg << 16) | (avg << 8) | avg;

                ret.setRGB(i, j, p);
            }
        }

        return ret;
    }

    /**
     * Returns whether the image represented by the provided file is a solid color.
     *
     * @param file the path to the file
     * @return whether the image is a solid color
     * @throws IOException if the provided file could nto be read from
     */
    public static boolean isSolidColor(File file) throws IOException {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());

        return isSolidColor(read(file));
    }

    /**
     * Returns whether the image is a solid color.
     *
     * @param bi the buffered image
     * @return whether the image is a solid color
     */
    public static boolean isSolidColor(BufferedImage bi) {
        Preconditions.checkNotNull(bi);

        boolean ret = true;

        try {
            Image icon = new ImageIcon(bi).getImage();
            int w = icon.getWidth(null);
            int h = icon.getHeight(null);
            int[] pixels = new int[w * h];
            PixelGrabber pg = new PixelGrabber(icon, 0, 0, w, h, pixels, 0, w);
            pg.grabPixels();
            Color firstColor = new Color(pixels[0]);
            for (int i = 1 ; i < pixels.length ; i++) {
                if (!new Color(pixels[i]).equals(firstColor)) {
                    ret = false;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * Returns whether the provided ImageIcons are equal.
     *
     * @param first  the first image icon
     * @param second the second image icon
     * @return whether the provided ImageIcons are equal
     */
    public static boolean areImagesEqual(ImageIcon first, ImageIcon second) {
        Preconditions.checkNotNull(first);
        Preconditions.checkNotNull(second);

        return areImagesEqual(first.getImage(), second.getImage());
    }

    /**
     * Returns whether the two images represent the same pixel data.
     *
     * @param firstImage  the first image
     * @param secondImage the second image
     * @return whether the two images represent the same pixel data
     */
    public static boolean areImagesEqual(Image firstImage, Image secondImage) {
        Preconditions.checkNotNull(firstImage);
        Preconditions.checkNotNull(secondImage);

        boolean ret = true;

        try {
            int w1 = firstImage.getWidth(null);
            int h1 = firstImage.getHeight(null);

            int w2 = secondImage.getWidth(null);
            int h2 = secondImage.getHeight(null);

            if (w1 != w2 || h1 != h2) {
                ret = false;
            } else {
                int[] pixels1 = new int[w1 * h1];
                int[] pixels2 = new int[w2 * h2];

                PixelGrabber pg1 = new PixelGrabber(firstImage, 0, 0, w1, h2, pixels1, 0, w1);
                pg1.grabPixels();

                PixelGrabber pg2 = new PixelGrabber(secondImage, 0, 0, w2, h2, pixels2, 0, w2);
                pg2.grabPixels();

                if (pixels1.length != pixels2.length) {
                    ret = false;
                } else {
                    for (int i = 1 ; i < pixels1.length ; i++) {
                        if (pixels1[i] != pixels2[i]) {
                            ret = false;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ret = false;
        }

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

    /**
     * Returns the buffered image read from the provided url.
     *
     * @param url the url to read a buffered image from
     * @return the buffered image read from the provided url
     * @throws IOException if the provided resource cannot be loaded
     */
    public static BufferedImage read(String url) throws IOException {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());
        Preconditions.checkArgument(NetworkUtil.isValidUrl(url));

        return ImageIO.read(new URL(url));
    }

    /**
     * Returns the buffered image read from the provided file.
     *
     * @param file the file
     * @return the buffered image read from the file
     * @throws IOException if the image cannot be read
     */
    public static BufferedImage read(File file) throws IOException {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());

        return ImageIO.read(file);
    }
}
