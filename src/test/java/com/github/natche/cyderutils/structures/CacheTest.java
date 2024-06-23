package com.github.natche.cyderutils.structures;

import com.github.natche.cyderutils.network.CommonServicePort;
import com.github.natche.cyderutils.network.Port;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for the {@link Cache} structure.
 */
class CacheTest {
    /**
     * An implementation of a possible cache refresh supplier.
     */
    private final CacheRefreshSupplier<Port> cacheRefreshSupplier = new CacheRefreshSupplier<>() {
        @Override
        public Port get() {
            // You can do lots of other logic here like async calls
            return new Port(1738);
        }

        @Override
        public String toString() {
            return "1738.port.supplier";
        }
    };

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
        Cache<Port> portCache = new Cache<>(null, true);
        assertThrows(NullPointerException.class, () -> portCache.setCachedValueUpdater(null));
        assertDoesNotThrow(() -> portCache.setCachedValueUpdater(cacheRefreshSupplier));
    }

    /**
     * Tests for the refreshCachedValue method.
     */
    @Test
    void testRefreshCachedValue() {
        Port port = Port.from(CommonServicePort.DNS);
        Cache<Port> portCache = new Cache<>(null, true);
        assertThrows(IllegalStateException.class, portCache::refreshCachedValue);

        portCache.setCache(port);
        Port httpsPort = Port.from(CommonServicePort.HTTPS);
        portCache.setCachedValueUpdater(cacheRefreshSupplier);
        assertDoesNotThrow(portCache::refreshCachedValue);
        assertEquals(new Port(1738), portCache.getCache());
    }

    /**
     * Tests for the isNullAllowed method.
     */
    @Test
    void testIsNullAllowed() {
        Cache<?> nullAllowed = new Cache<>(null, true);
        Cache<?> nullDisallowed = new Cache<>(new Object(), false);

        assertTrue(nullAllowed.isNullAllowed());
        assertFalse(nullDisallowed.isNullAllowed());
    }

    /**
     * Tests for the equals method.
     */
    @Test
    void testEquals() {
        Port port = Port.from(CommonServicePort.HTTPS);
        Cache<?> first = new Cache<>(port, false);
        Cache<?> equal = new Cache<>(port, false);
        Cache<?> notEqual = new Cache<>(port, true);

        assertEquals(first, first);
        assertEquals(first, equal);
        assertNotEquals(first, notEqual);
        assertNotEquals(first, new Object());
    }

    /**
     * Tests for the hashcode method.
     */
    @Test
    void testHashCode() {
        Port port = Port.from(CommonServicePort.HTTPS);
        Cache<?> first = new Cache<>(port, false);
        Cache<?> equal = new Cache<>(port, false);
        Cache<?> notEqual = new Cache<>(port, true);

        assertEquals(first.hashCode(), first.hashCode());
        assertEquals(first.hashCode(), equal.hashCode());
        assertNotEquals(first.hashCode(), notEqual.hashCode());
        assertEquals(-2112506629, first.hashCode());
        assertEquals(-2112506635, notEqual.hashCode());
    }

    /**
     * Tests for the toString method.
     */
    @Test
    void testToString() {
        Port port = Port.from(CommonServicePort.HTTPS);
        Cache<?> first = new Cache<>(port, false);
        Cache<Port> notEqual = new Cache<>(port, true);

        notEqual.setCachedValueUpdater(cacheRefreshSupplier);

        assertEquals("Cache{cachedValue=Port{port=443, portAvailableTimeout=PT0.4S}allowNull=false,"
                + " cachedValueUpdater=null}", first.toString());
        assertEquals("Cache{cachedValue=Port{port=443, portAvailableTimeout=PT0.4S}allowNull=true,"
                + " cachedValueUpdater=1738.port.supplier}", notEqual.toString());
    }
}
