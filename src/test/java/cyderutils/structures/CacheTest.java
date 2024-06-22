package cyderutils.structures;

import cyderutils.network.CommonServicePort;
import cyderutils.network.Port;
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
        assertDoesNotThrow(() -> new Cache<>(new Object(), false));
        assertDoesNotThrow(() -> new Cache<>(new Port(6969), false));

        assertDoesNotThrow(() -> new Cache<>());
    }

    /**
     * Test for the setCache method.
     */
    @Test
    void testSetCache() {
        Cache<Object> allowsNull = new Cache<>(null, true);
        Cache<Object> disallowsNull = new Cache<>(new Object(), false);

        assertDoesNotThrow(() -> allowsNull.setCache(null));
        assertThrows(NullPointerException.class, () -> disallowsNull.setCache(null));
    }

    /**
     * Tests for the getCache method.
     */
    @Test
    void testGetCache() {
        Port port = Port.from(CommonServicePort.DNS);
        Cache<Port> portCache = new Cache<>(port, false);
        assertEquals(port, portCache.getCache());
    }

    /**
     * Tests for the clear method.
     */
    @Test
    void testClear() {
        Port port = Port.from(CommonServicePort.DNS);
        Cache<Port> portCache = new Cache<>(port, false);
        assertEquals(port, portCache.getCache());
        portCache.clear();
        assertNull(portCache.getCache());
    }

    /**
     * Tests for the isCachePresent method.
     */
    @Test
    void testIsCachePresent() {
        Port port = Port.from(CommonServicePort.DNS);
        Cache<Port> portCache = new Cache<>(port, false);
        assertEquals(port, portCache.getCache());
        assertTrue(portCache.isCachePresent());
        portCache.clear();
        assertFalse(portCache.isCachePresent());
    }

    /**
     * Tests for the cacheIfNotPresent method.
     */
    @Test
    void testCacheIfNotPresent() {
        Port port = Port.from(CommonServicePort.DNS);
        Cache<Port> portCache = new Cache<>(null, true);
        assertFalse(portCache.isCachePresent());
        assertTrue(portCache.cacheIfNotPresent(port));
        assertTrue(portCache.isCachePresent());
        assertEquals(port, portCache.getCache());

        Cache<Port> notNull = new Cache<>(port, false);
        notNull.clear();
        assertThrows(NullPointerException.class, () -> notNull.cacheIfNotPresent(null));
    }

    /**
     * Tests for the set cached value updater.
     */
    @Test
    void testSetCachedValueUpdater() {
        Port port = Port.from(CommonServicePort.DNS);
        Cache<Port> portCache = new Cache<>(null, true);
        assertThrows(NullPointerException.class, () -> portCache.setCachedValueUpdater(null));
        assertDoesNotThrow(() -> portCache.setCachedValueUpdater(() -> port));
    }

    /**
     * Tests for the refreshCachedValue method.
     */
    @Test
    void testRefreshCachedValue() {

    }
}
