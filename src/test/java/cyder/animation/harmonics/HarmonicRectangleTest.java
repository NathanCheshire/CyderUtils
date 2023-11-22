package cyder.animation.harmonics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for a {@link HarmonicRectangle}.
 */
public class HarmonicRectangleTest {
    /**
     * Constructs a new instance of this class for testing purposes.
     */
    HarmonicRectangleTest() {}

    /**
     * Tests for construction of harmonic rectangles.
     */
    @Test
    void testConstruction() {
        assertThrows(IllegalArgumentException.class,
                () -> new HarmonicRectangle(0, 0, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new HarmonicRectangle(0, 0, 1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new HarmonicRectangle(0, 0, 0, 10));
        assertThrows(IllegalArgumentException.class,
                () -> new HarmonicRectangle(0, 0, 10, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new HarmonicRectangle(-1, 0, 10, 10));
        assertThrows(IllegalArgumentException.class,
                () -> new HarmonicRectangle(0, -1, 10, 10));
        assertThrows(IllegalArgumentException.class,
                () -> new HarmonicRectangle(10, 10, 10, 10));
        assertThrows(IllegalArgumentException.class,
                () -> new HarmonicRectangle(10, 10, 9, 9));

        assertDoesNotThrow(() -> new HarmonicRectangle(0, 0, 1, 1));
        assertDoesNotThrow(() -> new HarmonicRectangle(0, 0, 10, 10));
    }

    /**
     * Tests for teh get animation delay method.
     */
    @Test
    void testGetAnimationDelay() {

    }

    /**
     * Tests for the set animation delay method.
     */
    @Test
    void testSetAnimationDelay() {

    }

    /**
     * Tests for the get background color method.
     */
    @Test
    void testGetBackgroundColor() {

    }

    /**
     * Tests for the set background color method.
     */
    @Test
    void testSetBackgroundColor() {

    }

    /**
     * Tests for the get harmonic direction method.
     */
    @Test
    void testGetHarmonicDirection() {

    }

    /**
     * Tests for the set harmonic direction method.
     */
    @Test
    void testSetHarmonicDirection() {

    }

    /**
     * Tests for the get animation increment method.
     */
    @Test
    void testGetAnimationIncrement() {

    }

    /**
     * Tests for the set animation increment method.
     */
    @Test
    void testSetAnimationIncrement() {

    }

    /**
     * Tests for the animation cycle such as starting, running, and stopping.
     */
    @Test
    void testAnimationCycle() {

    }

    /**
     * Tests for the manu animation step method.
     */
    @Test
    void testManualAnimationStep() {

    }

    /**
     * Tests for the to string method.
     */
    @Test
    void testToString() {

    }

    /**
     * Tests for the hash code method.
     */
    @Test
    void testHashCode() {

    }

    /**
     * Tests for the equals method.
     */
    @Test
    void testEquals() {

    }
}
