package com.cyder.utilities;

import com.cyder.constants.CyderColors;
import com.cyder.constants.CyderFonts;
import com.cyder.enums.ConsoleDirection;
import com.cyder.enums.Direction;
import com.cyder.exception.FatalException;
import com.cyder.handler.ErrorHandler;
import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

public class ImageUtil {

    private ImageUtil() {} //private constructor to avoid object creation

    private static CyderFrame pixelFrame;

    public static BufferedImage pixelate(BufferedImage imageToPixelate, int pixelSize) {
        BufferedImage pixelateImage = new BufferedImage(
                imageToPixelate.getWidth(),
                imageToPixelate.getHeight(),
                imageToPixelate.getType());

        for (int y = 0; y < imageToPixelate.getHeight(); y += pixelSize) {
            for (int x = 0; x < imageToPixelate.getWidth(); x += pixelSize) {
                BufferedImage croppedImage = getCroppedImage(imageToPixelate, x, y, pixelSize, pixelSize);
                Color dominantColor = getDominantColor(croppedImage);

                for (int yd = y; (yd < y + pixelSize) && (yd < pixelateImage.getHeight()); yd++)
                    for (int xd = x; (xd < x + pixelSize) && (xd < pixelateImage.getWidth()); xd++)
                        pixelateImage.setRGB(xd, yd, dominantColor.getRGB());

            }
        }

        return pixelateImage;
    }

    public static BufferedImage getCroppedImage(BufferedImage image, int startx, int starty, int width, int height) {
        if (startx < 0)
            startx = 0;

        if (starty < 0)
            starty = 0;

        if (startx > image.getWidth())
            startx = image.getWidth();

        if (starty > image.getHeight())
            starty = image.getHeight();

        if (startx + width > image.getWidth())
            width = image.getWidth() - startx;

        if (starty + height > image.getHeight())
            height = image.getHeight() - starty;

        return image.getSubimage(startx, starty, width, height);
    }

