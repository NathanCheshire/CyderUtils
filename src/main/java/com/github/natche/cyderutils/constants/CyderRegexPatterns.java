package com.github.natche.cyderutils.constants;

import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.strings.CyderStrings;

import java.util.regex.Pattern;

/** Pre-compiled regex patterns for use throughout Cyder. */
// todo this should be an enum really
public final class CyderRegexPatterns {
    /** Pattern used to validate an ipv4 address. */
    public static final Pattern ipv4Pattern =
            Pattern.compile("\\s*[0-9]{1,3}(\\s*|\\.[0-9]{1,3}\\s*" +
                    "|\\.[0-9]{1,3}\\.[0-9]{1,3}\\s*|\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\s*)");

    /** Pattern used to identify one or more numbers. */
    public static final Pattern numberPattern = Pattern.compile("[0-9]+");

    /** Regex pattern to determine if a line is a comment. */
    public static final Pattern commentPattern = Pattern.compile(
            "\\s*[/]{2}.*|\\s*[/][*].*|\\s*[*].*|\\s*.*[*][/]\\s*");

    /** Regex of all invalid characters for a filename on Windows. */
    public static final Pattern windowsInvalidFilenameChars = Pattern.compile("[*?|/\":<>\\\\']+");

    /** Regex for rgb color or hex color such as 00FF00 or 0,255,0. */
    public static final Pattern rgbOrHex = Pattern.compile(
            "((\\d{1,3})|(\\d{1,3},)|(\\d{1,3},\\d{1,3})|(\\d{1,3}," +
                    "\\d{1,3},)|(\\d{1,3},\\d{1,3},\\d{1,3}))|([0-9A-Fa-f]{0,6})");

    /** Regex for a hex color value. */
    public static final Pattern hexPattern = Pattern.compile("[0-9A-Fa-f]{0,6}");

    /** Regex for a rgb color value. */
    public static final Pattern rgbPattern = Pattern.compile(
            "((\\d{1,3})|(\\d{1,3},)|(\\d{1,3},\\d{1,3})|(\\d{1,3},\\d{1,3},)|(\\d{1,3},\\d{1,3},\\d{1,3}))");

    /** The pattern used to grab the YouTube-dl progress from the process. */
    public static final Pattern updatePattern = Pattern.compile(
            "\\s*\\[download]\\s*([0-9]{1,3}.[0-9]%)\\s*of\\s*([0-9A-Za-z.]+)" +
                    "\\s*at\\s*([0-9A-Za-z./]+)\\s*ETA\\s*([0-9:]+)");

    /** The pattern used to extract the uuid from a YouTube video. */
    public static final Pattern extractYoutubeUuidPattern
            = Pattern.compile("(?<=youtu.be/|watch\\?v=|/videos/|embed/)[^#&?]*");

    /** The pattern used to validate whether a Url is constructed properly. */
    public static final Pattern urlFormationPattern = Pattern.compile(
            "\\b(?:(https?|ftp|file)://|www\\.)?[-A-Z0-9+&#/%?=~_|$!:,.;]*[A-Z0-9+&@#/%=~_|$]\\."
                    + "[-A-Z0-9+&@#/%?=~_|$!:,.;]*[A-Z0-9+&@#/%=~_|$]",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    /** The regex used to match 1-n whitespace. */
    public static final String whiteSpaceRegex = "\\s+";

    /** The regex for filtering out any non-numbers. */
    public static final String nonNumberRegex = "[^0-9]";

    /** The regex for filtering out non-numbers and periods. */
    public static final String nonNumberAndPeriodRegex = "[^0-9.]";

    /** A regex for targeting two or more spaces. */
    public static final String multipleWhiteSpaceRegex = "\\s{2,}";

    /** A regex for targeting characters not acceptable in a url. */
    public static final String illegalUrlCharsRegex = "[^0-9A-Za-z\\-._~%]+";

    /** The regex for targeting the 26 english characters, both upper and lower case. */
    public static final String englishLettersRegex = "[A-Za-z]+";

    /** The pattern YouTube ids comply to. */
    public static final Pattern youTubeUuidPattern = Pattern.compile("[a-zA-Z0-9_\\-]{11}");

    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private CyderRegexPatterns() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }
}
