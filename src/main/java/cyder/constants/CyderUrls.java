package cyder.constants;

import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;

/**
 * Urls used throughout Cyder.
 */
public final class CyderUrls {
    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private CyderUrls() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The open weather url to get weather data from.
     */
    public static final String OPEN_WEATHER_BASE = "https://api.openweathermap.org/data/2.5/weather?q=";

    /**
     * The wikipedia query base.
     */
    public static final String WIKIPEDIA_SUMMARY_BASE = "https://en.wikipedia.org/w/api.php?format=json&action=query";

    /**
     * Microsoft.com
     */
    public static final String MICROSOFT = "https://www.microsoft.com//en-us//";

    /**
     * Apple.com
     */
    public static final String APPLE = "https://www.apple.com";

    /**
     * YouTube.com
     */
    public static final String YOUTUBE = "https://www.youtube.com";

    /**
     * The pastebin link for raw text
     */
    public static final String PASTEBIN_RAW_BASE = "https://pastebin.com/raw/";

    /**
     * The url of the default background to give to newly created users provided a
     * connection is available.
     */
    public static final String DEFAULT_BACKGROUND_URL = "https://i.imgur.com/kniH8y9.png";

    /**
     * The header for individual YouTube videos without their uuid.
     */
    public static final String YOUTUBE_VIDEO_HEADER = "https://www.youtube.com/watch?v=";
}