    public static Color getDominantColor(BufferedImage image) {
        Map<Integer, Integer> colorCounter = new HashMap<>(100);

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int currentRGB = image.getRGB(x, y);
                int count = colorCounter.getOrDefault(currentRGB, 0);
                colorCounter.put(currentRGB, count + 1);
            }
        }

        return getDominantColor(colorCounter);
    }

    public static Color getDominantColorOpposite(BufferedImage image) {
        Color c = getDominantColor(image);
        return new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue(), c.getAlpha());
    }

    public static Color getDominantColor(Map<Integer, Integer> colorCounter) {
        int dominantRGB = colorCounter.entrySet().stream()
                .max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1)
                .get()
                .getKey();

        return new Color(dominantRGB);
    }

    public static void pixelate(File path, int pixelSize) {
        try {
            BufferedImage retImage = ImageUtil.pixelate(ImageIO.read(path), pixelSize);
            String NewName = path.getName().replace(".png", "") + "_Pixelated_Pixel_Size_" + pixelSize + ".png";

            if (pixelFrame != null)
                pixelFrame.closeAnimation();

            pixelFrame = new CyderFrame(retImage.getWidth(),retImage.getHeight(), new ImageIcon(retImage));
            pixelFrame.setTitle("Approve Pixelation");

            CyderButton approveImage = new CyderButton("Approve Image");
            approveImage.setFocusPainted(false);
            approveImage.setBackground(CyderColors.regularRed);
            approveImage.setColors(CyderColors.regularRed);
            approveImage.setBorder(new LineBorder(CyderColors.navy,3,false));
            approveImage.setFont(CyderFonts.weatherFontSmall);

            approveImage.addActionListener(e -> {
                try {
                    ImageIO.write(retImage, "png", new File("C:\\Users\\" + SystemUtil.getWindowsUsername() + "\\Downloads\\" + NewName));
                } catch (Exception exc) {
                    ErrorHandler.handle(exc);
                }

                pixelFrame.closeAnimation();
                pixelFrame.inform("The pixelated image has been saved to your Downloads folder.","Saved");
            });
            approveImage.setBounds(20, retImage.getHeight() - 100,retImage.getWidth() - 40, 40);
            pixelFrame.getContentPane().add(approveImage);

            CyderButton rejectImage = new CyderButton("Reject Image");
            rejectImage.setFocusPainted(false);
            rejectImage.setBackground(CyderColors.regularRed);
            rejectImage.setBorder(new LineBorder(CyderColors.navy,3,false));
            rejectImage.setColors(CyderColors.regularRed);
            rejectImage.setFont(CyderFonts.weatherFontSmall);
            rejectImage.addActionListener(e -> pixelFrame.closeAnimation());
            rejectImage.setSize(pixelFrame.getX(), 20);
            rejectImage.setBounds(20, retImage.getHeight() - 60,retImage.getWidth() - 40, 40);
            pixelFrame.getContentPane().add(rejectImage);

            pixelFrame.setVisible(true);
            pixelFrame.setLocationRelativeTo(null);
            pixelFrame.setAlwaysOnTop(true);
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public static BufferedImage imageFromColor(int x, int y, Color c) {
        BufferedImage bi = new BufferedImage(x,y,BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bi.createGraphics();

        graphics.setPaint(c);
        graphics.fillRect ( 0, 0, x, y);

        return bi;
    }

    public static BufferedImage resizeImage(int x, int y, File UneditedImage) {
        BufferedImage ReturnImage = null;

        try {
            File CurrentConsole = UneditedImage;
            Image ConsoleImage = ImageIO.read(CurrentConsole);
            Image TransferImage = ConsoleImage.getScaledInstance(x, y, Image.SCALE_SMOOTH);
            ReturnImage = new BufferedImage(TransferImage.getWidth(null), TransferImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D bGr = ReturnImage.createGraphics();

            bGr.drawImage(TransferImage, 0, 0, null);
            bGr.dispose();
            return ReturnImage;
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return ReturnImage;
    }

    public BufferedImage getBi(File imageFile) {
        try {
            return ImageIO.read(imageFile);
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return null;
    }

    public static BufferedImage getBi(ImageIcon im) {
        BufferedImage bi = new BufferedImage(im.getIconWidth(), im.getIconHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.createGraphics();
        im.paintIcon(null, g, 0,0);
        return bi;
    }

    public static BufferedImage getBi(String filename) {
        try {
            return ImageIO.read(new File(filename));
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return null;
    }

    public static BufferedImage getRotatedImage(String name, ConsoleDirection consoleDirection) {
        switch(consoleDirection) {
            case UP:
                return ImageUtil.getBi(name);
            case RIGHT:
                return ImageUtil.rotateImageByDegrees(ImageUtil.getBi(name),90);
            case DOWN:
                return ImageUtil.rotateImageByDegrees(ImageUtil.getBi(name),180);
            case LEFT:
                return ImageUtil.rotateImageByDegrees(ImageUtil.getBi(name),-90);
        }

        return null;
    }

    //Used for barrel roll and flip screen hotkeys, credit: MadProgrammer from StackOverflow
    public static BufferedImage rotateImageByDegrees(BufferedImage img, double angle) {
        double rads = Math.toRadians(angle);

        double sin = Math.abs(Math.sin(rads));
        double cos = Math.abs(Math.cos(rads));

        int w = img.getWidth();
        int h = img.getHeight();

        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2, (newHeight - h) / 2);

        at.rotate(rads, w / 2, h / 2);
        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return rotated;
    }

    public static int xOffsetForCenterJLabel(int compWidth, String title) {
        return (int) Math.floor(5 + (compWidth / 2.0)) - (((int) Math.ceil(14 * title.length())) / 2);
    }

    public static BufferedImage resizeImage(BufferedImage originalImage, int type, int img_width, int img_height) {
        BufferedImage resizedImage = new BufferedImage(img_width, img_height, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, img_width, img_height, null);
        g.dispose();

        return resizedImage;
    }

    public static double getAspectRatio(BufferedImage im) {
        return ((double) im.getWidth() / (double) im.getHeight());
    }

    public int getScreenResolution() {
        return Toolkit.getDefaultToolkit().getScreenResolution();
    }

    public static ImageIcon resizeImage(ImageIcon srcImg, int w, int h){
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg.getImage(), 0, 0, w, h, null);
        g2.dispose();

        return new ImageIcon(resizedImg);
    }

    /** the two images must be of the same size in order to merge them into one image
     *
     * @param newImage - the new image (image to be placed to the dir[ection] of the old image)
     * @param oldImage - the old image (image to be placed center)
     * @param dir - the direction to place the newImage relative to the oldImage
     * @return - the combined image
     */
    public static ImageIcon combineImages(ImageIcon oldImage, ImageIcon newImage, Direction dir) {
        ImageIcon ret = null;

        if (oldImage.getIconWidth() != newImage.getIconWidth() || oldImage.getIconHeight() != newImage.getIconHeight())
            return ret;

        try {
            BufferedImage bi1 = ImageIcon2BufferedImage(oldImage);
            BufferedImage bi2 = ImageIcon2BufferedImage(newImage);

            int width = 0;
            int height = 0;
            BufferedImage combined = null;
            Graphics2D g2 = null;

            switch (dir) {
                case LEFT:
                    width = 2 * newImage.getIconWidth();
                    height = newImage.getIconHeight();

                    combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    g2 = combined.createGraphics();

                    g2.drawImage(bi1, null, width / 2, 0);
                    g2.drawImage(bi2, null, 0, 0);
                    g2.dispose();

                    break;
                case RIGHT:
                    width = 2 * newImage.getIconWidth();
                    height = newImage.getIconHeight();

                    combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    g2 = combined.createGraphics();

                    g2.drawImage(bi1, null, 0, 0);
                    g2.drawImage(bi2, null, width / 2, 0);
                    g2.dispose();

                    break;
                case TOP:
                    width = newImage.getIconWidth();
                    height = 2 * newImage.getIconHeight();

                    combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    g2 = combined.createGraphics();

                    g2.drawImage(bi1, null, 0, height / 2);
                    g2.drawImage(bi2, null, 0, 0);
                    g2.dispose();

                    break;
                case BOTTOM:
                    width = newImage.getIconWidth();
                    height = 2 * newImage.getIconHeight();

                    combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    g2 = combined.createGraphics();

                    g2.drawImage(bi1, null, 0, 0);
                    g2.drawImage(bi2, null, 0, height / 2);
                    g2.dispose();

                    break;
                default:
                    throw new FatalException("Somehow an invalid direction was specified");
            }

            ret = new ImageIcon(combined);

        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return ret;
    }

    public static BufferedImage ImageIcon2BufferedImage(ImageIcon icon) {
        BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.createGraphics();
        icon.paintIcon(null, g, 0,0);
        g.dispose();
        return bi;
    }
}