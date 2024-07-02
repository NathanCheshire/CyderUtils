package com.github.natche.cyderutils.animation.harmonics;

import com.google.common.base.Preconditions;
import com.github.natche.cyderutils.annotations.ForReadability;
import com.github.natche.cyderutils.color.CyderColors;
import com.github.natche.cyderutils.threads.CyderThreadRunner;
import com.github.natche.cyderutils.threads.ThreadUtil;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/** A rectangle which can grow and shrink (oscillate) in a specific cardinal direction (horizontally or vertically). */
public final class HarmonicRectangle extends JLabel {
    /** Whether the rectangle is animating currently. */
    private final AtomicBoolean isAnimating = new AtomicBoolean(false);

    /** Whether the animation has been requested to stop. */
    private final AtomicBoolean stoppingAnimation = new AtomicBoolean(false);

    /** The maximum allowable width for the component. */
    private final int maxWidth;

    /** The maximum allowable height for the component. */
    private final int maxHeight;

    /** The minimum allowable height for the component. */
    private final int minWidth;

    /** The minimum allowable width for the component. */
    private final int minHeight;

    /** The amount to increase or decrease the animation direction by. */
    private int animationIncrement = 1;

    /** The delay between animation updates. */
    private Duration animationDelay = Duration.ofMillis(50);

    /** The background color of the drawn component. */
    private Color backgroundColor = CyderColors.vanilla;

    /** The current direction of harmonic oscillation. */
    private HarmonicDirection harmonicDirection = HarmonicDirection.VERTICAL;

    /** The current state of the harmonic direction. */
    private ScalingDirection scalingDirection = ScalingDirection.INCREASING;

    /**
     * Constructs a new instance of a HarmonicRectangle from the provided initial dimensions.
     * Minimum size will be set during construction.
     *
     * @param minimumSize the minimum size
     * @param maximumSize the maximum size
     * @throws NullPointerException if either of the dimensions are null
     */
    public HarmonicRectangle(Dimension minimumSize, Dimension maximumSize) {
        this(Preconditions.checkNotNull(minimumSize).width,
                Preconditions.checkNotNull(minimumSize).height,
                Preconditions.checkNotNull(maximumSize).width,
                Preconditions.checkNotNull(maximumSize).height);
    }

    /**
     * Constructs a new instance of a HarmonicRectangle from the provided initial dimensions.
     * Minimum size will be set during construction.
     *
     * @param minWidth  the minimum width
     * @param minHeight the minimum height
     * @param maxWidth  the maximum width
     * @param maxHeight the maximum height
     * @throws IllegalArgumentException if either of the minimum dimensions are less than their
     *                                  corresponding maximum dimensions, or if either of the minimum
     *                                  dimensions are less than zero, or if either of the maximum dimensions
     *                                  are less than or equal to zero.
     */
    public HarmonicRectangle(int minWidth, int minHeight, int maxWidth, int maxHeight) {
        Preconditions.checkArgument(maxWidth > 0);
        Preconditions.checkArgument(maxHeight > 0);
        Preconditions.checkArgument(minWidth >= 0);
        Preconditions.checkArgument(minHeight >= 0);
        Preconditions.checkArgument(minWidth < maxWidth || minHeight < maxHeight);

        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.minWidth = minWidth;
        this.minHeight = minHeight;

        super.setSize(minWidth, minHeight);
    }

    /**
     * Returns the delay between animation frame updates.
     *
     * @return the delay between animation frame updates
     */
    public Duration getAnimationDelay() {
        return animationDelay;
    }

    /**
     * Sets the delay between animation frame updates.
     *
     * @param animationDelay the delay between animation frame updates
     * @throws NullPointerException     if the provided animation delay is null
     * @throws IllegalArgumentException if the provided duration is negative
     */
    public void setAnimationDelay(Duration animationDelay) {
        Preconditions.checkNotNull(animationDelay);
        Preconditions.checkArgument(!animationDelay.isNegative());
        this.animationDelay = animationDelay;
    }

    /**
     * Returns the background color.
     *
     * @return the background color
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color.
     *
     * @param backgroundColor the background color
     * @throws NullPointerException if the provided color is null
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = Preconditions.checkNotNull(backgroundColor);
    }

    /**
     * Returns the animation to oscillate in.
     *
     * @return the animation to oscillate in
     */
    public HarmonicDirection getHarmonicDirection() {
        return harmonicDirection;
    }

    /**
     * Sets the direction to oscillate in.
     *
     * @param harmonicDirection the direction to oscillate in
     * @throws NullPointerException if the provided direction is null
     */
    public void setHarmonicDirection(HarmonicDirection harmonicDirection) {
        this.harmonicDirection = Preconditions.checkNotNull(harmonicDirection);
    }

    /**
     * Returns the increment to increase/decrease the animation side by.
     *
     * @return the increment to increase/decrease the animation side by
     */
    public int getAnimationIncrement() {
        return animationIncrement;
    }

    /**
     * Sets the increment to increase/decrease the animation side by.
     *
     * @param animationIncrement the increment to increase/decrease the animation side by
     * @throws NullPointerException if the provided increment value is less than or equal to zero
     */
    public void setAnimationIncrement(int animationIncrement) {
        Preconditions.checkArgument(animationIncrement > 0);
        this.animationIncrement = animationIncrement;
    }

