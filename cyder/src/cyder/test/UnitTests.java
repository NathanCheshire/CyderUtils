package cyder.test;

import cyder.constants.CyderRegexPatterns;
import cyder.file.FileUtil;
import cyder.math.AngleUtil;
import cyder.time.TimeUtil;
import cyder.utils.*;
import cyder.weather.WeatherWidget;
import cyder.youtube.YoutubeUtil;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Unit tests for common Cyder functions. This will be broken up
 * into smaller classes if Cyder expands to a true production-grade program
 * and more unit tests are added for every util function.
 */
public class UnitTests {
    /**
     * Default constructor permitted for JUnit.
     */
    @SuppressWarnings("RedundantNoArgConstructor")
    public UnitTests() {
    }

    @Test
    public void testInsertBreaks() {
        assertEquals("It's the strangest feeling,<br/>feeling this way for you.", BoundsUtil.insertBreaks(
                "It's the strangest feeling, feeling this way for you.", 2));
        assertEquals("Waka waka<br/>waka<br/>waka<br/>waka waka waka.", BoundsUtil.insertBreaks(
                "Waka waka waka waka waka waka waka.", 4));
    }

    @Test
    public void testWindBearingDirection() {
        // Cardinal directions and +/1 1, +/1 0.1

        WeatherWidget instance = WeatherWidget.getInstance();

        //east
        assertEquals(instance.getWindDirection(-1.0), "SE");
        assertEquals(instance.getWindDirection(-0.1), "SE");
        assertEquals(instance.getWindDirection(0.0), "E");
        assertEquals(instance.getWindDirection(0.1), "NE");
        assertEquals(instance.getWindDirection(1.0), "NE");

        //north
        assertEquals(instance.getWindDirection(89.0), "NE");
        assertEquals(instance.getWindDirection(89.9), "NE");
        assertEquals(instance.getWindDirection(90.0), "N");
        assertEquals(instance.getWindDirection(90.1), "NW");
        assertEquals(instance.getWindDirection(91.0), "NW");

        //west
        assertEquals(instance.getWindDirection(179.0), "NW");
        assertEquals(instance.getWindDirection(179.9), "NW");
        assertEquals(instance.getWindDirection(180.0), "W");
        assertEquals(instance.getWindDirection(180.1), "SW");
        assertEquals(instance.getWindDirection(181.0), "SW");

        //south
        assertEquals(instance.getWindDirection(269.0), "SW");
        assertEquals(instance.getWindDirection(269.9), "SW");
        assertEquals(instance.getWindDirection(270.0), "S");
        assertEquals(instance.getWindDirection(270.1), "SE");
        assertEquals(instance.getWindDirection(271.0), "SE");

        //half angles
        assertEquals(instance.getWindDirection(45.0), "NE");
        assertEquals(instance.getWindDirection(45.0 + 360.0), "NE");
        assertEquals(instance.getWindDirection(45.0 - 360.0), "NE");

        assertEquals(instance.getWindDirection(135.0), "NW");
        assertEquals(instance.getWindDirection(135.0 + 360.0), "NW");
        assertEquals(instance.getWindDirection(135.0 - 360.0), "NW");

        assertEquals(instance.getWindDirection(225.0), "SW");
        assertEquals(instance.getWindDirection(225.0 + 360.0), "SW");
        assertEquals(instance.getWindDirection(225.0 - 360.0), "SW");

        assertEquals(instance.getWindDirection(315.0), "SE");
        assertEquals(instance.getWindDirection(315.0 + 360.0), "SE");
        assertEquals(instance.getWindDirection(315.0 - 360.0), "SE");
    }

