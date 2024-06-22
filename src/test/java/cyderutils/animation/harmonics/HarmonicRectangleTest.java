package cyderutils.animation.harmonics;

import com.google.common.collect.ImmutableList;
import cyderutils.color.CyderColors;
import cyderutils.threads.ThreadUtil;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.time.Duration;

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

        // Other constructor
        assertThrows(NullPointerException.class, () -> new HarmonicRectangle(null, null));
        assertThrows(NullPointerException.class, () -> new HarmonicRectangle(new Dimension(1, 1), null));
    }

    /**
     * Tests for the get and set animation delay method.
     */
    @Test
    void testGetAndSetAnimationDelay() {
        HarmonicRectangle rect = new HarmonicRectangle(0, 0, 10, 10);
        assertEquals(Duration.ofMillis(50), rect.getAnimationDelay());
        rect.setAnimationDelay(Duration.ofMillis(500));
        assertEquals(Duration.ofMillis(500), rect.getAnimationDelay());
    }

    /**
     * Tests for the get and set background color method.
     */
    @Test
    void testGetAndSetBackgroundColor() {
        HarmonicRectangle rect = new HarmonicRectangle(0, 0, 10, 10);
        assertEquals(CyderColors.vanilla, rect.getBackgroundColor());
        Color color = new Color(0, 51, 50);
        rect.setBackgroundColor(color);
        assertEquals(color, rect.getBackgroundColor());
    }

    /**
     * Tests for the get and set harmonic direction method.
     */
    @Test
    void testGetAndSetHarmonicDirection() {
        HarmonicRectangle rect = new HarmonicRectangle(0, 0, 10, 10);
        assertEquals(HarmonicDirection.VERTICAL, rect.getHarmonicDirection());
        rect.setHarmonicDirection(HarmonicDirection.HORIZONTAL);
        assertEquals(HarmonicDirection.HORIZONTAL, rect.getHarmonicDirection());
    }

    /**
     * Tests for the get and set animation increment method.
     */
    @Test
    void testGetAndSetAnimationIncrement() {
        HarmonicRectangle rect = new HarmonicRectangle(0, 0, 10, 10);
        assertEquals(1, rect.getAnimationIncrement());
        rect.setAnimationIncrement(22);
        assertEquals(22, rect.getAnimationIncrement());
    }

    /**
     * Tests for the animation cycle such as starting, running, and stopping.
     */
    @Test
    void testAnimationCycle() {
        HarmonicRectangle rect = new HarmonicRectangle(0, 20, 100, 20);
        rect.setAnimationIncrement(5);
        rect.setHarmonicDirection(HarmonicDirection.HORIZONTAL);

        int stepTime = 50;
        ImmutableList<Number> possibleWidthValues
                = ImmutableList.of(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100);

        rect.setAnimationDelay(Duration.ofMillis(stepTime));
        assertEquals(0, rect.getWidth());
        assertEquals(20, rect.getHeight());

        rect.animate();
        assertTrue(rect.isAnimating());
        ThreadUtil.sleep(5000);
        rect.stopAnimation();
        ThreadUtil.sleep(100); // wait for animation to stop
        assertFalse(rect.isAnimating());
        assertTrue(possibleWidthValues.contains(rect.getWidth()));
    }

    /**
     * Tests for the manu animation step method.
     */
    @Test
    void testManualAnimationStep() {
        HarmonicRectangle rect = new HarmonicRectangle(0, 0, 10, 1);
        rect.setHarmonicDirection(HarmonicDirection.HORIZONTAL);
        rect.setAnimationIncrement(5);
        assertFalse(rect.isAnimating());
        rect.takeAnimationStep();
        assertFalse(rect.isAnimating());
        assertEquals(5, rect.getWidth());
        rect.takeAnimationStep();
        assertEquals(10, rect.getWidth());
        rect.takeAnimationStep();
        assertEquals(5, rect.getWidth());
    }

    /**
     * Tests for the to string method.
     */
    @Test
    void testToString() {
        HarmonicRectangle rect = new HarmonicRectangle(0, 0, 10, 10);
        HarmonicRectangle otherRect = new HarmonicRectangle(5, 5, 15, 15);
        HarmonicRectangle maxedOutRect = new HarmonicRectangle(5, 5, 15, 15);
        maxedOutRect.setAnimationIncrement(22);
        maxedOutRect.setBackgroundColor(CyderColors.navy);
        maxedOutRect.setHarmonicDirection(HarmonicDirection.HORIZONTAL);
        maxedOutRect.setAnimationDelay(Duration.ofSeconds(1));

        assertEquals("HarmonicRectangle{minimumWidth=0, minimumHeight=0, currentWidth=0,"
                + " currentHeight=0, maximumWidth=10, maximumHeight=10, isAnimating=false}", rect.toString());
        assertEquals("HarmonicRectangle{minimumWidth=5, minimumHeight=5, currentWidth=5,"
                + " currentHeight=5, maximumWidth=15, maximumHeight=15, isAnimating=false}", otherRect.toString());
        assertEquals("HarmonicRectangle{minimumWidth=5, minimumHeight=5, currentWidth=5,"
                + " currentHeight=5, maximumWidth=15, maximumHeight=15, isAnimating=false}", maxedOutRect.toString());
    }

    /**
     * Tests for the hash code method.
     */
    @Test
    void testHashCode() {
        HarmonicRectangle rect = new HarmonicRectangle(0, 0, 10, 10);
        HarmonicRectangle equalRect = new HarmonicRectangle(0, 0, 10, 10);
        HarmonicRectangle notEqualRect = new HarmonicRectangle(0, 0, 15, 15);

        assertEquals(rect, rect);
        assertEquals(rect, equalRect);
        assertNotEquals(rect, notEqualRect);
        assertNotEquals(rect, new Object());
    }

    /**
     * Tests for the equals method.
     */
    @Test
    void testEquals() {
        HarmonicRectangle rect = new HarmonicRectangle(0, 0, 10, 10);
        HarmonicRectangle equalRect = new HarmonicRectangle(0, 0, 10, 10);
        HarmonicRectangle notEqualRect = new HarmonicRectangle(0, 0, 15, 15);

        assertEquals(11157, rect.hashCode());
        assertEquals(11157, equalRect.hashCode());
        assertEquals(16117, notEqualRect.hashCode());
        assertEquals(rect.hashCode(), equalRect.hashCode());
        assertNotEquals(rect.hashCode(), notEqualRect.hashCode());
        assertNotEquals(rect.hashCode(), new Object().hashCode());
    }
}
