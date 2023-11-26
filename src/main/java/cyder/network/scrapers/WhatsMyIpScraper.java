package cyder.network.scrapers;

import cyder.constants.CyderRegexPatterns;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Utilities related to web-scraping.
 */
public final class WhatsMyIpScraper {
    /**
     * The URL this scraper scrapes details from.
     */
    private static final String ispQueryUrl = "https://www.whatismyisp.com/";

    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private WhatsMyIpScraper() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    // todo maybe clean up with some helper methods

    /**
     * Returns information about this user's isp, their ip, location, city, state/region, and country.
     *
     * @return information about this user's isp, their ip, location, city, state/region, and country
     */
    public static WhatsMyIpScraperResult getIspAndNetworkDetails() {
        Document locationDocument;
        try {
            locationDocument = Jsoup.connect(ispQueryUrl).get();
        } catch (IOException e) {
            throw new WhatsMyIpScraperException("JSoup failed to get document from "
                    + ispQueryUrl + ", error: " + e.getMessage());
        }

        String isp = locationDocument.getElementsByClass(HtmlClassName.ISP.getHtmlClassName()).text();
        Elements cityStateCountryElements = locationDocument.getElementsByClass(
                HtmlClassName.CITY_STATE_COUNTRY.getHtmlClassName());
        if (cityStateCountryElements.size() < 1) {
            throw new WhatsMyIpScraperException("Could not parse document for city state country element");
        }
        Element firstCityStateCountryElement = cityStateCountryElements.get(0);
        Elements cityStateCountryElementAllElements = firstCityStateCountryElement.getAllElements();
        if (cityStateCountryElementAllElements.size() < HtmlElementIndex.COUNTRY.getIndex() - 1) {
            throw new WhatsMyIpScraperException("Not enough city state country sub elements");
        }
        String city = cityStateCountryElementAllElements.get(HtmlElementIndex.CITY.getIndex()).text();
        String state = cityStateCountryElementAllElements.get(HtmlElementIndex.STATE.getIndex()).text();
        String country = cityStateCountryElementAllElements.get(HtmlElementIndex.COUNTRY.getIndex()).text();

        Elements hostnameElements = locationDocument.getElementsByClass(HtmlClassName.HOSTNAME.getHtmlClassName());
        if (hostnameElements.size() < HtmlElementIndex.HOSTNAME.getIndex()) {
            throw new WhatsMyIpScraperException("Not enough hostname elements");
        }

        String rawHostname = hostnameElements.get(HtmlElementIndex.HOSTNAME.getIndex()).text();
        String rawClassResult = rawHostname.substring(rawHostname.indexOf("'") + 1);
        String hostname = rawClassResult.substring(0, rawClassResult.indexOf("'"));

        Elements ipElements = locationDocument.getElementsByClass(HtmlClassName.IP.getHtmlClassName());
        if (ipElements.isEmpty()) {
            throw new WhatsMyIpScraperException("Not enough ip elements");
        }
        Element ipElement = ipElements.get(HtmlElementIndex.IP.getIndex());
        String ip = ipElement.text().replaceAll(CyderRegexPatterns.nonNumberAndPeriodRegex, "");

        return new WhatsMyIpScraperResult(isp, hostname, ip, city, state, country);
    }
}
