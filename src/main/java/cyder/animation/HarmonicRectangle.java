package cyder.animation;

import com.google.common.base.Preconditions;
import cyder.constants.CyderColors;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;

/**
 * A rectangle which can grow and shrink (oscillate) in a specific cardinal direction (horizontally or vertically).
 */
public final class HarmonicRectangle extends JLabel {
    /**
     * The current width.
     */
    private int currentWidth;

    /**
     * The current height.
     */
    private int currentHeight;

    /**
     * The maximum allowable width for the component.
     */
    private final int maxWidth;

    /**
     * The maximum allowable height for the component.
     */
    private final int maxHeight;

    /**
     * The minimum allowable height for the component.
     */
    private final int minWidth;

    /**
     * The minimum allowable width for the component.
     */
    private final int minHeight;

    /**
     * The amount to increase or decrease the animation direction by.
     */
    private int animationIncrement = 1;

    /**
     * The delay between animation updates.
     */
    private Duration animationDelay = Duration.ofMillis(50);

    /**
     * The background color of the drawn component.
     */
    private Color backgroundColor = CyderColors.vanilla;

    /**
     * The current direction of harmonic oscillation.
     */
    private HarmonicDirection harmonicDirection = HarmonicDirection.VERTICAL;

    /**
     * The position directions of harmonic oscillation.
     */
    public enum HarmonicDirection {
        /**
         * The rectangle will oscillate in the vertical direction.
         */
        VERTICAL,

        /**
         * The rectangle will oscillate in the horizontal direction.
         */
        HORIZONTAL
    }

    /**
     * The current state of the harmonic direction.
     */
    private ScalingDirection scalingDirection = ScalingDirection.INCREASING;

    /**
     * The possible states of the harmonic direction.
     */
    public enum ScalingDirection {
        /**
         * The rectangle is currently increasing.
         */
        INCREASING,

        /**
         * The rectangle is currently decreasing.
         */
        DECREASING
    }

    /**
     * Suppress default constructor.
     */
    private HarmonicRectangle() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Constructs a new harmonic rectangle.
     *
     * @param minWidth  the minimum width
     * @param minHeight the minimum height
     * @param maxWidth  the maximum width
     * @param maxHeight the maximum height
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
     * {@inheritDoc}
     */
    @Override
    public int getWidth() {
        return currentWidth;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHeight() {
        return currentHeight;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSize(int width, int height) {
        currentWidth = width;
        currentHeight = height;

        super.setSize(width, height);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBounds(int x, int y, int width, int height) {
        currentWidth = width;
        currentHeight = height;

        super.setBounds(x, y, width, height);
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
     */
    public void setAnimationIncrement(int animationIncrement) {
        Preconditions.checkArgument(animationIncrement > 0);
        this.animationIncrement = animationIncrement;
    }

    /**
     * Whether the rectangle is animating currently.
     */
    private boolean isAnimating;

    /**
     * Returns whether the rectangle is currently in animation.
     *
     * @return whether the rectangle is currently in animation
     */
    public boolean isAnimating() {
        return isAnimating;
    }

    /**
     * The default animation thread name.
     */
    private static final String DEFAULT_ANIMATION_THREAD_NAME = "Harmonic Rectangle Animator";

    /**
     * Starts the animation.
     * If the animation is already running the method returns immediately.
     */
    public void startAnimation() {
        if (isAnimating) return;
        isAnimating = true;

        CyderThreadRunner.submit(() -> {
            while (isAnimating) {
                animationStep();
                ThreadUtil.sleep(animationDelay);
            }
        }, DEFAULT_ANIMATION_THREAD_NAME);
    }

    /**
     * Takes an animation step.
     */
    public void animationStep() {
        switch (harmonicDirection) {
            case HORIZONTAL:
                switch (scalingDirection) {
                    case INCREASING:
                        if (currentWidth + animationIncrement < maxWidth) {
                            currentWidth += animationIncrement;
                        } else {
                            currentWidth = maxWidth;
                            scalingDirection = ScalingDirection.DECREASING;
                        }

                        break;
                    case DECREASING:
                        if (currentWidth - animationIncrement > minWidth) {
                            currentWidth -= animationIncrement;
                        } else {
                            currentWidth = minWidth;
                            scalingDirection = ScalingDirection.INCREASING;
                        }

                        break;
                    default:
                        throw new IllegalStateException("Invalid delta direction: " + scalingDirection);
                }

                break;
            case VERTICAL:
                switch (scalingDirection) {
                    case INCREASING:
                        if (currentHeight + animationIncrement < maxHeight) {
                            currentHeight += animationIncrement;
                        } else {
                            currentHeight = maxHeight;
                            scalingDirection = ScalingDirection.DECREASING;
                        }

                        break;
                    case DECREASING:
                        if (currentHeight - animationIncrement > minHeight) {
                            currentHeight -= animationIncrement;
                        } else {
                            currentHeight = minHeight;
                            scalingDirection = ScalingDirection.INCREASING;
                        }

                        break;
                    default:
                        throw new IllegalStateException("Invalid delta direction: " + scalingDirection);
                }

                break;
            default:
                throw new IllegalStateException("Invalid harmonic direction: " + harmonicDirection);
        }

        setSize(currentWidth, currentHeight);
        revalidate();
        repaint();
    }

    /**
     * Stops the animation of on-going.
     */
    public void stopAnimation() {
        isAnimating = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paint(Graphics g) {
        g.setColor(backgroundColor);
        g.fillRect(0, 0, currentWidth, currentHeight);
    }
}