    @Test
    public void testIPv4RegexMatcher() {
        ArrayList<String> ipv4Tests = new ArrayList<>();
        ipv4Tests.add("  127.045.04.1  ");
        ipv4Tests.add("  127.045.04.1");
        ipv4Tests.add("123");
        ipv4Tests.add("123.123");
        ipv4Tests.add("123.123.123");
        ipv4Tests.add("123.123.123.123");
        ipv4Tests.add("127.045.04.1   ");
        ipv4Tests.add("0.0.0.0");
        ipv4Tests.add("045.450.330.340");
        ipv4Tests.add("045.450.330");
        ipv4Tests.add("045.450");
        ipv4Tests.add("045");

        for (String ipv4Address : ipv4Tests) {
            assertTrue(ipv4Address.matches(CyderRegexPatterns.ipv4Pattern.pattern()));
        }
    }

    @Test
    public void testPluralConversion() {
        assertEquals(StringUtil.getPlural(-1, "dog"), "dogs");
        assertEquals(StringUtil.getPlural(0, "dog"), "dogs");
        assertEquals(StringUtil.getPlural(1, "dog"), "dog");
        assertEquals(StringUtil.getPlural(2, "dog"), "dogs");

        assertEquals(StringUtil.getPlural(-1, "bus"), "buses");
        assertEquals(StringUtil.getPlural(0, "bus"), "buses");
        assertEquals(StringUtil.getPlural(1, "bus"), "bus");
        assertEquals(StringUtil.getPlural(2, "bus"), "buses");
    }

    @Test
    public void testPhoneNumberPattern() {
        ArrayList<String> phoneNumbers = new ArrayList<>();
        phoneNumbers.add("456 0112");
        phoneNumbers.add("456-0112");
        phoneNumbers.add("456 - 0112");
        phoneNumbers.add("(888) 456 0112");
        phoneNumbers.add("888 456 0112");
        phoneNumbers.add("888 - 456 0112");
        phoneNumbers.add("888 - 456 - 0112");
        phoneNumbers.add("4 (888) - 456 - 0112");
        phoneNumbers.add("4 - (888) - 456 - 0112");
        phoneNumbers.add("4 - 888 - 456 - 0112");
        phoneNumbers.add("4-888-456-0112");
        phoneNumbers.add("48884560112");

        for (String phoneNumber : phoneNumbers) {
            assertTrue(phoneNumber.matches(CyderRegexPatterns.phoneNumberPattern.pattern()));
        }
    }

    @Test
    public void testIsComment() {
        assertTrue(StatUtil.isComment("*"));
        assertTrue(StatUtil.isComment("//*"));
        assertTrue(StatUtil.isComment("*/"));
        assertTrue(StatUtil.isComment("**"));
        assertTrue(StatUtil.isComment("/* is this one */"));
        assertTrue(StatUtil.isComment("//**//**//"));
        assertTrue(StatUtil.isComment("/*/"));

        assertFalse(StatUtil.isComment("raw text"));
        assertFalse(StatUtil.isComment("tee: //haha"));

        //technically this line contains a comment but it itself is not a comment
        assertFalse(StatUtil.isComment("tee: /*haha*/ alpha"));

        assertFalse(StatUtil.isComment("tee: //haha   /*"));
        assertFalse(StatUtil.isComment("tee: //haha/*"));
        assertFalse(StatUtil.isComment("tee: /*haha"));
        assertFalse(StatUtil.isComment("\"//\""));
    }

    @Test
    public void testFileSignature() {
        assertTrue(FileUtil.fileMatchesSignature(StaticUtil.getStaticResource("CyderIcon.png"),
                FileUtil.PNG_SIGNATURE));
        assertFalse(FileUtil.fileMatchesSignature(new File(""), FileUtil.PNG_SIGNATURE));
        assertFalse(FileUtil.fileMatchesSignature(null, FileUtil.PNG_SIGNATURE));
    }

