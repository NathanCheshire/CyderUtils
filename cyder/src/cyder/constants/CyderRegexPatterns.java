package cyder.constants;

import cyder.exceptions.IllegalMethodException;

import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class CyderRegexPatterns {
    /**
     * Pattern used to validate an ipv4 address
     */
    public static final Pattern ipv4Pattern = Pattern.compile("\\s*[0-9]{1,3}(\\s*|\\.[0-9]{1,3}\\s*" +
            "|\\.[0-9]{1,3}\\.[0-9]{1,3}\\s*|\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\s*)");

    /**
     * Pattern used to identify 1 or more numbers
     */
    public static final Pattern numberPattern = Pattern.compile("[0-9]+");

    /**
     * Pattern to identify 0 or more letters
     */
    public static final String lettersPattern = "[A-Za-z]*";

    /**
     * Pattern to identify common phone number patterns
     */
    public static final Pattern phoneNumberPattern =
            Pattern.compile("\\s*[0-9]?\\s*[\\-]?\\s*[(]?\\s*[0-9]{0,3}\\s*[)]?\\s*" +
                    "[\\-]?\\s*[0-9]{3}\\s*[\\-]?\\s*[0-9]{4}\\s*");

    /**
     * Pattern to identify common phone number patterns with an
     * extended region code (+1 is american and the numbers range
     * from 1-9 but if they were extended, this pattern would match those new numbers)
     */
    public static final Pattern phoneNumberRegionCodeExtendedPattern =
            Pattern.compile("\\s*[0-9]{0,2}\\s*[\\-]?\\s*[(]?\\s*[0-9]{0,3}\\s*" +
                    "[)]?\\s*[\\-]?\\s*[0-9]{3}\\s*[\\-]?\\s*[0-9]{4}\\s*");

    /**
     * Pattern to match zero or more numbers and letters
     */
    public static final Pattern numbersAndLettersPattern = Pattern.compile("\\s*[/]{2}.*" +
            "|\\s*[/][*].*|\\s*[*].*|\\s*.*[*][/]\\s*");

    /**
     * Regex pattern to determine if a line is a comment.
     */
    public static final Pattern commentPattern = Pattern.compile(
            "\\s*[/]{2}.*|\\s*[/][*].*|\\s*[*].*|\\s*.*[*][/]\\s*");

    /**
     * Regex of all invalid characters for a filename on Windows.
     */
    public static final Pattern windowsInvalidFilenameChars = Pattern.compile("[*?|/\":<>\\\\']+");

    /**
     * Regex for rgb color or hex color such as 00FF00 or 0,255,0.
     */
    public static final Pattern rgbOrHex = Pattern.compile("((\\d{1,3})|(\\d{1,3},)|(\\d{1,3},\\d{1,3})|(\\d{1,3}," +
            "\\d{1,3},)|(\\d{1,3},\\d{1,3},\\d{1,3}))|([0-9A-Fa-f]{0,6})");

    /**
     * Regex for a hex color value.
     */
    public static final Pattern hexPattern = Pattern.compile("[0-9A-Fa-f]{0,6}");

    /**
     * Regex for a rgb color value.
     */
    public static final Pattern rgbPattern =
            Pattern.compile("((\\d{1,3})|(\\d{1,3},)|(\\d{1,3},\\d{1,3})|(\\d{1,3},\\d{1,3},)" +
                    "|(\\d{1,3},\\d{1,3},\\d{1,3}))");

    /**
     * The pattern for matching carriage returns.
     */
    public static final Pattern newLinePattern = Pattern.compile("\\R");

    /**
     * Prevent illegal class instantiation.
     */
    private CyderRegexPatterns() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The pattern used to grab the youtube-dl progress from the process.
     */
    public static final Pattern updatePattern = Pattern.compile(
            "\\s*\\[download]\\s*([0-9]{1,3}.[0-9]%)\\s*of\\s*([0-9A-Za-z.]+)" +
                    "\\s*at\\s*([0-9A-Za-z./]+)\\s*ETA\\s*([0-9:]+)");

    /**
     * The pattern used to scrape the youtube uuids returned from the youtube api v3 instead
     * of using JSON serialization via GSON.
     */
    public static final Pattern youtubeApiV3UuidPattern = Pattern.compile(
            """
                    "resourceId":\\s*\\{\\s*
                    \\s*"kind":\\s*"youtube#video",\\s*
                    \\s*"videoId":\\s*"(.*)"\\s*
                    \\s*},""");

    /**
     * The pattern sued to webscrape the isp from a google search.
     */
    public static final Pattern whereAmIPattern = Pattern.compile("^\\s*<p class=\"isp\">(.*)</p>\\s*$");

    /**
     * The pattern used to extract the uuid from a youtube video.
     */
    public static final Pattern extractYoutubeUuidPattern
            = Pattern.compile("(?<=youtu.be/|watch\\?v=|/videos/|embed/)[^#&?]*");

    /**
     * The pattern used to validate whether a Url is constructed properly.
     */
    public static final Pattern urlFormationPattern = Pattern.compile("\\b(?:(https?|ftp|file)://" +
                    "|www\\.)?[-A-Z0-9+&#/%?=~_|$!:,.;]*[A-Z0-9+&@#/%=~_|$]\\." +
                    "[-A-Z0-9+&@#/%?=~_|$!:,.;]*[A-Z0-9+&@#/%=~_|$]",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    /**
     * The pattern used to detect one or more whitespace characters.
     */
    public static final Pattern whiteSpace = Pattern.compile("\\s+");

    /**
     * The regex used to match 1-n whitespace.
     */
    public static final String whiteSpaceRegex = "\\s+";
}
