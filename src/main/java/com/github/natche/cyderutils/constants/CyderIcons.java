package com.github.natche.cyderutils.constants;

import com.github.natche.cyderutils.color.CyderColors;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.image.CyderImage;
import com.github.natche.cyderutils.strings.CyderStrings;
import com.github.natche.cyderutils.utils.StaticUtil;

import javax.swing.*;

/**
 * Common {@link ImageIcon}s used throughout Cyder.
 */
public final class CyderIcons {
    /**
     * The Cyder logo.
     */
    public static final ImageIcon CYDER_ICON = new ImageIcon(StaticUtil.getStaticPath("CyderIcon.png"));

    /**
     * The length of the default background.
     */
    private static final int DEFAULT_BACKGROUND_LEN = 1000;

    /**
     * A default image with dimensions {@link #DEFAULT_BACKGROUND_LEN}x{@link #DEFAULT_BACKGROUND_LEN}.
     */
    public static final ImageIcon defaultBackground = generateDefaultBackground(
            DEFAULT_BACKGROUND_LEN,
            DEFAULT_BACKGROUND_LEN);

    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private CyderIcons() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Generates a default icon of the requested dimensions.
     * The icon will change depending on whether dark mode
     * has been activated by the current user.
     *
     * @param width  the width of the image
     * @param height the height of the image
     * @return the ImageIcon of the requested dimensions
     */
    public static ImageIcon generateDefaultBackground(int width, int height) {
        return CyderImage.fromColor(CyderColors.regularBackgroundColor, width, height).getImageIcon();
    }
}
