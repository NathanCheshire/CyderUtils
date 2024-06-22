package cyderutils.network.scrapers.showmyip;

import cyderutils.exceptions.IllegalMethodException;
import cyderutils.network.scrapers.Scraper;
import cyderutils.strings.CyderStrings;
import cyderutils.utils.JsoupUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Optional;

/**
 * Utilities related to web-scraping.
 */
public final class ShowMyIpScraper implements Scraper {
    /**
     * The URL this scraper scrapes details from.
     */
    public static final String url = "https://www.showmyip.com/";

    @SuppressWarnings("SpellCheckingInspection")
    private static final String tableDataElementQuery = "table.iptab";

    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private ShowMyIpScraper() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Scrapes the showmyip.com page and returns a ShowMyIpResult object.
     *
     * @return an Optional containing the ShowMyIpResult if found, otherwise an empty Optional
     */
    public static Optional<ShowMyIpResult> scape() {
        Optional<Document> document = JsoupUtils.readDocument(url);
        if (document.isEmpty()) {
            return Optional.empty();
        }

        Element table = document.get().select(tableDataElementQuery).first();
        if (table != null) {
            return Optional.of(new ShowMyIpResult(
                    Key.IPv4.extractValue(table).orElse(""),
                    Key.IPv6.extractValue(table).orElse(""),
                    Key.COUNTRY.extractValue(table).orElse(""),
                    Key.REGION.extractValue(table).orElse(""),
                    Key.CITY.extractValue(table).orElse(""),
                    Key.ZIP.extractValue(table).orElse(""),
                    Key.TIMEZONE.extractValue(table).orElse(""),
                    Key.ISP.extractValue(table).orElse(""),
                    Key.ORGANIZATION.extractValue(table).orElse(""),
                    Key.AS_NUMBER_AND_NAME.extractValue(table).orElse(""),
                    Key.USER_AGENT.extractValue(table).orElse("")
            ));
        }

        return Optional.empty();
    }
}
