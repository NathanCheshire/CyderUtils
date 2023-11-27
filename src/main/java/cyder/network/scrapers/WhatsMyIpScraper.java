package cyder.network.scrapers;

import cyder.constants.CyderRegexPatterns;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import cyder.utils.JsoupUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

    /**
     * Reads and returns the ISP (Internet Service Provider) from the provided document.
     *
     * @param document the Jsoup document
     * @return the ISP (Internet Service Provider) from the provided document
     */
    private static String getIsp(Document document) {
        return document.getElementsByClass(HtmlClassName.ISP.getHtmlClassName()).text();
    }

    /**
     * Reads and returns the hostname from the provided document.
     *
     * @param document the Jsoup document
     * @return the hostname from the provided document
     * @throws WhatsMyIpScraperException if the hostname elements length is less than the required length
     */
    private static String getHostname(Document document) {
        Elements hostnameElements = document.getElementsByClass(HtmlClassName.HOSTNAME.getHtmlClassName());
        int length = hostnameElements.size();
        int requiredLength = HtmlElementIndex.HOSTNAME.getIndex() - 1;
        if (length < requiredLength) {
            throw new WhatsMyIpScraperException("Not enough hostname elements,"
                    + " length: " + length + ", required length: " + requiredLength);
        }
        String rawHostname = hostnameElements.get(HtmlElementIndex.HOSTNAME.getIndex()).text();
        String rawClassResult = rawHostname.substring(rawHostname.indexOf("'") + 1);
        return rawClassResult.substring(0, rawClassResult.indexOf("'"));
    }

    /**
     * Reads and returns the ip from the provided document.
     *
     * @param document the Jsoup document
     * @return the ip from the provided document
     * @throws WhatsMyIpScraperException if the IP elements list is empty
     */
    private static String getIp(Document document) {
        Elements ipElements = document.getElementsByClass(HtmlClassName.IP.getHtmlClassName());
        if (ipElements.isEmpty()) {
            throw new WhatsMyIpScraperException("Not enough ip elements");
        }
        Element ipElement = ipElements.get(HtmlElementIndex.IP.getIndex());
        return ipElement.text().replaceAll(CyderRegexPatterns.nonNumberAndPeriodRegex, "");
    }

    /**
     * Reads and returns the city, state, and country from the provided document.
     *
     * @param document the Jsoup document
     * @return the city, state, and country from the provided document
     * @throws WhatsMyIpScraperException if the primary elements list is empty or if the first
     *                                   sub element of the primary element's length is less than required
     */
    private static Triple<String, String, String> getCityStateCountry(Document document) {
        Elements cityStateCountryElements = document.getElementsByClass(
                HtmlClassName.CITY_STATE_COUNTRY.getHtmlClassName());
        if (cityStateCountryElements.isEmpty()) {
            throw new WhatsMyIpScraperException("Could not parse document for city state country element");
        }
        Element firstCityStateCountryElement = cityStateCountryElements.get(0);
        Elements cityStateCountryElementAllElements = firstCityStateCountryElement.getAllElements();
        int size = cityStateCountryElementAllElements.size();
        if (size < HtmlElementIndex.COUNTRY.getIndex() - 1) {
            throw new WhatsMyIpScraperException("Not enough city, state, country sub elements,"
                    + " length: " + size
                    + ", required length: " + (HtmlElementIndex.COUNTRY.getIndex() - 1));
        }
        String city = cityStateCountryElementAllElements.get(HtmlElementIndex.CITY.getIndex()).text();
        String state = cityStateCountryElementAllElements.get(HtmlElementIndex.STATE.getIndex()).text();
        String country = cityStateCountryElementAllElements.get(HtmlElementIndex.COUNTRY.getIndex()).text();

        return Triple.of(city, state, country);
    }

    /**
     * Returns a new {@link WhatsMyIpScraperResult} object containing information about the user
     * such as ISP, hostname, ip, city, state, and country.
     *
     * @return a new {@link WhatsMyIpScraperResult} object
     * @throws WhatsMyIpScraperException if Jsoup fails to read a document from whatismyisp.com
     */
    public static WhatsMyIpScraperResult getIspAndNetworkDetails() {
        Document locationDocument = JsoupUtils.readDocument(ispQueryUrl).orElseThrow(() ->
                new WhatsMyIpScraperException("Jsoup failed to get document from " + ispQueryUrl + ", error: "));

        String isp = getIsp(locationDocument);
        Triple<String, String, String> cityStateCountry = getCityStateCountry(locationDocument);
        String city = cityStateCountry.getLeft();
        String state = cityStateCountry.getMiddle();
        String country = cityStateCountry.getRight();
        String hostname = getHostname(locationDocument);
        String ip = getIp(locationDocument);

        return new WhatsMyIpScraperResult(isp, hostname, ip, city, state, country);
    }
}
