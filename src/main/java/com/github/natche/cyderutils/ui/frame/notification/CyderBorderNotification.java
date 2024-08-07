package com.github.natche.cyderutils.ui.frame.notification;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.github.natche.cyderutils.color.CyderColor;
import com.github.natche.cyderutils.threads.ThreadUtil;
import com.github.natche.cyderutils.ui.drag.CyderDragLabel;
import com.github.natche.cyderutils.ui.frame.CyderFrame;

import java.awt.*;
import java.awt.geom.GeneralPath;

public final class CyderBorderNotification extends CyderToastNotification {
    /** The offset from the top and bottom of the frame for border notifications. */
    private static final int topBottomOffset = 5;

    /** The pixel increment for border notification animations. */
    private static final int animationIncrement = 8;

    /** The millisecond timeout for border notification animations. */
    private static final int animationTimeout = 8;

    /** The direction this border notification should appear and disappear from. */
    private final NotificationDirection notificationDirection;

    /**
     * Constructs a new border notification.
     *
     * @param builder the builder to construct this border notification from
     */
    public CyderBorderNotification(NotificationBuilder builder) {
        super(builder);
        this.notificationDirection = builder.getNotificationDirection();
    }

    /** {@inheritDoc} */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;
        paintArrowBorder(g2d);
        paintArrowFill(g2d);
    }

    /**
     * Paints the arrow border on this.
     *
     * @param g2d the 2D graphics object to paint with
     */
    private void paintArrowBorder(Graphics2D g2d) {
        CyderColor notificationCyderColor = new CyderColor(notificationBorderColor);
        notificationCyderColor.withOpacity(opacity.get());
        g2d.setColor(notificationCyderColor);

        // Artificially inflate length to draw arrow
        int componentWidth = container.getWidth() + 2 * borderLength;
        int componentHeight = container.getHeight() + 2 * borderLength;

        int halfCompWidth = componentWidth / 2;
        int halfCompHeight = componentHeight / 2;

        GeneralPath outlinePath = new GeneralPath();

        switch (arrowDirection) {
            case TOP -> {
                outlinePath.moveTo(2 * 2 + halfCompWidth - arrowLength, arrowLength);
                outlinePath.lineTo(2 * 2 + halfCompWidth, 0);
                outlinePath.lineTo(2 * 2 + (halfCompWidth) + arrowLength, arrowLength);
                outlinePath.lineTo(2 * 2 + halfCompWidth - arrowLength, arrowLength);
            }
            case LEFT -> {
                outlinePath.moveTo(arrowLength, 2 * 2 + halfCompHeight - arrowLength);
                outlinePath.lineTo(0, 2 * 2 + halfCompHeight);
                outlinePath.lineTo(arrowLength, 2 * 2 + halfCompHeight + arrowLength);
                outlinePath.moveTo(arrowLength, 2 * 2 + halfCompHeight - arrowLength);
            }
            case RIGHT -> {
                outlinePath.moveTo(2 * 2 * 2 + componentWidth, 2 * 2 + halfCompHeight - arrowLength);
                outlinePath.lineTo(2 * 2 * 2 + componentWidth + arrowLength, 2 * 2 + halfCompHeight);
                outlinePath.lineTo(2 * 2 * 2 + componentWidth, 2 * 2 + halfCompHeight + arrowLength);
                outlinePath.moveTo(2 * 2 * 2 + componentWidth, 2 * 2 + halfCompHeight - arrowLength);
            }
            case BOTTOM -> {
                outlinePath.moveTo(2 * 2 + halfCompWidth - arrowLength, 2 * 2 * 2 + componentHeight);
                outlinePath.lineTo(2 * 2 + halfCompWidth, 2 * 2 * 2 + componentHeight + arrowLength);
                outlinePath.lineTo(2 * 2 + halfCompWidth + arrowLength, 2 * 2 * 2 + componentHeight);
                outlinePath.lineTo(2 * 2 + halfCompWidth - arrowLength, 2 * 2 * 2 + componentHeight);
            }
        }

        outlinePath.closePath();
        g2d.fill(outlinePath);
    }

    /**
     * Paints the fill of the arrow on this.
     *
     * @param g2d the 2D graphics object to paint with
     */
    private void paintArrowFill(Graphics2D g2d) {
        CyderColor cyderColorNotificationBackground = new CyderColor(notificationBackgroundColor);
        cyderColorNotificationBackground.withOpacity(opacity.get());
        g2d.setColor(cyderColorNotificationBackground);

        GeneralPath fillPath = new GeneralPath();

        int componentWidth = container.getWidth();
        int componentHeight = container.getHeight();

        int halfCompWidth = container.getWidth() / 2;
        int halfCompHeight = container.getHeight() / 2;

        switch (arrowDirection) {
            case TOP -> {
                fillPath.moveTo(2 * 2 + borderLength + halfCompWidth - arrowLength, arrowLength + borderLength);
                fillPath.lineTo(2 * 2 + borderLength + halfCompWidth, borderLength);
                fillPath.lineTo(2 * 2 + borderLength + (halfCompWidth) + arrowLength, arrowLength + borderLength);
                fillPath.lineTo(2 * 2 + borderLength - arrowLength, arrowLength + borderLength);
            }
            case LEFT -> {
                fillPath.moveTo(arrowLength + borderLength, 2 * 2 + borderLength + halfCompHeight - arrowLength);
                fillPath.lineTo(borderLength, 2 * 2 + borderLength + halfCompHeight);
                fillPath.lineTo(arrowLength + borderLength, 2 * 2 + borderLength + halfCompHeight + arrowLength);
                fillPath.moveTo(arrowLength + borderLength, 2 * 2 + borderLength + halfCompHeight - arrowLength);
            }
            case RIGHT -> {
                fillPath.moveTo(2 * 2 * 2 + borderLength + componentWidth,
                        2 * 2 + halfCompHeight - arrowLength + borderLength);
                fillPath.lineTo(2 * 2 * 2 + borderLength + componentWidth + arrowLength,
                        2 * 2 + halfCompHeight + borderLength);
                fillPath.lineTo(2 * 2 * 2 + borderLength + componentWidth,
                        2 * 2 + halfCompHeight + arrowLength + borderLength);
                fillPath.moveTo(2 * 2 * 2 + borderLength + componentWidth,
                        2 * 2 + halfCompHeight - arrowLength + borderLength);
            }
            case BOTTOM -> {
                fillPath.moveTo(2 * 2 + halfCompWidth - arrowLength + borderLength,
                        2 * 2 * 2 + componentHeight + borderLength);
                fillPath.lineTo(2 * 2 + halfCompWidth + borderLength,
                        2 * 2 * 2 + componentHeight + arrowLength + borderLength);
                fillPath.lineTo(2 * 2 + halfCompWidth + arrowLength + borderLength,
                        2 * 2 * 2 + componentHeight + borderLength);
                fillPath.lineTo(2 * 2 + halfCompWidth - arrowLength + borderLength,
                        2 * 2 * 2 + componentHeight + borderLength);
            }
        }

        fillPath.closePath();
        g2d.fill(fillPath);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void appear() {
        if (appearInvoked.get()) return;
        if (animating.get()) return;
        appearInvoked.set(true);
        animating.set(true);

        Futures.submit(() -> {
            setToStartAndEndingPosition();
            setVisible(true);

            switch (notificationDirection) {
                case TOP_LEFT, LEFT, BOTTOM_LEFT -> {
                    for (int i = getX() ; i < CyderFrame.BORDER_LEN ; i += animationIncrement) {
                        if (shouldStopAnimation()) break;
                        setLocation(i, getY());
                        ThreadUtil.sleep(animationTimeout);
                    }
                }
                case TOP -> {
                    for (int i = getY() ; i < CyderDragLabel.DEFAULT_HEIGHT ; i += animationIncrement) {
                        if (shouldStopAnimation()) break;
                        setBounds(getX(), i, getWidth(), getHeight());
                        ThreadUtil.sleep(animationTimeout);
                    }
                }
                case BOTTOM -> {
                    for (int i = getY() ; i > getParent().getHeight() - CyderFrame.BORDER_LEN - getHeight()
                            ; i -= animationIncrement) {
                        if (shouldStopAnimation()) break;
                        setBounds(getX(), i, getWidth(), getHeight());
                        ThreadUtil.sleep(animationTimeout);
                    }
                }
                case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> {
                    for (int i = getX() ; i > getParent().getWidth() - getWidth() - CyderFrame.BORDER_LEN
                            ; i -= animationIncrement) {
                        if (shouldStopAnimation()) break;
                        setLocation(i, getY());
                        ThreadUtil.sleep(animationTimeout);
                    }
                }
            }

            animating.set(false);
            setToMidAnimationPosition();
            repaint();

            // todo persistent notifications check return
            if (shouldRemainVisibleUntilDismissed(visibleDuration.toMillis())) return;
            ThreadUtil.sleep(visibleDuration.toMillis());
            // todo persistent notifications check return
            disappear();
        }, appearAnimationService);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void disappear() {
        Preconditions.checkState(appearInvoked.get());
        Preconditions.checkState(!animating.get());
        if (disappearInvoked.get()) return;
        disappearInvoked.set(true);
        animating.set(true);

        Futures.submit(() -> {
            setToMidAnimationPosition();

            switch (notificationDirection) {
                case TOP_LEFT, LEFT, BOTTOM_LEFT -> {
                    for (int i = getX() ; i > CyderFrame.BORDER_LEN - getWidth() ; i -= animationIncrement) {
                        if (shouldStopAnimation()) break;
                        setBounds(i, getY(), getWidth(), getHeight());
                        ThreadUtil.sleep(animationTimeout);
                    }
                }
                case TOP -> {
                    for (int i = getY() ; i > CyderDragLabel.DEFAULT_HEIGHT - getHeight() ; i -= animationIncrement) {
                        if (shouldStopAnimation()) break;
                        setBounds(getX(), i, getWidth(), getHeight());
                        ThreadUtil.sleep(animationTimeout);
                    }
                }
                case BOTTOM -> {
                    for (int i = getY() ; i < getParent().getHeight() - CyderFrame.BORDER_LEN + getHeight()
                            ; i += animationIncrement) {
                        if (shouldStopAnimation()) break;
                        setBounds(getX(), i, getWidth(), getHeight());
                        ThreadUtil.sleep(animationTimeout);
                    }
                }
                case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> {
                    for (int i = getX() ; i < getParent().getWidth() + getWidth() - CyderFrame.BORDER_LEN
                            ; i += animationIncrement) {
                        if (shouldStopAnimation()) break;
                        setBounds(i, getY(), getWidth(), getHeight());
                        ThreadUtil.sleep(animationTimeout);
                    }
                }
            }

            setToStartAndEndingPosition();

            setVisible(false);
            Container parent = getParent();
            if (parent != null) {
                parent.remove(this);
                parent.repaint();
            }

            kill();
        }, disappearAnimationService);
    }

    /** {@inheritDoc} */
    @Override
    public void setToMidAnimationPosition() {
        int w = getWidth();
        int h = getHeight();
        int pw = getParent().getWidth();
        int ph = getParent().getHeight();
        int topDragHeight = CyderDragLabel.DEFAULT_HEIGHT;
        int sideBorderLen = CyderFrame.BORDER_LEN;
        int centerXForTopBottom = pw / 2 - w / 2 + arrowLength / 2;
        int centerYForLeftRight = topDragHeight + topBottomOffset + (ph - topDragHeight) / 2 - h / 2;

        switch (notificationDirection) {
            case TOP_LEFT -> setBounds(sideBorderLen, topDragHeight + topBottomOffset, w, h);
            case TOP -> setBounds(centerXForTopBottom, topDragHeight, w, h);
            case TOP_RIGHT -> setBounds(pw - w - sideBorderLen, topDragHeight + topBottomOffset, w, h);
            case LEFT -> setBounds(sideBorderLen, centerYForLeftRight, w, h);
            case RIGHT -> setBounds(pw - w - sideBorderLen, centerYForLeftRight, w, h);
            case BOTTOM_LEFT -> setBounds(sideBorderLen, ph - h - sideBorderLen - topBottomOffset, w, h);
            case BOTTOM -> setBounds(centerXForTopBottom, ph - h - sideBorderLen, w, h);
            case BOTTOM_RIGHT -> setBounds(pw - w - sideBorderLen, ph - h - sideBorderLen - topBottomOffset, w, h);
        }
    }

    /** Sets the position of this notification to the start/end of the animation. */
    @Override
    public void setToStartAndEndingPosition() {
        int w = getWidth();
        int h = getHeight();
        int pw = getParent().getWidth();
        int ph = getParent().getHeight();
        int topDragHeight = CyderDragLabel.DEFAULT_HEIGHT;
        int sideBorderLen = CyderFrame.BORDER_LEN;
        int centerXForTopBottom = pw / 2 - w / 2 + arrowLength / 2;
        int centerYForLeftRight = topDragHeight + topBottomOffset + (ph - topDragHeight) / 2 - h / 2;

        switch (notificationDirection) {
            case TOP_LEFT -> setBounds(-w, topDragHeight + topBottomOffset, w, h);
            case TOP -> setBounds(centerXForTopBottom, -h, w, h);
            case TOP_RIGHT -> setBounds(pw - sideBorderLen, topDragHeight + topBottomOffset, w, h);
            case LEFT -> setBounds(-w, centerYForLeftRight, w, h);
            case RIGHT -> setBounds(pw - sideBorderLen, centerYForLeftRight, w, h);
            case BOTTOM_LEFT -> setBounds(-w, ph - h - sideBorderLen - topBottomOffset, w, h);
            case BOTTOM -> setBounds(centerXForTopBottom, ph + h - sideBorderLen, w, h);
            case BOTTOM_RIGHT -> setBounds(pw - sideBorderLen, ph - h - sideBorderLen - topBottomOffset, w, h);
        }
    }
}
