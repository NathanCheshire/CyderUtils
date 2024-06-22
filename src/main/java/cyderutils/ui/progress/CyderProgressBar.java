package cyderutils.ui.progress;

import cyderutils.strings.ToStringUtil;
import cyderutils.ui.UiUtil;

import javax.swing.*;

/**
 * A progress bar styled and configured for Cyder.
 */
public class CyderProgressBar extends JProgressBar {
    /**
     * Constructs a new CyderProgressBar.
     *
     * @param orientation the orientation of the progressbar
     */
    public CyderProgressBar(int orientation) {
        super(orientation);
        addMouseListener(UiUtil.generateUiActionLoggingMouseAdapter());
    }

    /**
     * Constructs a new CyderProgressBar.
     *
     * @param min the minimum progress bar value
     * @param max the maximum progress bar value
     */
    public CyderProgressBar(int min, int max) {
        super(min, max);
        addMouseListener(UiUtil.generateUiActionLoggingMouseAdapter());
    }

    /**
     * Constructs a new CyderProgressBar.
     *
     * @param orientation the orientation of the progressbar
     * @param min         the minimum progress bar value
     * @param max         the maximum progress bar value
     */
    public CyderProgressBar(int orientation, int min, int max) {
        super(orientation, min, max);
        addMouseListener(UiUtil.generateUiActionLoggingMouseAdapter());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ToStringUtil.commonUiComponentToString(this);
    }
}
