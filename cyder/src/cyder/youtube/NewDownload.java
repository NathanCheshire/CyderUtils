package cyder.youtube;

import com.google.common.base.Preconditions;
import cyder.console.Console;
import cyder.constants.CyderRegexPatterns;
import cyder.enums.Dynamic;
import cyder.enums.Extension;
import cyder.exceptions.FatalException;
import cyder.exceptions.YoutubeException;
import cyder.handlers.input.BaseInputHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.network.NetworkUtil;
import cyder.strings.StringUtil;
import cyder.user.UserFile;
import cyder.utils.ImageUtil;
import cyder.utils.OsUtil;
import cyder.utils.SecurityUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static cyder.youtube.YoutubeConstants.YOUTUBE_VIDEO_URL_TITLE_SUFFIX;

/**
 * An object to download audio and thumbnails from YouTube.
 * An instance of this class can represent a video/playlist of videos.
 */
public class NewDownload {
    private static final int DIMENSION_TO_BE_DETERMINED = -1;

    /**
     * The string which could be a link, id, or query.
     */
    private String providedDownloadString;

    /**
     * The name to save the audio download as.
     */
    private String audioDownloadName;

    /**
     * The name to save the thumbnail download as.
     */
    private String thumbnailDownloadName;

    /**
     * The handler to use for printing updates and ui elements.
     */
    private BaseInputHandler printOutputHandler;

    /**
     * The width to crop the thumbnail to.
     */
    private int requestedThumbnailWidth = DIMENSION_TO_BE_DETERMINED;

    /**
     * The height to crop the thumbnail to.
     */
    private int requestedThumbnailHeight = DIMENSION_TO_BE_DETERMINED;

    /**
     * Constructs a new YoutubeDownload object.
     */
    public NewDownload() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Sets the download type of this download to a video link.
     *
     * @param videoLink the video link
     */
    public void setVideoLink(String videoLink) {
        Preconditions.checkNotNull(videoLink);
        Preconditions.checkArgument(!videoLink.isEmpty());
        Preconditions.checkArgument(!NetworkUtil.readUrl(videoLink).isEmpty());

        this.providedDownloadString = videoLink;
    }

    /**
     * Sets the download type of this download to a video id.
     *
     * @param videoId the video id
     */
    public void setVideoId(String videoId) {
        Preconditions.checkNotNull(videoId);
        Preconditions.checkArgument(!videoId.isEmpty());
        Preconditions.checkArgument(videoId.length() == YoutubeConstants.UUID_LENGTH);

        String videoLink = YoutubeUtil.buildVideoUrl(videoId);
        Preconditions.checkArgument(!NetworkUtil.readUrl(videoLink).isEmpty());

        this.providedDownloadString = videoLink;
    }

    /**
     * Sets the download type of this download to a playlist id.
     *
     * @param playlistId the playlist id
     */
    public void setPlaylistId(String playlistId) {
        Preconditions.checkNotNull(playlistId);
        Preconditions.checkArgument(!playlistId.isEmpty());

        String videoLink = YoutubeConstants.YOUTUBE_PLAYLIST_HEADER + playlistId;
        Preconditions.checkArgument(!NetworkUtil.readUrl(videoLink).isEmpty());

        this.providedDownloadString = videoLink;
    }

    /**
     * Sets the download type of this download to a query.
     *
     * @param query the video link
     */
    public void setVideoQuery(String query) {
        Preconditions.checkNotNull(query);
        Preconditions.checkArgument(!query.isEmpty());

        String firstUuid = YoutubeUtil.getFirstUuid(query);
        if (firstUuid == null || firstUuid.length() != YoutubeConstants.UUID_LENGTH) {
            throw new IllegalArgumentException("Could not find video for query: " + query);
        }

        this.providedDownloadString = YoutubeUtil.buildVideoUrl(firstUuid);
    }

