package cyder.animation;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.enumerations.Direction;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;

import java.awt.*;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A wrapper for a {@link Component} to perform cardinal direction animations on it.
 * This component uses a slight modification on a builder pattern; accessor methods
 * return {@code this} instance instead of {@link Void}.
 */
public final class ComponentAnimator {
    /**
     * The default animation delay.
     */
    private static final Duration DEFAULT_DELAY = Duration.ofMillis(8);

    /**
     * The default animation increment.
     */
    private static final int DEFAULT_INCREMENT = 4;

    /**
     * Whether this component is currently animating.
     */
    private final AtomicBoolean isAnimating = new AtomicBoolean(false);

    /**
     * Whether {@link #stopAnimation()} has been invoked and the animation is being stopped.
     */
    private final AtomicBoolean stoppingAnimation = new AtomicBoolean(false);

    /**
     * The direction of animation.
     */
    private final Direction animationDirection;

    /**
     * The component to animate.
     */
    private final Component animationComponent;

    /**
     * The animation starting position.
     */
    private final int animationStart;

    /**
     * The animation ending position.
     */
    private final int animationEnd;

    /**
     * The delay between animation frames.
     */
    private Duration animationDelay = DEFAULT_DELAY;

    /**
     * The animation increment amount.
     */
    private int animationIncrement = DEFAULT_INCREMENT;

    /**
     * Constructs a new ComponentAnimator instance.
     *
     * @param animationDirection the direction to animate the component towards
     * @param animationComponent the component to animate
     * @param animationStart     the starting value of the animation
     * @param animationEnd       the ending value of the animation
     * @throws NullPointerException     if the provided direction or component are null
     * @throws IllegalArgumentException if the start is less than or equal to the end
     */
    public ComponentAnimator(Direction animationDirection, Component animationComponent,
                             int animationStart, int animationEnd) {
        Preconditions.checkNotNull(animationDirection);
        Preconditions.checkNotNull(animationComponent);
        Preconditions.checkArgument(animationStart < animationEnd);

        this.animationDirection = animationDirection;
        this.animationComponent = animationComponent;
        this.animationStart = animationStart;
        this.animationEnd = animationEnd;
    }

    /**
     * Sets the delay between animation frames.
     *
     * @param animationDelay the delay between animation frames
     * @return this component animator
     * @throws NullPointerException     if the provided delay is null
     * @throws IllegalArgumentException if the provided delay is negative
     */
    @CanIgnoreReturnValue
    public ComponentAnimator setAnimationDelay(Duration animationDelay) {
        Preconditions.checkNotNull(animationDelay);
        Preconditions.checkArgument(!animationDelay.isNegative());
        this.animationDelay = animationDelay;
        return this;
    }

    /**
     * Sets the amount by which to increment the animation component by each animation frame.
     *
     * @param animationIncrement the amount to increment the animation by each frame
     * @return this component animator
     * @throws IllegalArgumentException if the provided increment is less than or equal to zero
     */
    @CanIgnoreReturnValue
    public ComponentAnimator setAnimationIncrement(int animationIncrement) {
        Preconditions.checkArgument(animationIncrement > 0);
        this.animationIncrement = animationIncrement;
        return this;
    }

    /**
     * Returns the direction to animate the component towards.
     *
     * @return the direction to animate the component towards
     */
    public Direction getAnimationDirection() {
        return animationDirection;
    }

    /**
     * Returns the component to animate.
     *
     * @return the component to animate
     */
    public Component getAnimationComponent() {
        return animationComponent;
    }

    /**
     * Returns the starting value for the animation.
     *
     * @return the starting value for the animation
     */
    public int getAnimationStart() {
        return animationStart;
    }

    /**
     * Returns the ending value for the animation.
     *
     * @return the ending value for the animation
     */
    public int getAnimationEnd() {
        return animationEnd;
    }

    /**
     * Returns the delay between animation frames.
     *
     * @return the delay between animation frames
     */
    public Duration getAnimationDelay() {
        return animationDelay;
    }

    /**
     * Returns the increment between animation frames.
     *
     * @return the increment between animation frames
     */
    public int getAnimationIncrement() {
        return animationIncrement;
    }

    /**
     * Returns whether this component is animating.
     *
     * @return whether this component is animating
     */
    public boolean isAnimating() {
        return isAnimating.get();
    }

