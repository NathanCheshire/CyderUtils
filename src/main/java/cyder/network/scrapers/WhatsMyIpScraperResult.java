package cyder.network.scrapers;

/**
 * A record used to store the data scraped by {@link WhatsMyIpScraper#getIspAndNetworkDetails()}.
 */
public record WhatsMyIpScraperResult(String isp,
                                     String hostname,
                                     String ip,
                                     String city,
                                     String state,
                                     String country) {}
