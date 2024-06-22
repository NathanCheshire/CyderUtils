package cyder.structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for the {@link Cache} structure.
 */
class CacheTest {
    /**
     * Constructs a new instance of this class for testing purposes.
     */
    CacheTest() {}



    /**
     * Tests for construction of Caches.
     */
    @Test
    void testConstruction() {
        assertThrows(NullPointerException.class, () -> new Cache<>(null, false));
        assertDoesNotThrow(() -> new Cache<>(null, true));
        assertDoesNotThrow(() -> new Cache<>());


    }
}