    @Test
    public void testValidateGitHubRepoCloneUrl() {
        // with protocols
        assertTrue(GitHubUtil.validateGitHubRepoCloneUrl("http://github.com/nathancheshire/cyder.git"));
        assertTrue(GitHubUtil.validateGitHubRepoCloneUrl("https://github.com/nathancheshire/cyder.git"));

        // without protocol
        assertTrue(GitHubUtil.validateGitHubRepoCloneUrl("github.com/nathancheshire/cyder.git"));

        // www prefix
        assertTrue(GitHubUtil.validateGitHubRepoCloneUrl("www.github.com/nathancheshire/cyder.git"));
        assertTrue(GitHubUtil.validateGitHubRepoCloneUrl("http://www.github.com/nathancheshire/cyder.git"));
        assertTrue(GitHubUtil.validateGitHubRepoCloneUrl("https://www.github.com/nathancheshire/cyder.git"));

        // user point
        assertFalse(GitHubUtil.validateGitHubRepoCloneUrl("http://github.com/nathancheshire"));

        // repo but not .git
        assertFalse(GitHubUtil.validateGitHubRepoCloneUrl("http://github.com/nathancheshire/cyder"));

        // bogus url
        assertFalse(GitHubUtil.validateGitHubRepoCloneUrl("http://www.youtube.com/nathancheshire/cyder.git"));

        //bogus user yet url wont know that
        assertTrue(GitHubUtil.validateGitHubRepoCloneUrl("http://github.com/loooppieloper/cyder.git"));

        // bogus repo yet url wont know that
        assertTrue(GitHubUtil.validateGitHubRepoCloneUrl("http://github.com/nathancheshire/adverbs.git"));
    }

    @Test
    public void testFormatSeconds() {
        assertEquals(TimeUtil.formatMillis(0), "0ms");
        assertEquals(TimeUtil.formatMillis(30000), "30s");
        assertEquals(TimeUtil.formatMillis(59000), "59s");
        assertEquals(TimeUtil.formatMillis(60000), "1m");
        assertEquals(TimeUtil.formatMillis(61000), "1m 1s");
        assertEquals(TimeUtil.formatMillis(120000), "2m");
        assertEquals(TimeUtil.formatMillis(121000), "2m 1s");
        assertEquals(TimeUtil.formatMillis(3599000), "59m 59s");
        assertEquals(TimeUtil.formatMillis(3600000), "1h");
        assertEquals(TimeUtil.formatMillis(3601000), "1h 1s");
        assertEquals(TimeUtil.formatMillis(3661000), "1h 1m 1s");
    }

    @Test
    public void testYouTubeVideoQueryConstruction() {
        String query = YoutubeUtil.buildYouTubeApiV3SearchQuery(1, "hello world");
        assertTrue(query.startsWith("https://www.googleapis.com/youtube/v3/search?part=snippet" +
                "&maxResults=1&q=hello%20world&type=video&key="));

        query = YoutubeUtil.buildYouTubeApiV3SearchQuery(15, "hello world");
        assertTrue(query.startsWith("https://www.googleapis.com/youtube/v3/search?part=snippet" +
                "&maxResults=15&q=hello%20world&type=video&key="));

        query = YoutubeUtil.buildYouTubeApiV3SearchQuery(20, "hello world");
        assertTrue(query.startsWith("https://www.googleapis.com/youtube/v3/search?part=snippet" +
                "&maxResults=20&q=hello%20world&type=video&key="));

        query = YoutubeUtil.buildYouTubeApiV3SearchQuery(20, "hello worldly");
        assertTrue(query.startsWith("https://www.googleapis.com/youtube/v3/search?part=snippet" +
                "&maxResults=20&q=hello%20worldly&type=video&key="));

        query = YoutubeUtil.buildYouTubeApiV3SearchQuery(20, "hello");
        assertTrue(query.startsWith("https://www.googleapis.com/youtube/v3/search?part=snippet" +
                "&maxResults=20&q=hello&type=video&key="));

        query = YoutubeUtil.buildYouTubeApiV3SearchQuery(20, "hello      world");
        assertTrue(query.startsWith("https://www.googleapis.com/youtube/v3/search?part=snippet" +
                "&maxResults=20&q=hello%20world&type=video&key="));

        query = YoutubeUtil.buildYouTubeApiV3SearchQuery(20, "helloworld");
        assertTrue(query.startsWith("https://www.googleapis.com/youtube/v3/search?part=snippet" +
                "&maxResults=20&q=helloworld&type=video&key="));

        query = YoutubeUtil.buildYouTubeApiV3SearchQuery(20, "hello ''\\() world");
        assertTrue(query.startsWith("https://www.googleapis.com/youtube/v3/search?part=snippet" +
                "&maxResults=20&q=hello%20world&type=video&key="));

        assertThrows(IllegalArgumentException.class, () -> YoutubeUtil
                .buildYouTubeApiV3SearchQuery(0, "hello world"));
        assertThrows(IllegalArgumentException.class, () -> YoutubeUtil
                .buildYouTubeApiV3SearchQuery(-1, "hello world"));
        assertThrows(IllegalArgumentException.class, () -> YoutubeUtil
                .buildYouTubeApiV3SearchQuery(21, "hello world"));
        assertThrows(IllegalArgumentException.class, () -> YoutubeUtil
                .buildYouTubeApiV3SearchQuery(10, ""));
        assertThrows(NullPointerException.class, () -> YoutubeUtil
                .buildYouTubeApiV3SearchQuery(10, null));
    }

