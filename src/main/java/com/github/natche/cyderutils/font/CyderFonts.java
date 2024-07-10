package com.github.natche.cyderutils.font;

import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.strings.CyderStrings;

import java.awt.*;

/**
 * Common fonts used throughout Cyder.
 * <p>
 * Format for new fonts: NAME_SIZE unless there is a rare exception that applies.
 */
public final class CyderFonts {
    public static final String SEGOE_UI_BLACK = "Segoe UI Black";
    public static final String AGENCY_FB = "Agency FB";
    public static final String TAHOMA = "tahoma";

    public static final Font SEGOE_20 = new FontBuilder(SEGOE_UI_BLACK).setSize(20).build();
    public static final Font SEGOE_30 = new FontBuilder(SEGOE_UI_BLACK).setSize(30).build();

    public static final Font AGENCY_FB_22 = new FontBuilder(AGENCY_FB).setSize(22).build();
    public static final Font AGENCY_FB_30 = new FontBuilder(AGENCY_FB).setSize(30).build();
    public static final Font AGENCY_FB_35 = new FontBuilder(AGENCY_FB).setSize(35).build();

    // todo manager for fonts?

    public static final Font DEFAULT_FONT_SMALL = AGENCY_FB_22;
    public static final Font DEFAULT_FONT = AGENCY_FB_30;
    public static final Font DEFAULT_FONT_LARGE = AGENCY_FB_35;

    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private CyderFonts() {
        throw new IllegalMethodException("Instances of CyderFonts are not allowed");
    }
}
