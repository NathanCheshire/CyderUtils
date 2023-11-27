package cyder.network.scrapers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link WhatsMyIpScraperResult} class.
 */
class WhatsMyIpScraperResultTest {
    /**
     * Constructs a new instance of this class for testing purposes.
     */
    WhatsMyIpScraperResultTest() {}

    /**
     * Tests for construction.
     */
    @Test
    void testConstruction() {
        assertThrows(NullPointerException.class, () -> new WhatsMyIpScraperResult(
                null, null, null,
                null, null, null));
        assertThrows(NullPointerException.class, () -> new WhatsMyIpScraperResult(
                "", null, null,
                null, null, null));
        assertThrows(NullPointerException.class, () -> new WhatsMyIpScraperResult(
                "", "", null,
                null, null, null));
        assertThrows(NullPointerException.class, () -> new WhatsMyIpScraperResult(
                "", "", "",
                null, null, null));
        assertThrows(NullPointerException.class, () -> new WhatsMyIpScraperResult(
                "", "", "",
                "", null, null));
        assertThrows(NullPointerException.class, () -> new WhatsMyIpScraperResult(
                "", "", "",
                "", "", null));
        assertThrows(IllegalArgumentException.class, () -> new WhatsMyIpScraperResult(
                "", "", "",
                "", "", ""));
        assertThrows(IllegalArgumentException.class, () -> new WhatsMyIpScraperResult(
                "a", "", "",
                "", "", ""));
        assertThrows(IllegalArgumentException.class, () -> new WhatsMyIpScraperResult(
                "a", "b", "",
                "", "", ""));
        assertThrows(IllegalArgumentException.class, () -> new WhatsMyIpScraperResult(
                "a", "b", "c",
                "", "", ""));
        assertThrows(IllegalArgumentException.class, () -> new WhatsMyIpScraperResult(
                "a", "b", "c",
                "d", "", ""));
        assertThrows(IllegalArgumentException.class, () -> new WhatsMyIpScraperResult(
                "a", "b", "c",
                "d", "e", ""));

        assertDoesNotThrow(() -> new WhatsMyIpScraperResult(
                "a", "b", "c",
                "d", "e", "e"));
    }

    /**
     * Tests for accessors.
     */
    @Test
    void testGetters() {
        WhatsMyIpScraperResult result = new WhatsMyIpScraperResult(
                "isp", "hostname", "ip",
                "city", "state", "country");
        assertEquals("isp", result.getIsp());
        assertEquals("hostname", result.getHostname());
        assertEquals("ip", result.getIp());
        assertEquals("city", result.getCity());
        assertEquals("state", result.getState());
        assertEquals("country", result.getCountry());
    }

    /**
     * Tests for the sharable report.
     */
    @Test
    void testGetSharableReport() {
        WhatsMyIpScraperResult result = new WhatsMyIpScraperResult(
                "isp", "hostname", "ip",
                "city", "state", "country");
        assertEquals("https://www.whatismyisp.com/ip/" + result.getIp(), result.getSharableReport());
    }

    /**
     * Tests the is valid ipv4 method.
     */
    @Test
    void testIsValidIpv4() {
        WhatsMyIpScraperResult invalid = new WhatsMyIpScraperResult(
                "isp", "hostname", "ip",
                "city", "state", "country");
        assertFalse(invalid.isIpValidIpv4());
        WhatsMyIpScraperResult valid = new WhatsMyIpScraperResult(
                "isp", "hostname", "255.255.255.255",
                "city", "state", "country");
        assertTrue(valid.isIpValidIpv4());
    }

    /**
     * Tests for the equals method.
     */
    @Test
    void testEquals() {
        WhatsMyIpScraperResult result = new WhatsMyIpScraperResult(
                "isp", "hostname", "ip",
                "city", "state", "country");
        WhatsMyIpScraperResult equal = new WhatsMyIpScraperResult(
                "isp", "hostname", "ip",
                "city", "state", "country");
        WhatsMyIpScraperResult notEqual = new WhatsMyIpScraperResult(
                "isp", "hostname", "ip",
                "city", "state", "other country");

        assertEquals(result, result);
        assertEquals(result, equal);
        assertNotEquals(result, notEqual);
        assertNotEquals(result, new Object());
    }

    /**
     * Tests for the to string method.
     */
    @Test
    void testToString() {
        WhatsMyIpScraperResult result = new WhatsMyIpScraperResult(
                "Charter", "something-weird.2.718", "255.255.255.255",
                "New Orleans", "Louisiana", "France");
        assertEquals("WhatsMyIpScraperResult{isp=\"Charter\", hostname=\"something-weird.2.718\","
                + " ip=\"255.255.255.255\", city=\"New Orleans\", state=\"Louisiana\","
                + " country=\"France\"}", result.toString());
    }

    /**
     * Tests for the hash code method.
     */
    @Test
    void testHashCode() {
        WhatsMyIpScraperResult result = new WhatsMyIpScraperResult(
                "isp", "hostname", "ip",
                "city", "state", "country");
        WhatsMyIpScraperResult equal = new WhatsMyIpScraperResult(
                "isp", "hostname", "ip",
                "city", "state", "country");
        WhatsMyIpScraperResult notEqual = new WhatsMyIpScraperResult(
                "isp", "hostname", "ip",
                "city", "state", "other country");

        assertEquals(-543788234, result.hashCode());
        assertEquals(-543788234, equal.hashCode());
        assertEquals(-1486455258, notEqual.hashCode());
        assertEquals(result.hashCode(), equal.hashCode());
        assertNotEquals(result.hashCode(), notEqual.hashCode());
        assertNotEquals(result.hashCode(), new Object().hashCode());
    }
}