    /**
     * Animates this component.
     *
     * @return this animator
     * @throws IllegalStateException if this component is already animating
     */
    @CanIgnoreReturnValue
    public synchronized ComponentAnimator animate() {
        Preconditions.checkArgument(!isAnimating.get());
        Preconditions.checkArgument(!stoppingAnimation.get());

        isAnimating.set(true);

        String animationThreadName = getAnimationThreadName();
        CyderThreadRunner.submit(() -> {
            switch (animationDirection) {
                case LEFT -> {
                    int y = animationComponent.getY();
                    animationComponent.setLocation(animationStart, y);
                    for (int i = animationStart ; i >= animationEnd ; i -= animationIncrement) {
                        if (stoppingAnimation.get()) break;
                        animationComponent.setLocation(i, y);
                        ThreadUtil.sleep(animationDelay);
                    }
                    animationComponent.setLocation(animationEnd, y);
                }
                case RIGHT -> {
                    int y = animationComponent.getY();
                    animationComponent.setLocation(animationStart, y);
                    for (int i = animationStart ; i <= animationEnd ; i += animationIncrement) {
                        if (stoppingAnimation.get()) break;
                        animationComponent.setLocation(i, y);
                        ThreadUtil.sleep(animationDelay);
                    }
                    animationComponent.setLocation(animationEnd, y);
                }
                case TOP -> {
                    int x = animationComponent.getX();
                    animationComponent.setLocation(x, animationStart);
                    for (int i = animationStart ; i >= animationEnd ; i -= animationIncrement) {
                        if (stoppingAnimation.get()) break;
                        animationComponent.setLocation(x, i);
                        ThreadUtil.sleep(animationDelay);
                    }
                    animationComponent.setLocation(x, animationEnd);
                }
                case BOTTOM -> {
                    int x = animationComponent.getX();
                    animationComponent.setLocation(x, animationStart);
                    for (int i = animationStart ; i <= animationEnd ; i += animationIncrement) {
                        if (stoppingAnimation.get()) break;
                        animationComponent.setLocation(x, i);
                        ThreadUtil.sleep(animationDelay);
                    }
                    animationComponent.setLocation(x, animationEnd);
                }
                case default -> throw new IllegalStateException("Invalid animation direction: " + animationDirection);
            }

            isAnimating.set(false);
            stoppingAnimation.set(false);
        }, animationThreadName);

        return this;
    }

    /**
     * Requests that the animation be stopped if presently animating.
     *
     * @return whether the animation was requested to stop
     */
    @CanIgnoreReturnValue
    public boolean stopAnimation() {
        boolean isAnimating = isAnimating();
        if (isAnimating) stoppingAnimation.set(true);
        return isAnimating;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ComponentAnimator{"
                + "isAnimating=" + isAnimating + ", "
                + "stoppingAnimation=" + stoppingAnimation + ", "
                + "animationDirection=" + animationDirection + ", "
                + "animationComponent=" + animationComponent + ", "
                + "animationStart=" + animationStart + ", "
                + "animationEnd=" + animationEnd + ", "
                + "animationDelay=" + animationDelay + ", "
                + "animationIncrement=" + animationIncrement
                + "}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = isAnimating.hashCode();
        ret = 31 * ret + stoppingAnimation.hashCode();
        ret = 31 * ret + animationDirection.hashCode();
        ret = 31 * ret + animationComponent.hashCode();
        ret = 31 * ret + animationStart;
        ret = 31 * ret + animationEnd;
        ret = 31 * ret + animationDelay.hashCode();
        ret = 31 * ret + animationIncrement;
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof ComponentAnimator)) {
            return false;
        }

        ComponentAnimator other = (ComponentAnimator) o;
        return isAnimating.equals(other.isAnimating)
                && stoppingAnimation.equals(other.stoppingAnimation)
                && animationDirection.equals(other.animationDirection)
                && animationComponent.equals(other.animationComponent)
                && animationStart == other.animationStart
                && animationEnd == other.animationEnd
                && animationDelay.equals(other.animationDelay)
                && animationIncrement == other.animationIncrement;
    }

    /**
     * Returns the name for the thread which animates this component.
     *
     * @return the name for the thread which animates this component
     */
    private String getAnimationThreadName() {
        int currentAnimationValue = switch (animationDirection) {
            case LEFT, RIGHT -> animationComponent.getX();
            case TOP, BOTTOM -> animationComponent.getY();
            default -> throw new IllegalStateException("Invalid direction: " + animationDirection);
        };

        String currentAnimationValueRepresentation = isAnimating.get()
                ? "NOT ANIMATING" : String.valueOf(currentAnimationValue);

        return "ComponentAnimator{direction=" + animationDirection
                + ", animationStart=" + animationStart
                + ", currentAnimationValue=" + currentAnimationValueRepresentation
                + ", animationEnd=" + animationEnd
                + "}";
    }
}
