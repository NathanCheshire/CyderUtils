package cyder.font;

import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;

import java.awt.*;

/**
 * Common fonts used throughout Cyder.
 * <p>
 * Format for new fonts: NAME_SIZE unless there is a rare exception that applies.
 */
@SuppressWarnings("unused")
public final class CyderFonts {
    public static final String SEGOE_UI_BLACK = "Segoe UI Black";
    public static final String AGENCY_FB = "Agency FB";
    public static final String TAHOMA = "tahoma";

    public static final Font SEGOE_20 = new FontBuilder(SEGOE_UI_BLACK).setSize(20).generate();
    public static final Font SEGOE_30 = new FontBuilder(SEGOE_UI_BLACK).setSize(30).generate();

    public static final Font AGENCY_FB_22 = new FontBuilder(AGENCY_FB).setSize(22).generate();
    public static final Font AGENCY_FB_30 = new FontBuilder(AGENCY_FB).setSize(30).generate();
    public static final Font AGENCY_FB_35 = new FontBuilder(AGENCY_FB).setSize(35).generate();

    public static final Font DEFAULT_FONT_SMALL = AGENCY_FB_22;
    public static final Font DEFAULT_FONT = AGENCY_FB_30;
    public static final Font DEFAULT_FONT_LARGE = AGENCY_FB_35;

    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private CyderFonts() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }
}
