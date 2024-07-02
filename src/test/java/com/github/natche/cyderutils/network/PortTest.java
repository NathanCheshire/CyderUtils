package com.github.natche.cyderutils.network;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for the {@link Port}. */
class PortTest {
    /** Constructs a new instance of this class for testing purposes. */
    PortTest() {}

    /** Tests for construction of Ports. */
    @Test
    void testConstruction() {
        assertThrows(IllegalArgumentException.class, () -> new Port(-1024));
        assertThrows(IllegalArgumentException.class, () -> new Port(-1));

        assertDoesNotThrow(() -> new Port(0));
        assertDoesNotThrow(() -> new Port(1));
        assertDoesNotThrow(() -> new Port(1024));
        assertDoesNotThrow(() -> new Port(1025));
        assertDoesNotThrow(() -> new Port(65534));
        assertDoesNotThrow(() -> new Port(65535));

        assertThrows(IllegalArgumentException.class, () -> new Port(65536));
    }

    /** Tests for construction from {@link CommonServicePort}s. */
    @Test
    void testConstructionFrom() {
        assertThrows(NullPointerException.class, () -> Port.from(null));
        assertDoesNotThrow(() -> Port.from(CommonServicePort.HTTP));
    }

    /** Tests for the accessors and mutators. */
    @Test
    void testAccessorsMutators() {
        Port port = new Port(143);
        assertEquals(143, port.getPort());
        assertEquals(Duration.ofMillis(400), port.getPortAvailableTimeout());

        port.setPortAvailableTimeout(Duration.ofSeconds(5));
        assertEquals(Duration.ofSeconds(5), port.getPortAvailableTimeout());
    }

    /** Tests for the isAvailable method. */
    @Test
    void testIsAvailable() {
        Port someRandomPort = new Port(25000);
        assertDoesNotThrow(someRandomPort::isAvailable);
        assertTrue(someRandomPort.isAvailable());

        someRandomPort.setPortAvailableTimeout(Duration.ofSeconds(5));
        assertTrue(someRandomPort.isAvailable());
    }

    /** Tests for the toString method. */
    @Test
    void testToString() {
        Port port = new Port(25);
        Port otherPort = new Port(443);

        Port fromPort = Port.from(CommonServicePort.DNS);
        fromPort.setPortAvailableTimeout(Duration.ofSeconds(5));

        assertEquals("Port{port=25, portAvailableTimeout=PT0.4S}", port.toString());
        assertEquals("Port{port=443, portAvailableTimeout=PT0.4S}", otherPort.toString());
        assertEquals("Port{port=53, portAvailableTimeout=PT5S}", fromPort.toString());
    }

    /** Tests for the equals method. */
    @Test
    void testEquals() {
        Port port = new Port(25);
        Port equalPort = new Port(25);
        Port notEqual = new Port(100);

        assertEquals(port, port);
        assertEquals(port, equalPort);
        assertNotEquals(port, notEqual);
        assertNotEquals(port, new Object());
    }

    /** Tests for the hash code method. */
    @Test
    void testHashCode() {
        Port port = new Port(25);
        Port equalPort = new Port(25);
        Port notEqual = new Port(100);

        assertEquals(-1074835705, port.hashCode());
        assertEquals(-1074835705, equalPort.hashCode());
        assertEquals(-1074833380, notEqual.hashCode());
        assertEquals(port.hashCode(), equalPort.hashCode());
        assertNotEquals(port.hashCode(), notEqual.hashCode());
        assertNotEquals(port.hashCode(), new Object().hashCode());
    }
}