    /**
     * Sets the provided name as the name to save the .mp3 and .png audio and thumbnail downloads as.
     *
     * @param downloadNames the name to save the downloads as
     */
    public void setDownloadNames(String downloadNames) {
        setAudioDownloadName(downloadNames);
        setThumbnailDownloadName(downloadNames);
    }

    /**
     * Sets the name to save the .mp3 download as.
     *
     * @param audioDownloadName the name to save the .mp3 download as
     */
    public void setAudioDownloadName(String audioDownloadName) {
        Preconditions.checkNotNull(audioDownloadName);
        Preconditions.checkArgument(!audioDownloadName.isEmpty());
        Preconditions.checkArgument(OsUtil.isValidFilename(audioDownloadName));

        this.audioDownloadName = audioDownloadName;
    }

    /**
     * Sets the name to save the .png download as.
     *
     * @param thumbnailDownloadName the name to save the .png download as
     */
    public void setThumbnailDownloadName(String thumbnailDownloadName) {
        Preconditions.checkNotNull(thumbnailDownloadName);
        Preconditions.checkArgument(!thumbnailDownloadName.isEmpty());
        Preconditions.checkArgument(OsUtil.isValidFilename(thumbnailDownloadName));

        this.thumbnailDownloadName = thumbnailDownloadName;
    }

    /**
     * Sets the handler to use for printing updates and ui elements to the Console's.
     */
    public void setPrintOutputToConsole() {
        setPrintOutputHandler(Console.INSTANCE.getInputHandler());
    }

    /**
     * Sets the handler to use for printing updates and ui elements.
     *
     * @param inputHandler the handler to use for printing updates and ui elements
     */
    public void setPrintOutputHandler(BaseInputHandler inputHandler) {
        Preconditions.checkNotNull(inputHandler);

        this.printOutputHandler = inputHandler;
    }

    /**
     * Removes the handler to use for printing updates and ui elements.
     */
    public void removePrintOutputHandler() {
        this.printOutputHandler = null;
    }

    /**
     * Sets the request width to download the thumbnail.
     *
     * @param requestedThumbnailWidth the request width to download the thumbnail
     */
    public void setRequestedThumbnailWidth(int requestedThumbnailWidth) {
        this.requestedThumbnailWidth = requestedThumbnailWidth;
    }

    /**
     * Sets the request height to download the thumbnail.
     *
     * @param requestedThumbnailHeight the request height to download the thumbnail
     */
    public void setRequestedThumbnailHeight(int requestedThumbnailHeight) {
        this.requestedThumbnailHeight = requestedThumbnailHeight;
    }

    /**
     * Sets the requested thumbnail dimensions, that of width and height, to the provided integer value.
     * When the thumbnail is downloaded, the resulting image will have equal width and height.
     * If a dimensional length exceeds that of the original thumbnail, the thumbnail's maximum length is used
     * for both the width and height.
     *
     * @param sideLength the requested image side length
     */
    public void setThumbnailLength(int sideLength) {
        Preconditions.checkArgument(sideLength > 0);

        this.requestedThumbnailWidth = sideLength;
        this.requestedThumbnailHeight = sideLength;
    }

    /**
     * Starts the download of the audio and thumbnail file(s).
     */
    public void downloadAudioAndThumbnail() {
        if (audioDownloadName == null && thumbnailDownloadName == null) {
            initializeAudioAndThumbnailDownloadNames();
        } else {
            if (audioDownloadName == null) {
                initializeAudioDownloadNames();
            }
            if (thumbnailDownloadName == null) {
                initializeThumbnailDownloadName();
            }
        }

        downloadAudio();
        downloadThumbnail();
    }

    /**
     * Starts the download of the audio file(s).
     */
    public void downloadAudio() {
        if (audioDownloadName == null) {
            initializeAudioDownloadNames();
        }

        // todo
    }

