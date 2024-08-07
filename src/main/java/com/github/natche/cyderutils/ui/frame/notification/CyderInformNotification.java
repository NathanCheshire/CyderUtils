package com.github.natche.cyderutils.ui.frame.notification;

import com.github.natche.cyderutils.bounds.BoundsString;
import com.github.natche.cyderutils.color.CyderColors;
import com.github.natche.cyderutils.font.CyderFonts;
import com.github.natche.cyderutils.strings.StringUtil;
import com.github.natche.cyderutils.ui.drag.CyderDragLabel;
import com.github.natche.cyderutils.ui.frame.CyderFrame;
import com.github.natche.cyderutils.ui.frame.enumerations.FrameType;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.DoNotCall;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A notification expressed through a secondary pane if the content of
 * a different notification exceeded the allowable size.
 */
public final class CyderInformNotification extends CyderNotification {
    /** The notification string used for frame titles. */
    private static final String NOTIFICATION = "Notification";

    /** The top, bottom, left, and right padding between the frame and container. */
    private static final int notifyPadding = 20;

    /** The font to use for the text of the notify label. */
    private static final Font notifyFont = CyderFonts.DEFAULT_FONT_SMALL;

    /** Whether this notification has been killed. */
    private final AtomicBoolean killed = new AtomicBoolean();

    /** Whether the appear method has been invoked. */
    private final AtomicBoolean appearInvoked = new AtomicBoolean();

    /** Whether the disappear method has been invoked. */
    private final AtomicBoolean disappearInvoked = new AtomicBoolean();

    /** The frame used to show the container of this notification. */
    private CyderFrame notificationFrame;

    /** The container this notification will display. */
    private final Container container;

    /** The frame to position the notification frame relative to. */
    private final CyderFrame relativeFrame;

    /** The html text for this notification if a custom container is not specified. */
    private final String htmlText;

    /** The listener used to reposition the notification frame relative to the relative frame. */
    private ComponentListener repositionNotificationFrameListener;

    /**
     * Constructs a new inform notification.
     *
     * @param builder       the builder
     * @param relativeFrame the frame to set the notification relative to
     */
    public CyderInformNotification(NotificationBuilder builder, CyderFrame relativeFrame) {
        Preconditions.checkNotNull(builder);
        Preconditions.checkNotNull(relativeFrame);

        container = builder.getContainer();
        this.relativeFrame = relativeFrame;
        this.htmlText = builder.getHtmlText();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void appear() {
        Preconditions.checkState(!appearInvoked.get());
        appearInvoked.set(true);

        int containerX = CyderFrame.BORDER_LEN + notifyPadding;
        int containerY = CyderDragLabel.DEFAULT_HEIGHT + notifyPadding;

        if (StringUtil.isNullOrEmpty(htmlText)) {
            int width = container.getWidth() + 2 * CyderFrame.BORDER_LEN + 2 * notifyPadding;
            int height = container.getHeight() + CyderDragLabel.DEFAULT_HEIGHT
                    + CyderFrame.BORDER_LEN + 2 * notifyPadding;
            notificationFrame = new CyderFrame.Builder()
                    .setWidth(width)
                    .setHeight(height)
                    .build();
            container.setLocation(containerX, containerY);
            notificationFrame.getContentPane().add(container);
        } else {
            BoundsString bounds = new BoundsString.Builder(htmlText)
                    .setFont(notifyFont)
                    .setMaxWidth(1200)
                    .build();
            JLabel label = new JLabel(bounds.getText());
            label.setBounds(containerX, containerY, (int) bounds.getWidth(), (int) bounds.getHeight());
            label.setForeground(CyderColors.navy);
            label.setFont(notifyFont);
            int width = (int) bounds.getWidth() + 2 * CyderFrame.BORDER_LEN + 2 * notifyPadding;
            int height = (int) bounds.getHeight() + CyderDragLabel.DEFAULT_HEIGHT
                    + CyderFrame.BORDER_LEN + 2 * notifyPadding;
            notificationFrame = new CyderFrame.Builder()
                    .setWidth(width)
                    .setHeight(height)
                    .build();
            notificationFrame.getContentPane().add(label);
        }

        String frameTitle = relativeFrame.getTitle() + " (" + NOTIFICATION + ")";
        notificationFrame.setTitle(frameTitle);
        notificationFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                disappear();
            }
        });
        repositionNotificationFrameListener = new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                setToMidAnimationPosition();
            }
        };
        relativeFrame.addComponentListener(repositionNotificationFrameListener);
        notificationFrame.setFrameType(FrameType.POPUP);
        notificationFrame.finalizeAndShow(relativeFrame);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void disappear() {
        Preconditions.checkState(appearInvoked.get());
        Preconditions.checkState(!disappearInvoked.get());
        disappearInvoked.set(true);

        if (notificationFrame != null && !notificationFrame.isDisposed()) {
            notificationFrame.dispose();
        }

        kill();
    }

    /** {@inheritDoc} */
    @Override
    public void kill() {
        relativeFrame.removeComponentListener(repositionNotificationFrameListener);
        killed.set(true);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isKilled() {
        return killed.get();
    }

    /**
     * Has no effect on {@link CyderInformNotification}s. Do not invoke this method.
     *
     * @param ignored the boolean to completely ignore
     */
    @Override
    @DoNotCall
    public void setHovered(boolean ignored) {}

    /** {@inheritDoc} */
    @Override
    public Optional<String> getLabelText() {
        if (container instanceof JLabel label && !StringUtil.isNullOrEmpty(label.getText())) {
            return Optional.of(label.getText());
        }

        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public void setToStartAndEndingPosition() {
        if (notificationFrame != null) notificationFrame.setLocationRelativeTo(relativeFrame);
    }

    /** {@inheritDoc} */
    @Override
    public void setToMidAnimationPosition() {
        if (notificationFrame != null) notificationFrame.setLocationRelativeTo(relativeFrame);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAnimating() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String getContainerToString() {
        return container.toString();
    }
}
