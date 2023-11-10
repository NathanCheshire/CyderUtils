package cyder.animation;

import com.google.common.base.Preconditions;
import cyder.enumerations.Direction;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;

import java.awt.*;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utilities to animate components.
 */
public final class AnimationUtil {
    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private AnimationUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    public static final class ComponentAnimator {
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
         * Constructs a new builder.
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
         * @return this builder
         * @throws NullPointerException     if the provided delay is null
         * @throws IllegalArgumentException if the provided delay is negative
         */
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
         * @return this builder
         * @throws IllegalArgumentException if the provided increment is less than or equal to zero
         */
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
         * @throws IllegalStateException if this component is already animating
         */
        public synchronized void animate() {
            Preconditions.checkArgument(!isAnimating.get());
            Preconditions.checkArgument(!stoppingAnimation.get());

            isAnimating.set(true);

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
                    case default ->
                            throw new IllegalStateException("Invalid animation direction: " + animationDirection);
                }

                isAnimating.set(false);
                stoppingAnimation.set(false);
            }, "todo");
        }

        public void stopAnimation() {
            stoppingAnimation.set(true);
        }
    }
}
