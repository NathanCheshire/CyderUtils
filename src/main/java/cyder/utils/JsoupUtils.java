package cyder.utils;

import com.google.common.base.Preconditions;
import cyder.constants.CyderRegexPatterns;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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

        try {
            return Optional.of(Jsoup.connect(url).get());
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }
}
