package com.github.natche.cyderutils.youtube;

import com.google.common.collect.Range;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.strings.CyderStrings;

import java.util.regex.Pattern;

/** Constants used throughout YouTube utility classes. */
public final class YouTubeConstants {
    /** The length of YouTube UUIDs. */
    public static final int UUID_LENGTH = 11;

    /** The YouTube query base url. */
    static final String YOUTUBE_QUERY_BASE = "https://www.youtube.com/results?search_query=";

    /** The YouTube video base url. */
    static final String YOUTUBE_VIDEO_BASE = "https://www.youtube.com/watch?v=";

    /** A link to set environment variables for Windows. */
    static final String environmentVariables
            = "https://www.architectryan.com/2018/03/17/add-to-the-path-on-windows-10/";

    /** The header that all YouTube playlists start with. */
    static final String YOUTUBE_PLAYLIST_HEADER = "https://www.youtube.com/playlist?list=";

    /** The header used for obtaining a YouTube video's highest resolution thumbnail. */
    static final String YOUTUBE_THUMBNAIL_BASE = "https://img.youtube.com/vi/";

    /** The base for YouTube api v3 search queries. */
    static final String YOUTUBE_API_V3_SEARCH_BASE = "https://www.googleapis.com/youtube/v3/search?part=snippet";

    /** The maximum number of chars that can be used for a filename from a YouTube video's title. */
    static final int MAX_THUMBNAIL_CHARS = 20;

    /** The range of valid values for the number of results a YouTube api 3 search query. */
    static final Range<Integer> SEARCH_QUERY_RESULTS_RANGE = Range.closed(1, MAX_THUMBNAIL_CHARS);

    /** The key used for a max resolution thumbnail. */
    static final String MAX_RES_DEFAULT = "maxresdefault.jpg";

    /** The key used for a standard definition thumbnail. */
    static final String SD_DEFAULT = "sddefault.jpg";

    /** The pattern to identify a valid YouTube UUID. */
    static final Pattern UUID_PATTERN = Pattern.compile("[-_A-Za-z0-9]{" + UUID_LENGTH + "}");

    /** The delay between download button updates. */
    public static final int DOWNLOAD_UPDATE_DELAY = 1000;

    /** The max results parameter for searching YouTube. */
    static final String MAX_RESULTS_PARAMETER = "&maxResults=";

    /** The value to indicate a download has not yet finished. */
    static final int DOWNLOAD_NOT_FINISHED = Integer.MIN_VALUE;

    /** The regular exit code for a {@link Process}. */
    static final int SUCCESSFUL_EXIT_CODE = 0;

    /** The suffix appended to all YouTube video titles in the URL of a webpage. */
    static final String YOUTUBE_VIDEO_URL_TITLE_SUFFIX = "- YouTube";

    /** The cancel text. */
    static final String CANCEL = "Cancel";

    /** The canceled text. */
    static final String CANCELED = "Canceled";

    /** The minimum value of the download progress bar. */
    static final int downloadProgressMin = 0;

    /** The maximum value of the download progress bar. */
    static final int downloadProgressMax = 10000;

    /** The text for the download button after a download has finished successfully. */
    static final String PLAY = "Play";

    /** The failed text for the button. */
    static final String FAILED = "Failed";

    /** The index of the matcher group the progress of the YouTube download lies at. */
    static final int progressIndex = 1;

    /** The index of the matcher group the size of the YouTube download lies at. */
    static final int sizeIndex = 2;

    /** The index of the matcher group the rate of the YouTube download lies at. */
    static final int rateIndex = 3;

    /** The index of the matcher group the eta of the YouTube download lies at. */
    static final int etaIndex = 4;

    /** The url query parameter. */
    static final String queryParameter = "&q=";

    /** The YouTube video type url parameter. */
    static final String videoTypeParameter = "&type=";

    /** The video type parameter for constructed YouTube search query urls. */
    static final String video = "video";

    /** The key parameter for constructed YouTube search query urls. */
    static final String keyParameter = "&key=";

    /** The unknown title string if a title cannot be extracted from a url. */
    static final String UNKNOWN_TITLE = "Unknown_title";

    /** The substring to locate in raw HTML in order to extract video UUIDs from a YouTube playlist. */
    static final String videoIdHtmlSubstring = "videoId\":\"";

    /** The character used to indicate a space in a url query. */
    static final String querySpace = "+";

    /** Suppress default constructor. */
    private YouTubeConstants() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }
}
