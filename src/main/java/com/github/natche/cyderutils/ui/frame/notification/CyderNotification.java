package com.github.natche.cyderutils.ui.frame.notification;

import com.github.natche.cyderutils.ui.frame.CyderFrame;

import javax.swing.*;
import java.awt.*;

/** A notification for a {@link CyderFrame}. */
public abstract class CyderNotification extends JLabel implements ICyderNotification {
    /** The background used for notifications. */
    static final Color notificationBackgroundColor = new Color(0, 0, 0);

    /** The color used for notification borders. */
    static final Color notificationBorderColor = new Color(26, 32, 51);

    /** The magic number used to denote a notification should be shown until dismissed. */
    private static final int showUntilDismissed = -1;

    /**
     * Returns whether the provided duration is indicative that a notification
     * should remain visible until dismissed by a user.
     *
     * @param duration the duration
     * @return whether the provided duration is indicative that a notification
     * should remain visible until dismissed by a user
     */
    boolean shouldRemainVisibleUntilDismissed(long duration) {
        return duration == showUntilDismissed;
    }
}
