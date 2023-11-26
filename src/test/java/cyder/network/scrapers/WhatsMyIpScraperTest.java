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
        assertFalse(details.isp().isEmpty());
        assertFalse(details.hostname().isEmpty());
        assertFalse(details.city().isEmpty());
        assertFalse(details.state().isEmpty());
        assertFalse(details.country().isEmpty());

        assertTrue(CyderRegexPatterns.ipv4Pattern.matcher(details.ip()).matches());
    }
}
