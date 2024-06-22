package cyderutils.utils;

import com.google.common.base.Preconditions;
import cyderutils.constants.CyderRegexPatterns;
import cyderutils.exceptions.IllegalMethodException;
import cyderutils.strings.CyderStrings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Optional;

/**
 * Utilities related to {@link Jsoup}.
 */
public class JsoupUtils {
    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private JsoupUtils() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Reads and returns a {@link Jsoup} {@link Document} read from the provided url.
     *
     * @param url the url to read, parse, and return a Document from
     * @return the Document
     * @throws NullPointerException     if the provided url is null
     * @throws IllegalArgumentException if the provided url is empty or not a valid url
     */
    public static Optional<Document> readDocument(String url) {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());
        Preconditions.checkArgument(CyderRegexPatterns.urlFormationPattern.matcher(url).matches());

        Document doc = null;

        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");

            WebDriver driver = new ChromeDriver(options);
            driver.get(url);
            String pageSource = driver.getPageSource();
            doc = Jsoup.parse(pageSource);
        } catch (Exception ignored) {}

        return Optional.ofNullable(doc);
    }
}
