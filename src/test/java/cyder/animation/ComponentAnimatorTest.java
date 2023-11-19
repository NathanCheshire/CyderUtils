package cyder.animation;

import cyder.enumerations.Direction;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

class ComponentAnimatorTest {
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
        assertThrows(IllegalArgumentException.class, () -> new ComponentAnimator(Direction.LEFT,
                new JLabel(), 0, 20));
        assertThrows(IllegalArgumentException.class, () -> new ComponentAnimator(Direction.TOP,
                new JLabel(), 0, 20));
        assertThrows(IllegalArgumentException.class, () -> new ComponentAnimator(Direction.BOTTOM,
                new JLabel(), 20, 0));
    }

    @Test
    void testMutators() {

    }

    @Test
    void testAccessors() {

    }

    @Test
    void testAnimationCycle() {

    }

    @Test
    void testEquals() {

    }

    @Test
    void testToString() {

    }

    @Test
    void testHashCode() {

    }
}
