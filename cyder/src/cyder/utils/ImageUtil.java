package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import cyder.constants.CyderStrings;
import cyder.enums.Direction;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadFactory;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.PixelGrabber;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Static utility methods revolving around Image manipulation.
 */
public class ImageUtil {
    /**
     * Prevent class instantiation.
     */
    private ImageUtil() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * Pixelates the provided bufferedImage.
     *
     * @param imageToPixelate the image to pixelate
     * @param pixelSize       the number of old pixels to represent a single new "pixel"
     * @return a buffered image in the same size as the original with new, bigger pixel blocks
     */
    public static BufferedImage pixelate(BufferedImage imageToPixelate, int pixelSize) {
        BufferedImage pixelateImage = new BufferedImage(
                imageToPixelate.getWidth(),
                imageToPixelate.getHeight(),
                imageToPixelate.getType());

        for (int y = 0 ; y < imageToPixelate.getHeight() ; y += pixelSize) {
            for (int x = 0 ; x < imageToPixelate.getWidth() ; x += pixelSize) {
                BufferedImage croppedImage = getCroppedImage(imageToPixelate, x, y, pixelSize, pixelSize);
                Color dominantColor = ColorUtil.getDominantColor(croppedImage);

                for (int yd = y ; (yd < y + pixelSize) && (yd < pixelateImage.getHeight()) ; yd++) {
                    for (int xd = x ; (xd < x + pixelSize) && (xd < pixelateImage.getWidth()) ; xd++) {
                        pixelateImage.setRGB(xd, yd, dominantColor.getRGB());
                    }
                }

            }
        }

        return pixelateImage;
    }

    /**
     * Crops the specified bufferedImage to the new bounds and returns a new buffered image.
     *
     * @param image  the buffered image to crop
     * @param x      the starting x pixel within the image
     * @param y      the starting y pixel within the image
     * @param width  the width of the new image
     * @param height the height of the new image
     * @return the requested cropped image
     */
    public static BufferedImage getCroppedImage(BufferedImage image,
                                                int x, int y, int width, int height) {
        Preconditions.checkNotNull(image);
        Preconditions.checkArgument(x >= 0);
        Preconditions.checkArgument(y >= 0);
        Preconditions.checkArgument(width <= image.getWidth());
        Preconditions.checkArgument(height <= image.getHeight());

        if (x + width > image.getWidth()) {
            x = 0;
            width = image.getWidth();
        }

        if (y + height > image.getHeight()) {
            y = 0;
            height = image.getHeight();
        }

        return image.getSubimage(x, y, width, height);
    }

    /**
     * Returns a buffered image of the specified color.
     *
     * @param c      the color of the requested image
     * @param width  the width of the requested image
     * @param height the height of the requested image
     * @return the buffered image of the provided color and dimensions
     */
    public static BufferedImage bufferedImageFromColor(Color c, int width, int height) {
        Preconditions.checkNotNull(c);
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);

        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bi.createGraphics();

        graphics.setPaint(c);
        graphics.fillRect(0, 0, width, height);

