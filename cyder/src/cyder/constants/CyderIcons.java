package cyder.constants;

import cyder.utilities.ImageUtil;
import cyder.utilities.ScreenUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Common ImageIcons used throughout Cyder for buttons
 */
public class CyderIcons {
    /**
     * The Cyder logo.
     */
    public static final ImageIcon CYDER_ICON = new ImageIcon("static/pictures/CyderIcon.png");

    /**
     * The Cyder logo used to indicate a background process is running.
     */
    public static final ImageIcon CYDER_ICON_BLINK = new ImageIcon("static/pictures/CyderIconBlink.png");

    /**
     * The x easter egg icon.
     */
    public static final ImageIcon xxxIcon = new ImageIcon("static/pictures/print/x.png");

    /**
     * The current icon to be used for CyderFrames.
     */
    private static ImageIcon currentCyderIcon = CYDER_ICON;

    /**
     * Minimize icons used for CyderFrame DragLabels
     */
    public static final ImageIcon minimizeIcon = new ImageIcon("static/pictures/windowicons/minimize1.png");
    public static final ImageIcon minimizeIconHover = new ImageIcon("static/pictures/windowicons/minimize2.png");

    /**
     * Change Size icons used for occasional frames to change the size of
     * the frame or in ConsoleFrame's case, switch the backgroune
     */
    public static final ImageIcon changeSizeIcon = new ImageIcon("static/pictures/icons/changesize1.png");
    public static final ImageIcon changeSizeIconHover = new ImageIcon("static/pictures/icons/changesize2.png");

    /**
     * Pin icons used for setting a frame to always on top,
     * pin3.png is used to indicate a frame is pinned to the ConsoleFrame
     */
    public static final ImageIcon pinIcon = new ImageIcon("static/pictures/icons/pin1.png");
    public static final ImageIcon pinIconHover = new ImageIcon("static/pictures/icons/pin2.png");
    public static final ImageIcon pinIconHoverPink = new ImageIcon("static/pictures/icons/pin3.png");

    /**
     * Close icons used for closing CyderFrames
     */
    public static final ImageIcon closeIcon = new ImageIcon("static/pictures/windowicons/Close1.png");
    public static final ImageIcon closeIconHover = new ImageIcon("static/pictures/windowicons/Close2.png");

    /**
     * Help icons used for ConsoleFrame
     */
    public static final ImageIcon helpIcon = new ImageIcon("static/pictures/icons/help1.png");
    public static final ImageIcon helpIconHover = new ImageIcon("static/pictures/icons/help2.png");

    /**
     * A default image with dimensions 1000x1000
     */
    public static final ImageIcon defaultBackground = generateDefaultBackground(1000,1000);

    /**
     * A default image that spans the size of the primar display
     */
    public static final ImageIcon defaultBackgroundLarge = generateDefaultBackground(
            Math.max(ScreenUtil.getScreenWidth(), ScreenUtil.getScreenHeight()),
            Math.max(ScreenUtil.getScreenWidth(), ScreenUtil.getScreenHeight()));

    /**
     * Instantiation of images class not allowed
     */
    private CyderIcons() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    /**
     * Generates a default image of the requested dimensions.
     * The image consists of a gradient between shades of white.
     *
     * @param width the width of the image
     * @param height the height of the image
     * @return the ImageIcon of the requested dimensions
     */
    public static ImageIcon generateDefaultBackground(int width, int height) {
        return new ImageIcon(ImageUtil.getImageGradient(width, height,
                new Color(252,245,255),
                new Color(164,154,187),
                new Color(249, 233, 241)));
    }

    public static ImageIcon getCurrentCyderIcon() {
        return currentCyderIcon;
    }

    public static void setCurrentCyderIcon(ImageIcon currentCyderIcon) {
        CyderIcons.currentCyderIcon = currentCyderIcon;
    }
}
