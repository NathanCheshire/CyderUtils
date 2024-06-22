package cyderutils.network.scrapers.showmyip;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Optional;

/**
 * Keys for extracting data from the table shown on the HTML content from showmyip.com.
 */
public enum Key {
    IPv4("Your IPv4"),
    IPv6("Your IPv6"),
    COUNTRY("Country"),
    REGION("Region"),
    CITY("City"),
    ZIP("ZIP"),
    TIMEZONE("Timezone"),
    ISP("Internet Service Provider (ISP)"),
    ORGANIZATION("Organization"),
    AS_NUMBER_AND_NAME("AS number and name"),
    USER_AGENT("User agent");

    private final String displayName;

    Key(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    private static Optional<String> extractValue(Element table, Key key) {
        Elements rows = table.select("tbody tr");
        for (Element row : rows) {
            Elements columns = row.select("td");
            if (columns.size() == 2) {
                String keyText = columns.get(0).text();
                if (key.displayName.equalsIgnoreCase(keyText)) {
                    return Optional.of(columns.get(1).text());
                }
            }
        }

        return Optional.empty();
    }

    public Optional<String> extractValue(Element table) {
        return extractValue(table, this);
    }
}