    /**
     * Returns whether the rectangle is currently in animation.
     *
     * @return whether the rectangle is currently in animation
     */
    public boolean isAnimating() {
        return isAnimating.get();
    }

    /**
     * Starts the animation.
     *
     * @throws IllegalStateException if already animating or waiting to stop animating
     */
    public void animate() {
        Preconditions.checkState(!isAnimating.get());
        Preconditions.checkState(!stoppingAnimation.get());

        isAnimating.set(true);

        CyderThreadRunner.submit(() -> {
            while (isAnimating.get() && !stoppingAnimation.get()) {
                innerAnimationStep();
                ThreadUtil.sleep(animationDelay);
            }

            isAnimating.set(false);
            stoppingAnimation.set(false);
        }, getAnimationThreadName());
    }

    /**
     * Returns the name of the thread to animate this harmonic rectangle.
     *
     * @return the name of the thread to animate this harmonic rectangle
     */
    @ForReadability
    private String getAnimationThreadName() {
        return "HarmonicRectangleAnimatorThread{"
                + "harmonicDirection=" + harmonicDirection
                + ", minWidth=" + minWidth
                + ", minHeight=" + minHeight
                + ", maxWidth=" + maxWidth
                + ", maxHeight=" + maxHeight
                + "}";
    }

    /**
     * Takes an animation step manually provided the animation is not
     * presently running and has not been requested to stop.
     *
     * @throws IllegalStateException if the animation is currently running or has been requested to stop
     */
    public void takeAnimationStep() {
        Preconditions.checkState(!isAnimating.get());
        Preconditions.checkState(!stoppingAnimation.get());

        innerAnimationStep();
    }

    /** Performs a singular animation frame step. */
    private void innerAnimationStep() {
        switch (harmonicDirection) {
            case HORIZONTAL -> innerHorizontalAnimationStep();
            case VERTICAL -> innerVerticalAnimationStep();
            default -> throw new IllegalStateException("Invalid harmonic direction: " + harmonicDirection);
        }

        revalidate();
        repaint();
    }

    /** Takes an animation step in the horizontal direction. */
    @ForReadability
    private void innerHorizontalAnimationStep() {
        int width = getWidth();
        int height = getHeight();
        int increasedWidth = width + animationIncrement;
        int decreasedWidth = width - animationIncrement;

        switch (scalingDirection) {
            case INCREASING -> {
                if (increasedWidth < maxWidth) {
                    setSize(increasedWidth, height);
                } else {
                    setSize(maxWidth, height);
                    scalingDirection = ScalingDirection.DECREASING;
                }
            }
            case DECREASING -> {
                if (decreasedWidth > minWidth) {
                    setSize(decreasedWidth, height);
                } else {
                    setSize(minWidth, height);
                    scalingDirection = ScalingDirection.INCREASING;
                }
            }
            default -> throw new IllegalStateException("Invalid scaling direction: " + scalingDirection);
        }
    }

    /** Takes an animation step in the vertical direction. */
    @ForReadability
    private void innerVerticalAnimationStep() {
        int width = getWidth();
        int height = getHeight();
        int increasedHeight = height + animationIncrement;
        int decreasedHeight = height - animationIncrement;

        switch (scalingDirection) {
            case INCREASING -> {
                if (increasedHeight < maxHeight) {
                    setSize(width, increasedHeight);
                } else {
                    setSize(width, maxHeight);
                    scalingDirection = ScalingDirection.DECREASING;
                }
            }
            case DECREASING -> {
                if (decreasedHeight > minHeight) {
                    setSize(width, decreasedHeight);
                } else {
                    setSize(width, minHeight);
                    scalingDirection = ScalingDirection.INCREASING;
                }
            }
            default -> throw new IllegalStateException("Invalid scaling direction: " + scalingDirection);
        }
    }

    /** Stops the animation if animating. */
    public void stopAnimation() {
        stoppingAnimation.set(true);
    }

    /** {@inheritDoc} */
    @Override
    public void paint(Graphics g) {
        g.setColor(backgroundColor);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "HarmonicRectangle{"
                + "minimumWidth=" + minWidth
                + ", minimumHeight=" + minHeight
                + ", currentWidth=" + getWidth()
                + ", currentHeight=" + getHeight()
                + ", maximumWidth=" + maxWidth
                + ", maximumHeight=" + maxHeight
                + ", isAnimating=" + isAnimating
                + "}";
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int ret = Integer.hashCode(minWidth);
        ret = 31 * ret + Integer.hashCode(minHeight);
        ret = 31 * ret + Integer.hashCode(getWidth());
        ret = 31 * ret + Integer.hashCode(getHeight());
        ret = 31 * ret + Integer.hashCode(maxWidth);
        ret = 31 * ret + Integer.hashCode(maxHeight);
        ret = 31 * ret + Boolean.hashCode(isAnimating.get());
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof HarmonicRectangle)) {
            return false;
        }

        HarmonicRectangle other = (HarmonicRectangle) o;
        return minWidth == other.minWidth
                && minHeight == other.minHeight
                && getWidth() == other.getWidth()
                && getHeight() == other.getHeight()
                && maxWidth == other.maxWidth
                && maxHeight == other.maxHeight
                && isAnimating.get() == other.isAnimating.get();
    }
}