    @Test
    public void testUnderAndOverDegreeAngleConversions() {
        // integer values
        assertEquals(AngleUtil.normalizeAngle360(-360), 0);
        assertEquals(AngleUtil.normalizeAngle360(-720), 0);
        assertEquals(AngleUtil.normalizeAngle360(-180), 180);
        assertEquals(AngleUtil.normalizeAngle360(-1), 359);

        assertEquals(AngleUtil.normalizeAngle360(0), 0);
        assertEquals(AngleUtil.normalizeAngle360(90), 90);
        assertEquals(AngleUtil.normalizeAngle360(180), 180);
        assertEquals(AngleUtil.normalizeAngle360(359), 359);
        assertEquals(AngleUtil.normalizeAngle360(360), 0);

        assertEquals(AngleUtil.normalizeAngle360(361), 1);
        assertEquals(AngleUtil.normalizeAngle360(370), 10);
        assertEquals(AngleUtil.normalizeAngle360(400), 40);

        assertEquals(AngleUtil.normalizeAngle360(720), 0);
        assertEquals(AngleUtil.normalizeAngle360(721), 1);

        // double values
        assertEquals(AngleUtil.normalizeAngle360(-360.5), 359.5, 0.0);
        assertEquals(AngleUtil.normalizeAngle360(-720.5), 359.5, 0.0);
        assertEquals(AngleUtil.normalizeAngle360(-180.5), 179.5, 0.0);
        assertEquals(AngleUtil.normalizeAngle360(-1.5), 358.5, 0.0);

        assertEquals(AngleUtil.normalizeAngle360(0.5), 0.5, 0.0);
        assertEquals(AngleUtil.normalizeAngle360(90.5), 90.5, 0.0);
        assertEquals(AngleUtil.normalizeAngle360(180.5), 180.5, 0.0);
        assertEquals(AngleUtil.normalizeAngle360(359.5), 359.5, 0.0);
        assertEquals(AngleUtil.normalizeAngle360(360.5), 0.5, 0.0);

        assertEquals(AngleUtil.normalizeAngle360(361.5), 1.5, 0.0);
        assertEquals(AngleUtil.normalizeAngle360(370.5), 10.5, 0.0);
        assertEquals(AngleUtil.normalizeAngle360(400.5), 40.5, 0.0);

        assertEquals(AngleUtil.normalizeAngle360(720.5), 0.5, 0.0);
        assertEquals(AngleUtil.normalizeAngle360(721.5), 1.5, 0.0);
    }

