package cyder.network.scrapers;

import cyder.constants.CyderRegexPatterns;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link WhatsMyIpScraper}.
 */
class WhatsMyIpScraperTest {
    /**
     * Constructs a new instance of this class for testing purposes.
     */
    WhatsMyIpScraperTest() {}

    /**
     * Tests for the getIspAndNetworkDetails method.
     */
    @Test
    void testGetIspAndNetworkDetails() {
        assertDoesNotThrow(WhatsMyIpScraper::getIspAndNetworkDetails);
        WhatsMyIpScraperResult details = WhatsMyIpScraper.getIspAndNetworkDetails();
        assertFalse(details.getIsp().isEmpty());
        assertFalse(details.getHostname().isEmpty());
        assertFalse(details.getCity().isEmpty());
        assertFalse(details.getState().isEmpty());
        assertFalse(details.getCountry().isEmpty());

        assertTrue(CyderRegexPatterns.ipv4Pattern.matcher(details.getIp()).matches());
        assertEquals("https://www.whatismyisp.com/ip/" + details.getIp(), details.getSharableReport());
        System.out.println("WhatsMyIpScraperTest.testGetIspAndNetworkDetails: " + details);
    }
}
