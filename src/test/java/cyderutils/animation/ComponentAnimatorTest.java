package cyderutils.animation;

import cyderutils.enumerations.Direction;
import cyderutils.threads.ThreadUtil;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link ComponentAnimator}.
 */
class ComponentAnimatorTest {
    /**
     * Creates a new instance of this class for testing purposes.
     */
    ComponentAnimatorTest() {}

    @Test
    void testCreation() {
        assertThrows(NullPointerException.class, () -> new ComponentAnimator(null,
                null, 0, 0));
        assertThrows(NullPointerException.class, () -> new ComponentAnimator(Direction.RIGHT,
                null, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new ComponentAnimator(Direction.RIGHT,
                new JLabel(), 0, 0));

        assertThrows(IllegalArgumentException.class, () -> new ComponentAnimator(Direction.RIGHT,
                new JLabel(), 20, 0));
        assertDoesNotThrow(() -> new ComponentAnimator(Direction.RIGHT,
                new JLabel(), 0, 20));

        assertThrows(IllegalArgumentException.class, () -> new ComponentAnimator(Direction.LEFT,
                new JLabel(), 0, 20));
        assertDoesNotThrow(() -> new ComponentAnimator(Direction.LEFT,
                new JLabel(), 20, 0));

        assertThrows(IllegalArgumentException.class, () -> new ComponentAnimator(Direction.TOP,
                new JLabel(), 0, 20));
        assertDoesNotThrow(() -> new ComponentAnimator(Direction.TOP,
                new JLabel(), 20, 0));

        assertThrows(IllegalArgumentException.class, () -> new ComponentAnimator(Direction.BOTTOM,
                new JLabel(), 20, 0));
        assertDoesNotThrow(() -> new ComponentAnimator(Direction.BOTTOM,
                new JLabel(), 0, 20));
    }

    @Test
    void testSetAnimationDelay() {
        ComponentAnimator componentAnimator = new ComponentAnimator(
                Direction.RIGHT, new JLabel(), 0, 20);
        assertThrows(NullPointerException.class, () -> componentAnimator.setAnimationDelay(null));
        assertThrows(IllegalArgumentException.class,
                () -> componentAnimator.setAnimationDelay(Duration.ofMillis(-1000)));
        assertDoesNotThrow(() -> componentAnimator.setAnimationDelay(Duration.ofMillis(1000)));
    }

    @Test
    void testSetAnimationIncrement() {
        ComponentAnimator componentAnimator = new ComponentAnimator(
                Direction.RIGHT, new JLabel(), 0, 20);
        assertThrows(IllegalArgumentException.class, () -> componentAnimator.setAnimationIncrement(-1));
        assertThrows(IllegalArgumentException.class, () -> componentAnimator.setAnimationIncrement(-20));
        assertThrows(IllegalArgumentException.class, () -> componentAnimator.setAnimationIncrement(0));
        assertDoesNotThrow(() -> componentAnimator.setAnimationIncrement(1));
        assertDoesNotThrow(() -> componentAnimator.setAnimationIncrement(20));
    }

    @Test
    void testAccessors() {
        Direction animationDirection = Direction.RIGHT;
        Component label = new JLabel();
        int animationStart = 0;
        int animationEnd = 20;

        ComponentAnimator componentAnimator = new ComponentAnimator(animationDirection, label,
                animationStart, animationEnd);
        assertEquals(animationDirection, componentAnimator.getAnimationDirection());
        assertEquals(label, componentAnimator.getAnimationComponent());
        assertEquals(animationStart, componentAnimator.getAnimationStart());
        assertEquals(animationEnd, componentAnimator.getAnimationEnd());

        int animationIncrement = 22;
        Duration animationDelay = Duration.ofMillis(40);
        componentAnimator.setAnimationIncrement(animationIncrement);
        componentAnimator.setAnimationDelay(animationDelay);
        assertEquals(animationIncrement, componentAnimator.getAnimationIncrement());
        assertEquals(animationDelay, componentAnimator.getAnimationDelay());
    }

    @Test
    void testAnimationCycle() {
        Direction animationDirection = Direction.RIGHT;
        Component label = new JLabel();
        int animationStart = 0;
        int animationEnd = 20;
        ComponentAnimator componentAnimator = new ComponentAnimator(animationDirection, label,
                animationStart, animationEnd);
        assertFalse(componentAnimator.isAnimating());
        componentAnimator.animate();
        assertTrue(componentAnimator.isAnimating());
        assertTrue(componentAnimator.stopAnimation());
        ThreadUtil.sleep(100);
        assertFalse(componentAnimator.isAnimating());
    }

    @Test
    void testEquals() {
        ComponentAnimator componentAnimator = new ComponentAnimator(
                Direction.RIGHT, new JLabel(), 0, 20);
        ComponentAnimator equal = new ComponentAnimator(
                Direction.RIGHT, new JLabel(), 0, 20);
        ComponentAnimator notEqual = new ComponentAnimator(
                Direction.LEFT, new JLabel(), 40, 20);

        assertEquals(componentAnimator, componentAnimator);
        assertEquals(componentAnimator, equal);
        assertNotEquals(componentAnimator, notEqual);
        assertNotEquals(componentAnimator, new Object());
    }

    @Test
    void testToString() {
        ComponentAnimator componentAnimator = new ComponentAnimator(
                Direction.RIGHT, new JLabel(), 0, 20);
        ComponentAnimator otherAnimator = new ComponentAnimator(
                Direction.LEFT, new JLabel(), 20, 0);
        ComponentAnimator anotherAnimator = new ComponentAnimator(
                Direction.LEFT, new JLabel(), 20, 0)
                .setAnimationDelay(Duration.ofMillis(50))
                .setAnimationIncrement(22);

        assertEquals("ComponentAnimator{isAnimating=false, stoppingAnimation=false,"
                + " animationDirection=RIGHT, animationStart=0, animationEnd=20, animationDelay=PT0.008S,"
                + " animationIncrement=4}", componentAnimator.toString());
        assertEquals("ComponentAnimator{isAnimating=false, stoppingAnimation=false,"
                + " animationDirection=LEFT, animationStart=20, animationEnd=0, animationDelay=PT0.008S,"
                + " animationIncrement=4}", otherAnimator.toString());
        assertEquals("ComponentAnimator{isAnimating=false, stoppingAnimation=false,"
                + " animationDirection=LEFT, animationStart=20, animationEnd=0, animationDelay=PT0.05S,"
                + " animationIncrement=22}", anotherAnimator.toString());
    }

    @Test
    void testHashCode() {
        ComponentAnimator componentAnimator = new ComponentAnimator(
                Direction.RIGHT, new JLabel(), 0, 20);
        ComponentAnimator equal = new ComponentAnimator(
                Direction.RIGHT, new JLabel(), 0, 20);
        ComponentAnimator notEqual = new ComponentAnimator(
                Direction.LEFT, new JLabel(), 44, 40);

        assertEquals(1871303316, componentAnimator.hashCode());
        assertEquals(1871303316, equal.hashCode());
        assertEquals(865209959, notEqual.hashCode());
        assertEquals(componentAnimator.hashCode(), componentAnimator.hashCode());
        assertEquals(componentAnimator.hashCode(), equal.hashCode());
        assertNotEquals(componentAnimator.hashCode(), notEqual.hashCode());
        assertNotEquals(componentAnimator.hashCode(), new Object().hashCode());
    }
}