    @Test
    public void testLevenshteinDistance() {
        assertEquals(StringUtil.levenshteinDistance("hello", ""), 5);
        assertEquals(StringUtil.levenshteinDistance("hello", "hell"), 1);
        assertEquals(StringUtil.levenshteinDistance("hello", "hel"), 2);
        assertEquals(StringUtil.levenshteinDistance("hello", "he"), 3);
        assertEquals(StringUtil.levenshteinDistance("hello", "hello     "), 5);
        assertEquals(StringUtil.levenshteinDistance("", ""), 0);
        assertEquals(StringUtil.levenshteinDistance("nathan", "nathan"), 0);

        assertNotEquals(StringUtil.levenshteinDistance("", ""), 1);

        assertEquals(StringUtil.levenshteinDistance("distance", "levenshtein"), 10);
    }

    private static final String defineString = "definition";

    /**
     * The expected definition for the define string.
     */
    private static final String expectedDefinition = "The Act Of Defining, Or Of Making Something Definite,"
            + " Distinct, or Clear: We Need A Better Definition Of Her Responsibilities.";

    @Test
    public void testDefine() {
        Optional<String> optionalDefinition = StringUtil.getDefinition(defineString);
        assertTrue(optionalDefinition.isPresent());
        String definition = optionalDefinition.get();
        assertEquals(expectedDefinition, definition);
    }

    /**
     * The summary tolerance for wikipedia summary queries.
     */
    private static final float wikiSumTol = 0.05f;

    /**
     * The wiki sum search phrase.
     */
    private static final String wikiSumSearch = "MGK";

    /**
     * The expected wiki sum result.
     */
    private static final String wikiSumExpected = """
            Colson Baker (born April 22, 1990), known professionally as Machine Gun Kelly (MGK),
            is an American rapper, singer, musician, and actor. He is noted for his compositional
            blending of contemporary and alternative hip hop with rock.\\nMachine Gun Kelly released
            four mixtapes between 2007 and 2010 before signing with Bad Boy Records. He released his
            debut studio album, Lace Up, in 2012, which peaked at number four on the US Billboard 200
            and contained his breakout single "Wild Boy" (featuring Waka Flocka Flame). His second
            and third albums, General Admission (2015) and Bloom (2017), achieved similar commercial
            success; the latter included the single "Bad Things" (with Camila Cabello), which peaked
            at number 4 on the Billboard Hot 100. His fourth album, Hotel Diablo (2019), included
            rap rock.\\nMachine Gun Kelly released his fifth album, Tickets to My Downfall,
            in 2020; it marked a complete departure from hip hop and entry into pop punk.
            It debuted at number one on the Billboard 200, the only rock album to do so that year,
            and contained the single "My Ex's Best Friend", which reached number 20 on the Hot 100.
            He achieved similar commercial success with its follow up Mainstream Sellout (2022).Machine
            Gun Kelly had his first starring role in the romantic drama Beyond the Lights (2014),
            and since appeared in the techno-thriller Nerve (2016), the horror Bird Box (2018),
            the comedy Big Time Adolescence and portrayed Tommy Lee in the biopic The Dirt (both 2019).
            """;

    @Test
    public void testWikiSum() {
        Optional<String> wikiSumOptional = StringUtil.getWikipediaSummary(wikiSumSearch);
        assertTrue(wikiSumOptional.isPresent());

        String wikiSum = wikiSumOptional.get();
        String wikiSumSpacesRemoved = wikiSum.replaceAll(
                CyderRegexPatterns.whiteSpaceRegex, "");
        String wikiSumExpectedSpacesRemoved = wikiSumExpected.replaceAll(
                CyderRegexPatterns.whiteSpaceRegex, "");

        int lt = StringUtil.levenshteinDistance(wikiSumSpacesRemoved, wikiSumExpectedSpacesRemoved);
        float difference = (float) lt / wikiSumExpected.length();

        assertTrue(difference <= wikiSumTol);
    }

    @Test
    public void testElevationUtil() {
        Optional<Double> elevation = ElevationUtil.getElevation(new Point(0, 0), ElevationUtil.LengthUnit.FEET);
        assertTrue(elevation.isPresent());
        assertEquals(elevation.get(), -1000000.0, 0);
    }
}