        return bi;
    }

    /**
     * Returns an ImageIcon of the requested color.
     *
     * @param c      the color of the requested image
     * @param width  the width of the requested image
     * @param height the height of the requested image
     * @return the image of the requested color and dimensions
     */
    public static ImageIcon imageIconFromColor(Color c, int width, int height) {
        Preconditions.checkNotNull(c);
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);

        BufferedImage im = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = im.createGraphics();
        g.setPaint(c);
        g.fillRect(0, 0, width, height);
        return new ImageIcon(im);
    }

    /**
     * Resizes the provided ImageIcon to the requested dimensions.
     *
     * @param width  the width of the requested image
     * @param height the height of the requested image
     * @param icon   the ImageIcon to resize
     * @return the resized image
     */
    public static BufferedImage resizeImage(int width, int height, ImageIcon icon) {
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);
        Preconditions.checkNotNull(icon);

        BufferedImage ReturnImage = null;

        try {
            Image ConsoleImage = icon.getImage();
            Image TransferImage = ConsoleImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            ReturnImage = new BufferedImage(TransferImage.getWidth(null),
                    TransferImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D bGr = ReturnImage.createGraphics();

            bGr.drawImage(TransferImage, 0, 0, null);
            bGr.dispose();
            return ReturnImage;
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ReturnImage;
    }

    /**
     * Returns the image at the provided location resized.
     *
     * @param width     the width to resize to
     * @param height    the height to resize to
     * @param imageFile the File representing an image
     * @return the resized image
     */
    public static BufferedImage resizeImage(int width, int height, File imageFile) {
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);
        Preconditions.checkNotNull(imageFile);

        BufferedImage ReturnImage = null;

        try {
            Image ConsoleImage = ImageIO.read(imageFile);
            Image TransferImage = ConsoleImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            ReturnImage = new BufferedImage(TransferImage.getWidth(null),
                    TransferImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D bGr = ReturnImage.createGraphics();

            bGr.drawImage(TransferImage, 0, 0, null);
            bGr.dispose();
            return ReturnImage;
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ReturnImage;
    }

    /**
     * Returns a buffered image from the provided file.
     *
     * @param imageFile the file to convert to a buffered image
     * @return the buffered image
     */
    @Nullable
    public static BufferedImage getBi(File imageFile) {
        Preconditions.checkNotNull(imageFile);
        Preconditions.checkArgument(imageFile.exists());

        try {
            return ImageIO.read(imageFile);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return null;
    }

    /**
     * Returns a buffered image from the provided image icon.
     *
     * @param im the image icon to convert to a buffered image.
     * @return the buffered image drawn from the provided image icon
     */
    public static BufferedImage getBi(ImageIcon im) {
        Preconditions.checkNotNull(im);

        BufferedImage bi = new BufferedImage(im.getIconWidth(), im.getIconHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.createGraphics();
        im.paintIcon(null, g, 0, 0);
        return bi;
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
     * Returns a buffered image by attempting to read the provided path.
     *
     * @param filename the path to read
     * @return the buffered image
     */
    public static BufferedImage getBi(String filename) {
        Preconditions.checkNotNull(filename);
        Preconditions.checkArgument(!filename.isEmpty());

        return getBi(new File(filename));
    }

    /**
     * Returns the rotated background file.
     *
     * @param filepath  the path to the file
     * @param direction the direction of rotation
     * @return the rotated image
     */
    @SuppressWarnings("UnnecessaryDefault")
    public static BufferedImage getRotatedImage(String filepath, Direction direction) {
        Preconditions.checkNotNull(filepath);
        Preconditions.checkArgument(!filepath.isEmpty());
        Preconditions.checkNotNull(direction);

        return switch (direction) {
            case TOP -> getBi(filepath);
            case RIGHT -> rotateImageByDegrees(getBi(filepath), 90);
            case BOTTOM -> rotateImageByDegrees(getBi(filepath), 180);
            case LEFT -> rotateImageByDegrees(getBi(filepath), -90);
            default -> throw new IllegalArgumentException("Invalid direction: " + direction);
        };
    }

    /**
     * Rotates the provided buffered image by the requested angle in degrees.
     *
     * @param img     the buffered image to rotate
     * @param degrees the angle to rotate by in degrees
     * @return the rotated image
     */
    public static BufferedImage rotateImageByDegrees(BufferedImage img, double degrees) {
        Preconditions.checkNotNull(img);

        degrees = MathUtil.convertAngleToStdForm(degrees);

        double rads = Math.toRadians(degrees);

        double sin = Math.abs(Math.sin(rads));
        double cos = Math.abs(Math.cos(rads));

        int w = img.getWidth();
        int h = img.getHeight();

        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2.0, (newHeight - h) / 2.0);

        at.rotate(rads, w / 2.0, h / 2.0);
        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return rotated;
    }

    /**
     * Rotates the provided ImageIcon by the requested angle in degrees
     *
     * @param imageIcon the image icon to rotate
     * @param degrees   the angle to rotate by in degrees
     * @return the rotated image
     */
    public static ImageIcon rotateImageByDegrees(ImageIcon imageIcon, double degrees) {
        BufferedImage img = getBi(imageIcon);

        degrees = MathUtil.convertAngleToStdForm(degrees);

        double rads = Math.toRadians(degrees);

        double sin = Math.abs(Math.sin(rads));
        double cos = Math.abs(Math.cos(rads));

        int w = img.getWidth();
        int h = img.getHeight();

        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2.0, (newHeight - h) / 2.0);

        at.rotate(rads, w / 2.0, h / 2.0);
        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return new ImageIcon(rotated);
    }

    /**
     * Draws the provided buffered image to a CyderFrame and displays it.
     *
     * @param bi the buffered image to display
     */
    public static void drawImage(BufferedImage bi) {
        Preconditions.checkNotNull(bi);

        drawImage(new ImageIcon(bi), "BufferedImage");
    }

    /**
     * Draws the provided buffered image to a CyderFrame and displays it.
     *
     * @param bi         the buffered image to display
     * @param frameTitle the title of the frame
     */
    public static void drawImage(BufferedImage bi, String frameTitle) {
        Preconditions.checkNotNull(bi);
        Preconditions.checkNotNull(frameTitle);
        Preconditions.checkArgument(!frameTitle.isEmpty());

        drawImage(new ImageIcon(bi), frameTitle);
    }

    /**
     * Draws the provided image icon to a CyderFrame and displays it.
     *
     * @param icon the icon to display
     */
    public static void drawImage(ImageIcon icon) {
        Preconditions.checkNotNull(icon);

        drawImage(icon, "ImageIcon");
    }

    /**
     * Draws the provided image icon to a CyderFrame and displays it.
     *
     * @param icon       the icon to display
     * @param frameTitle the title of the frame
     */
    public static void drawImage(ImageIcon icon, String frameTitle) {
        Preconditions.checkNotNull(icon);
        Preconditions.checkNotNull(frameTitle);
        Preconditions.checkArgument(!frameTitle.isEmpty());

        CyderFrame frame = new CyderFrame(icon.getIconWidth(), icon.getIconHeight());
        frame.setTitle("[" + icon.getIconWidth() + "x" + icon.getIconHeight() + "] " + frameTitle);

        JLabel label = new JLabel(icon);
        label.setBounds(0, 0, icon.getIconWidth(), icon.getIconHeight());
        frame.getContentPane().add(label);

        frame.finalizeAndShow();
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
     * dimensions using bilinear interpolation.
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
            ExceptionHandler.handle(e);
        }

        return ret;
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
     */
    public static boolean isImageGrayScale(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());

        try {
            Image icon = new ImageIcon(ImageIO.read(file)).getImage();
            int w = icon.getWidth(null);
            int h = icon.getHeight(null);
            int[] pixels = new int[w * h];
            PixelGrabber pg = new PixelGrabber(icon, 0, 0, w, h, pixels, 0, w);
            pg.grabPixels();
            boolean allBlack = true;
            for (int pixel : pixels) {
                Color color = new Color(pixel);
                if (color.getRed() != color.getGreen() || color.getRed() != color.getBlue()) {
                    allBlack = false;
                    break;
                }
            }

            return allBlack;
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return false;
    }

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
     * Returns whether the image represented by the provided path is a solid color.
     *
     * @param file the path to the file
     * @return whether the image represented by the provided path is a solid color
     */
    public static boolean solidColor(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());

        boolean ret = true;

        try {
            Image icon = new ImageIcon(ImageIO.read(file)).getImage();
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
            ExceptionHandler.handle(e);
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
    public static boolean imageIconsEqual(ImageIcon first, ImageIcon second) {
        return areImagesEqual(first.getImage(), second.getImage());
    }

    /**
     * Returns whether the two images represent the same pixel data.
     *
     * @param firstImage  the first image
     * @param secondImage the second image
     * @return whether the two images represent the same pixel data
     */
    @SuppressWarnings("ConstantConditions") // throw nullptr handled
    public static boolean areImagesEqual(Image firstImage, Image secondImage) {
        if (firstImage == null && secondImage == null) {
            return true;
        }

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
            ExceptionHandler.handle(e);
            ret = false;
        }

        return ret;
    }

    /**
     * Finds the optimal size provided the min/max width/height bounds. The return dimension is ensured
     * to be within the provided bounds
     *
     * @return an array representing the new image dimensions that the provided image should be cropped to
     * so that the provided min/max properties are maintained
     */
    @SuppressWarnings("ConstantConditions") // aspect ratio of 1.0 needs to not change delta values
    public static Dimension getImageResizeDimensions(int minWidth, int minHeight,
                                                     int maxWidth, int maxHeight, BufferedImage image) {
        Preconditions.checkArgument(minWidth > 0);
        Preconditions.checkArgument(minHeight > 0);
        Preconditions.checkArgument(maxWidth > 0);
        Preconditions.checkArgument(maxHeight > 0);
        Preconditions.checkArgument(maxWidth > minWidth);
        Preconditions.checkArgument(maxHeight > minHeight);

        Preconditions.checkNotNull(image);

        int backgroundWidth = image.getWidth();
        int backgroundHeight = image.getHeight();

        //inform the user we are changing the size of the image
        boolean resizeNeeded = backgroundWidth > maxWidth || backgroundHeight > maxHeight ||
                backgroundWidth < minWidth || backgroundHeight < minHeight;

        double widthToHeightRatio = ((double) backgroundWidth / (double) backgroundHeight);
        double heightToWidthRatio = ((double) backgroundHeight / (double) backgroundWidth);
        double deltaWidth = 0.0;
        double deltaHeight = 0.0;

        if (widthToHeightRatio < 1.0) {
            if (resizeNeeded) {
                if (backgroundWidth > maxWidth || backgroundHeight > maxHeight) {
                    deltaHeight = maxHeight;
                    deltaWidth = maxHeight * (1.0 / heightToWidthRatio);
                } else if (backgroundWidth < minWidth || backgroundHeight < minHeight) {
                    deltaWidth = minWidth;
                    deltaHeight = minWidth * heightToWidthRatio;
                }
            }
        } else {
            if (resizeNeeded) {
                if (backgroundWidth > maxWidth || backgroundHeight > maxHeight) {
                    deltaWidth = maxWidth;
                    deltaHeight = maxWidth * (1.0 / widthToHeightRatio);
                } else if (backgroundWidth < minWidth || backgroundHeight < minHeight) {
                    deltaHeight = minHeight;
                    deltaWidth = minHeight * widthToHeightRatio;
                }
            }
        }

        //after all this, if something's too big, crop as much as possible
        if (deltaWidth > maxWidth) {
            deltaWidth = maxWidth;
            deltaHeight = (int) Math.min(backgroundHeight, maxWidth * (1.0 / widthToHeightRatio));
        } else if (deltaHeight > maxHeight) {
            deltaHeight = maxHeight;
            deltaWidth = (int) Math.min(backgroundWidth, maxHeight * (1.0 / heightToWidthRatio));
        }

        return new Dimension((int) deltaWidth, (int) deltaHeight);
    }

    /**
     * Returns a buffered image object for the provided component.
     *
     * @param component the component to take a picture of
     * @return the buffered image representing the provided component
     */
    public static BufferedImage getScreenShot(Component component) {
        Preconditions.checkNotNull(component);

        BufferedImage image = new BufferedImage(
                component.getWidth(),
                component.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        component.paint(image.getGraphics());

        return image;
    }

    /**
     * Returns whether the two images from the provided file are equal.
     *
     * @param file1 the first file
     * @param file2 the second file
     * @return whether the two images from the provided file are equal
     */
    public static boolean compareImage(File file1, File file2) {
        Preconditions.checkNotNull(file1);
        Preconditions.checkNotNull(file2);
        Preconditions.checkArgument(file1.exists());
        Preconditions.checkArgument(file2.exists());

        try {
            BufferedImage bi1 = ImageIO.read(file1);
            DataBuffer db1 = bi1.getData().getDataBuffer();
            int size = db1.getSize();

            BufferedImage bi2 = ImageIO.read(file2);
            DataBuffer db2 = bi2.getData().getDataBuffer();
            int size2 = db2.getSize();

            if (size == size2) {
                for (int i = 0 ; i < size ; i++) {
                    if (db1.getElem(i) != db2.getElem(i)) {
                        return false;
                    }
                }

                return true;
            } else
                return false;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Returns whether the provided file is a valid image file.
     *
     * @param file the file to check for image validity
     * @return whether the provided file is a valid image file
     */
    public static boolean isValidImage(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());

        if (!file.exists())
            return false;

        boolean ret = true;

        try {
            ImageIO.read(file);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            ret = false;
        }

        return ret;
    }

    /**
     * Returns a good background color for the provided image file.
     *
     * @param imagePath the path to the image file
     * @return a good background color for the provided image file
     */
    public static Future<Optional<Color>> getComplementaryBackgroundColor(String imagePath) {
        Preconditions.checkNotNull(imagePath);
        Preconditions.checkArgument(!imagePath.isEmpty());
        Preconditions.checkArgument(new File(imagePath).exists());

        if (!OSUtil.isBinaryInstalled("python")) {
            ConsoleFrame.INSTANCE.getInputHandler()
                    .println("Python was not found; please install Python and add it" +
                            " to the windows PATH environment variable");

            CyderButton installPython = new CyderButton("Download Python");
            installPython.addActionListener(e -> NetworkUtil.openUrl("https://www.python.org/downloads/"));
            ConsoleFrame.INSTANCE.getInputHandler().println(installPython);

            return Futures.immediateFuture(Optional.empty());
        }

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory("Python Script Executor")).submit(() -> {
            try {
                String[] commands = {"python",
                        OSUtil.buildFile("static", "python", "k_means_color.py").getAbsolutePath(),
                        "--image", imagePath};
                Process proc = Runtime.getRuntime().exec(commands);

                BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String line;
                String lastLine = "";

                proc.waitFor();

                while ((line = stdInput.readLine()) != null) {
                    lastLine = line;
                }

                if (lastLine.contains(",")) {
                    String[] parts = lastLine.split(",");

                    if (parts.length == 3) {
                        Color color = new Color(
                                Integer.parseInt(parts[0]),
                                Integer.parseInt(parts[1]),
                                Integer.parseInt(parts[2]));

                        return Optional.of(color);
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }

            return Optional.empty();
        });
    }

    /**
     * Sets the alpha value of all pixels within the buffered image to the provided value.
     *
     * @param bi    the buffered image to alter
     * @param alpha the alpha value to set all the pixels to
     * @return the altered buffered image
     */
    public static BufferedImage setAlphaOfPixels(BufferedImage bi, int alpha) {
        Preconditions.checkNotNull(bi);
        Preconditions.checkArgument(alpha >= 0);
        Preconditions.checkArgument(alpha < 256);

        BufferedImage ret = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int x = 0 ; x < bi.getWidth() ; x++) {
            for (int y = 0 ; y < bi.getHeight() ; y++) {
                int rgb = bi.getRGB(x, y);
                int mc = (alpha << 24) | 0x00ffffff;

                ret.setRGB(x, y, (byte) (rgb & mc));
            }
        }

        return ret;
    }
}