    /**
     * Starts the download of the thumbnail file(s).
     *
     * @throws YoutubeException if an exception occurs when attempting to download/save the thumbnail image file
     */
    public void downloadThumbnail() throws YoutubeException {
        if (thumbnailDownloadName == null) {
            initializeThumbnailDownloadName();
        }

        String uuid = YoutubeUtil.extractUuid(providedDownloadString);

        Optional<BufferedImage> optionalThumbnail = YoutubeUtil.getMaxResolutionThumbnail(uuid);
        BufferedImage thumbnailImage = optionalThumbnail.orElseThrow(
                () -> new FatalException("Could not get max resolution or standard resolution"
                        + " thumbnail for provided download string: " + providedDownloadString));

        int width = thumbnailImage.getWidth();
        int height = thumbnailImage.getHeight();

        int cropOffsetX = 0;
        int cropOffsetY = 0;

        if (requestedThumbnailWidth != DIMENSION_TO_BE_DETERMINED && width > requestedThumbnailWidth) {
            cropOffsetX = (width - requestedThumbnailWidth) / 2;
        }

        if (requestedThumbnailHeight != DIMENSION_TO_BE_DETERMINED && height > requestedThumbnailHeight) {
            cropOffsetY = (height - requestedThumbnailHeight) / 2;
        }

        if (cropOffsetX != 0 || cropOffsetY != 0) {
            thumbnailImage = ImageUtil.cropImage(thumbnailImage, cropOffsetX, cropOffsetY,
                    requestedThumbnailWidth, requestedThumbnailHeight);
        }

        File saveFile = Dynamic.buildDynamic(Dynamic.USERS.getFileName(), Console.INSTANCE.getUuid(),
                UserFile.MUSIC.getName(), UserFile.ALBUM_ART, thumbnailDownloadName + Extension.PNG.getExtension());

        try {
            if (!ImageIO.write(thumbnailImage, Extension.PNG.getExtensionWithoutPeriod(), saveFile)) {
                throw new IOException("Failed to write album art to file: " + saveFile);
            }
        } catch (IOException e) {
            throw new YoutubeException(e.getMessage());
        }
    }

    /**
     * Initializes the name(s) of the audio and thumbnail download(s).
     */
    private void initializeAudioAndThumbnailDownloadNames() {
        String safeDownloadName = getSafeDownloadName(providedDownloadString);

        audioDownloadName = safeDownloadName;
        thumbnailDownloadName = safeDownloadName;
    }

    /**
     * Initializes the name(s) of the audio download(s).
     */
    private void initializeAudioDownloadNames() {
        audioDownloadName = getSafeDownloadName(providedDownloadString);
    }

    /**
     * Initializes the name(s) of the thumbnail download(s).
     */
    private void initializeThumbnailDownloadName() {
        thumbnailDownloadName = getSafeDownloadName(providedDownloadString);
    }

    /**
     * Returns a safe filename to save the audio/thumbnail downloaded from
     * the provided youtube url as on the host computer.
     *
     * @param youtubeUrl the url of the video/thumbnail to download
     * @return a safe filename to save the content from the url as when downloading
     */
    private static String getSafeDownloadName(String youtubeUrl) {
        Preconditions.checkNotNull(youtubeUrl);
        Preconditions.checkArgument(!youtubeUrl.isEmpty());

        String urlTitle = NetworkUtil.getUrlTitle(youtubeUrl).orElse("Unknown_title");

        String safeName = StringUtil.removeNonAscii(urlTitle)
                .replace(YOUTUBE_VIDEO_URL_TITLE_SUFFIX, "")
                .replaceAll(CyderRegexPatterns.windowsInvalidFilenameChars.pattern(), "").trim();

        while (safeName.endsWith(".")) {
            safeName = StringUtil.removeLastChar(safeName);
        }

        if (safeName.isEmpty()) {
            safeName = SecurityUtil.generateUuid();
        }

        return safeName;
    }
}
