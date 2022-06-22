package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import cyder.annotations.Widget;
import cyder.constants.*;
import cyder.enums.Dynamic;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.PropLoader;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.external.audio.AudioPlayer;
import cyder.handlers.external.audio.AudioUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.ui.*;
import cyder.user.UserFile;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods related to youtube videos.
 */
public final class YoutubeUtil {
    /**
     * The maximum number of chars that can be used for a filename from a youtube video's title.
     */
    public static final int MAX_THUMBNAIL_CHARS = 20;

    /**
     * The pattern to identify a valid YouTube UUID.
     */
    public static final Pattern uuidPattern = Pattern.compile("[A-Za-z0-9_\\-]{0,11}");

    /**
     * Restrict instantiation of class.
     */
    private YoutubeUtil() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * A list of youtube videos currently being downloaded.
     */
    private static final LinkedList<YoutubeDownload> currentDownloads = new LinkedList<>();

    /**
     * Downloads the youtube video with the provided url.
     *
     * @param url the url of the video to download
     */
    public static void downloadVideo(String url) {
        if (AudioUtil.ffmpegInstalled() && AudioUtil.youtubeDlInstalled()) {
            YoutubeDownload youtubeDownload = new YoutubeDownload(url);
            currentDownloads.add(youtubeDownload);
            youtubeDownload.download();
        } else {
            noFfmpegOrYoutubedl();
        }
    }

    /**
     * Refreshes the label font of all youtube download labels.
     */
    public static void refreshAllLabels() {
        for (YoutubeDownload youtubeDownload : currentDownloads) {
            youtubeDownload.refreshLabelFont();
        }
    }

    /**
     * A utility class for downloading a single video's audio from YouTube.
     */
    public static class YoutubeDownload {
        /**
         * The url of the youtube video to download.
         */
        private final String url;

        /**
         * Suppress default constructor.
         */
        @SuppressWarnings("unused")
        private YoutubeDownload() {
            throw new IllegalMethodException("Illegal use of constructor without download url");
        }

        /**
         * Constructs a new YoutubeDownload object.
         *
         * @param url the url of the video to download
         */
        public YoutubeDownload(String url) {
            Preconditions.checkNotNull(url);

            this.url = url;
        }

        /**
         * The label this class will print and update with statistics about the download.
         */
        private JLabel printLabel;

        /**
         * Updates the download progress labels.
         */
        public void updateLabel() {
            String updateText = "<html>" + downloadableName
                    + "<br/>File size: " + downloadableFileSize
                    + "<br/>Progress: " + downloadableProgress
                    + "%<br/>Rate: " + downloadableRate
                    + "<br/>Eta: " + downloadableEta + "</html>";

            printLabel.setText(updateText);
            printLabel.revalidate();
            printLabel.repaint();
            printLabel.setHorizontalAlignment(JLabel.LEFT);
        }

        /**
         * Refreshes the label font.
         */
        public void refreshLabelFont() {
            printLabel.setFont(ConsoleFrame.INSTANCE.generateUserFont());
        }

        /**
         * The download name of this download object.
         */
        private String downloadableName;

        /**
         * The download file size of this download object.
         */
        private String downloadableFileSize;

        /**
         * The download progress of this download object.
         */
        private float downloadableProgress;

        /**
         * The download rate of this download object.
         */
        private String downloadableRate;

        /**
         * The download eta of this download object.
         */
        private String downloadableEta;

        /**
         * Whether this download has completed downloading.
         */
        private boolean downloaded;

        /**
         * Returns the download name of this download.
         *
         * @return the download name of this download
         */
        public String getDownloadableName() {
            return downloadableName;
        }

        /**
         * Returns the download file size of this download.
         *
         * @return the download file size of this download
         */
        public String getDownloadableFileSize() {
            return downloadableFileSize;
        }

        /**
         * Returns the download progress of this download.
         *
         * @return the download progress of this download
         */
        public float getDownloadableProgress() {
            return downloadableProgress;
        }

        /**
         * Returns the download rate of this download.
         *
         * @return the download rate of this download
         */
        public String getDownloadableRate() {
            return downloadableRate;
        }

        /**
         * Returns the download eta of this download.
         *
         * @return the download eta of this download
         */
        public String getDownloadableEta() {
            return downloadableEta;
        }

        /**
         * Returns whether this download has completed.
         *
         * @return whether this download has completed
         */
        public boolean isDownloaded() {
            return downloaded;
        }

        public static final String EXTRACT_AUDIO_FLAG = "--extract-audio";
        public static final String AUDIO_FORMAT_FLAG =  "--audio-format";
        public static final String OUTPUT_FLAG =  "--output";

        /**
         * Downloads this object's youtube video.
         */
        public void download() {
            String saveDir = OSUtil.buildPath(Dynamic.PATH,
                    Dynamic.USERS.getDirectoryName(), ConsoleFrame.INSTANCE.getUUID(),
                    UserFile.MUSIC.getName());

            String extension = "." + PropLoader.getString("ffmpeg_audio_output_format");

            AtomicReference<String> parsedAsciiSaveName = new AtomicReference<>(
                    StringUtil.parseNonAscii(NetworkUtil.getUrlTitle(url))
                            .replace("- YouTube", "")
                            .replaceAll(CyderRegexPatterns.windowsInvalidFilenameChars.pattern(), "").trim());

            ConsoleFrame.INSTANCE.getInputHandler().println("Downloading audio as: "
                    + parsedAsciiSaveName + extension);

            // remove trailing periods
            while (parsedAsciiSaveName.get().endsWith(".")) {
                parsedAsciiSaveName.set(parsedAsciiSaveName.get().substring(0, parsedAsciiSaveName.get().length() - 1));
            }

            // if for some reason this case happens, account for it
            if (parsedAsciiSaveName.get().isEmpty()) {
                parsedAsciiSaveName.set(SecurityUtil.generateUUID());
            }

            String[] command = {
                    AudioUtil.getYoutubeDlCommand(), url,
                    EXTRACT_AUDIO_FLAG,
                    AUDIO_FORMAT_FLAG, PropLoader.getString("ffmpeg_audio_output_format"),
                    OUTPUT_FLAG, new File(saveDir).getAbsolutePath() + OSUtil.FILE_SEP
                    + parsedAsciiSaveName + ".%(ext)s"
            };

            downloaded = false;

            CyderProgressUI ui = new CyderProgressUI();
            CyderThreadRunner.submit(() -> {
                try {
                    Process proc = Runtime.getRuntime().exec(command);

                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

                    // progress label for this download to update
                    CyderProgressBar audioProgress = new CyderProgressBar(
                            CyderProgressBar.HORIZONTAL, 0, 10000);

                    ui.setColors(CyderColors.regularPink, CyderColors.regularBlue);
                    ui.setAnimationDirection(CyderProgressUI.AnimationDirection.LEFT_TO_RIGHT);
                    audioProgress.setUI(ui);
                    audioProgress.setMinimum(0);
                    audioProgress.setMaximum(10000);
                    audioProgress.setBorder(new LineBorder(Color.black, 2));
                    audioProgress.setBounds(0, 0, 400, 40);
                    audioProgress.setVisible(true);
                    audioProgress.setValue(0);
                    audioProgress.setOpaque(false);
                    audioProgress.setFocusable(false);
                    audioProgress.repaint();

                    printLabel = new JLabel("\"" + parsedAsciiSaveName + "\"");
                    printLabel.setFont(ConsoleFrame.INSTANCE.generateUserFont());
                    printLabel.setForeground(CyderColors.vanilla);
                    printLabel.setHorizontalAlignment(JLabel.LEFT);
                    printLabel.setForeground(ConsoleFrame.INSTANCE.getInputField().getForeground());
                    printLabel.setFont(ConsoleFrame.INSTANCE.getInputField().getFont());

                    // todo not always, no need to construct if not printing too
                    ConsoleFrame.INSTANCE.getInputHandler().println(audioProgress);
                    ConsoleFrame.INSTANCE.getInputHandler().println(printLabel);

                    String fileSize = null;

                    String outputString;

                    while ((outputString = stdInput.readLine()) != null) {
                        Matcher updateMatcher = CyderRegexPatterns.updatePattern.matcher(outputString);

                        if (updateMatcher.find()) {
                            float progress = Float.parseFloat(updateMatcher.group(1)
                                    .replaceAll("[^0-9.]", ""));
                            audioProgress.setValue((int) ((progress / 100.0) * audioProgress.getMaximum()));

                            fileSize = fileSize == null ? updateMatcher.group(2) : fileSize;

                            this.downloadableName = parsedAsciiSaveName.get();
                            this.downloadableFileSize = fileSize;
                            this.downloadableProgress = progress;
                            this.downloadableRate = updateMatcher.group(3);
                            this.downloadableEta = updateMatcher.group(4);
                            updateLabel();
                        }
                    }

                    downloadThumbnail(url);

                    downloaded = true;

                    // todo not always
                    ConsoleFrame.INSTANCE.getInputHandler().println("Download complete: saved as "
                            + parsedAsciiSaveName + extension + " and added to audio queue");
                    AudioPlayer.addAudioNext(new File(OSUtil.buildPath(
                            saveDir, parsedAsciiSaveName + extension)));
                    ui.setColors(CyderColors.regularBlue, CyderColors.regularBlue);
                    audioProgress.repaint();
                    ui.stopAnimationTimer();
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                    ConsoleFrame.INSTANCE.getInputHandler().println("An exception occurred while "
                            + "attempting to download: " + url);
                    ui.stopAnimationTimer();
                } finally {
                    currentDownloads.remove(this);
                }
            }, "YouTube Downloader: " + parsedAsciiSaveName.get());
        }
    }

    /**
     * Downloads the youtube playlist provided the playlist exists.
     *
     * @param playlist the url of the playlist to download
     */
    public static void downloadPlaylist(String playlist) {
        if (AudioUtil.ffmpegInstalled() && AudioUtil.youtubeDlInstalled()) {
            String playlistID = extractPlaylistId(playlist);

            if (StringUtil.isNull(PropLoader.getString("youtube_api_3_key"))) {
                ConsoleFrame.INSTANCE.getInputHandler().println(
                        "Sorry, your YouTubeAPI3 key has not been set. Visit the user editor " +
                                "to learn how to set this in order to download whole playlists. " +
                                "In order to download individual videos, simply use the same play " +
                                "command followed by a video URL or query");
            } else {
                try {
                    String link = CyderUrls.YOUTUBE_API_V3_PLAYLIST_ITEMS +
                            "part=snippet%2C+id&playlistId=" + playlistID + "&key="
                            + PropLoader.getString("youtube_api_3_key");

                    String jsonResponse = NetworkUtil.readUrl(link);

                    Matcher m = CyderRegexPatterns.youtubeApiV3UuidPattern.matcher(jsonResponse);
                    ArrayList<String> uuids = new ArrayList<>();

                    while (m.find()) {
                        uuids.add(m.group(1));
                    }

                    for (String uuid : uuids) {
                        downloadVideo(buildYoutubeVideoUrl(uuid));
                    }
                } catch (Exception e) {
                    ExceptionHandler.silentHandle(e);
                    ConsoleFrame.INSTANCE.getInputHandler().println(
                            "An exception occurred while downloading playlist: " + playlistID);
                }
            }
        } else {
            noFfmpegOrYoutubedl();
        }
    }

    /**
     * The default resolution of thumbnails to download when the play command is invoked.
     */
    public static final Dimension DEFAULT_THUMBNAIL_DIMENSION = new Dimension(720, 720);

    /**
     * Downloads the youtube video's thumbnail with the provided
     * url to the current user's album art directory.
     *
     * @param url the url of the youtube video to download
     */
    public static void downloadThumbnail(String url) {
        downloadThumbnail(url, DEFAULT_THUMBNAIL_DIMENSION);
    }

    /**
     * Downloads the youtube video's thumbnail with the provided
     * url to the current user's album aart directory.
     *
     * @param url       the url of the youtube video to download
     * @param dimension the dimensions to crop the image to
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void downloadThumbnail(String url, Dimension dimension) {
        Preconditions.checkNotNull(ConsoleFrame.INSTANCE.getUUID());

        // get thumbnail url and file name to save it as
        BufferedImage save = getSquareThumbnail(url, dimension);

        // could not download thumbnail for some reason
        if (save == null) {
            return;
        }

        String parsedAsciiSaveName =
                StringUtil.parseNonAscii(NetworkUtil.getUrlTitle(url))
                        .replace("- YouTube", "")
                        .replaceAll(CyderRegexPatterns.windowsInvalidFilenameChars.pattern(),
                                "").trim();

        // remove trailing periods
        while (parsedAsciiSaveName.endsWith("."))
            parsedAsciiSaveName = parsedAsciiSaveName.substring(0, parsedAsciiSaveName.length() - 1);

        // if for some reason this case happens, account for it
        if (parsedAsciiSaveName.isEmpty())
            parsedAsciiSaveName = SecurityUtil.generateUUID();

        String finalParsedAsciiSaveName = parsedAsciiSaveName + ".png";

        // init album art dir
        File albumArtDir = OSUtil.buildFile(Dynamic.PATH,
                Dynamic.USERS.getDirectoryName(), ConsoleFrame.INSTANCE.getUUID(),
                UserFile.MUSIC.getName(), "AlbumArt");

        // create if not there
        if (!albumArtDir.exists()) {
            albumArtDir.mkdirs();
        }

        // create the reference file and save to it
        File saveAlbumArt = OSUtil.buildFile(Dynamic.PATH,
                Dynamic.USERS.getDirectoryName(),
                ConsoleFrame.INSTANCE.getUUID(), UserFile.MUSIC.getName(),
                "AlbumArt", finalParsedAsciiSaveName);

        try {
            ImageIO.write(save, "png", saveAlbumArt);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Retrieves the first valid UUID for the provided query (if one exists)
     *
     * @param youtubeQuery the user friendly query on youtube. Example: "Gryffin Digital Mirage"
     * @return the first UUID obtained from the raw html page youtube returns corresponding to the desired query
     */
    public static String getFirstUUID(String youtubeQuery) {
        String ret = null;

        String query = CyderUrls.YOUTUBE_QUERY_BASE + youtubeQuery.replace(" ", "+");
        String jsonString = NetworkUtil.readUrl(query);

        if (jsonString.contains("\"videoId\":\"")) {
            String[] parts = jsonString.split("\"videoId\":\"");
            ret = parts[1].substring(0, 11);
        }

        return ret;
    }

    /**
     * Outputs instructions to the ConsoleFrame due to youtube-dl or ffmpeg not being installed.
     */
    private static void noFfmpegOrYoutubedl() {
        ConsoleFrame.INSTANCE.getInputHandler().println("Sorry, but ffmpeg and/or youtube-dl " +
                "couldn't be located. Please make sure they are both installed and added to your PATH Windows" +
                " variable. Remember to also set the path to your youtube-dl executable in the user editor");

        CyderButton environmentVariableHelp = new CyderButton("Learn how to add environment variables");
        environmentVariableHelp.addActionListener(e -> NetworkUtil.openUrl(CyderUrls.environmentVariables));
        ConsoleFrame.INSTANCE.getInputHandler().println(environmentVariableHelp);

        CyderButton downloadFFMPEG = new CyderButton("Learn how to download ffmpeg");
        downloadFFMPEG.addActionListener(e -> NetworkUtil.openUrl(CyderUrls.FFMPEG_INSTALLATION));
        ConsoleFrame.INSTANCE.getInputHandler().println(downloadFFMPEG);

        CyderButton downloadYoutubeDL = new CyderButton("Learn how to download youtube-dl");
        downloadYoutubeDL.addActionListener(e ->
                NetworkUtil.openUrl(CyderUrls.YOUTUBE_DL_INSTALLATION));
        ConsoleFrame.INSTANCE.getInputHandler().println(downloadYoutubeDL);
    }

    /**
     * A widget for downloading a youtube video's thumbnail.
     */
    @Widget(triggers = {"youtube", "thumbnail"}, description = "A widget to steal youtube thumbnails")
    public static void showGui() {
        CyderFrame uuidFrame = new CyderFrame(400, 240, CyderIcons.defaultBackground);
        uuidFrame.setTitle("Thumbnail Stealer");
        uuidFrame.setTitlePosition(CyderFrame.TitlePosition.LEFT);

        CyderLabel label = new CyderLabel("Enter any valid YouTube UUID");
        label.setFont(label.getFont().deriveFont(22f));
        int labelWidth = StringUtil.getMinWidth("Enter any valid YouTube UUID", label.getFont());
        label.setBounds(400 / 2 - labelWidth / 2, 60, labelWidth, 30);
        uuidFrame.add(label);

        CyderTextField inputField = new CyderTextField(30);
        inputField.setHorizontalAlignment(JTextField.CENTER);
        inputField.setBounds(200 - labelWidth / 2, 100, labelWidth, 40);
        inputField.setToolTipText("Must be a valid UUID");
        uuidFrame.add(inputField);

        CyderButton stealButton = new CyderButton("Submit");
        stealButton.setBounds(200 - labelWidth / 2, 160, labelWidth, 40);
        uuidFrame.add(stealButton);
        stealButton.setToolTipText("Save image");
        stealButton.addActionListener(e -> {
            try {
                String uuid = inputField.getText().trim();

                if (!uuidPattern.matcher(uuid).matches()) {
                    uuidFrame.notify("Invalid UUID");
                    return;
                }

                String videoTitle = NetworkUtil.getUrlTitle(CyderUrls.YOUTUBE_VIDEO_HEADER + uuid);

                Optional<BufferedImage> optionalThumbnail = getMaxResolutionThumbnail(uuid);
                BufferedImage thumbnail = optionalThumbnail.orElse(null);

                if (thumbnail == null) {
                    uuidFrame.inform("No thumbnail found for provided youtube uuid", "Error");
                    return;
                }

                thumbnail = ImageUtil.resizeImage(thumbnail, thumbnail.getType(),
                        thumbnail.getWidth(), thumbnail.getHeight());

                CyderFrame thumbnailFrame = new CyderFrame(thumbnail.getWidth() + 10,
                        thumbnail.getHeight() + 60, new ImageIcon(thumbnail));
                thumbnailFrame.setBackground(CyderColors.navy);
                thumbnailFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
                thumbnailFrame.setTitle(videoTitle);

                CyderButton addToBackgrounds = new CyderButton("Set as background");
                addToBackgrounds.setBounds(10, thumbnail.getHeight() + 10,
                        (thumbnail.getWidth() - 30) / 2, 40);
                String finalThumbnailURL = buildMaxResThumbnailUrl(uuid);
                addToBackgrounds.addActionListener(e1 -> {

                    try {
                        BufferedImage save = ImageIO.read(new URL(finalThumbnailURL));

                        String title = videoTitle.substring(Math.min(MAX_THUMBNAIL_CHARS, videoTitle.length()));

                        File saveFile = OSUtil.buildFile(Dynamic.PATH,
                                Dynamic.USERS.getDirectoryName(), ConsoleFrame.INSTANCE.getUUID(),
                                UserFile.BACKGROUNDS.getName(), title + ".png");

                        ImageIO.write(save, "png", saveFile);

                        thumbnailFrame.notify("Successfully saved as a background file." +
                                " You may view this by switching the background or by typing \"prefs\" " +
                                "to view your profile settings.");
                    } catch (IOException ex) {
                        ExceptionHandler.handle(ex);
                    }
                });
                thumbnailFrame.add(addToBackgrounds);

                // open the video, I'm not sure why the user would want to do this but it's here
                CyderButton openVideo = new CyderButton("Open Video");
                openVideo.setBounds(20 + addToBackgrounds.getWidth(),
                        thumbnail.getHeight() + 10, (thumbnail.getWidth() - 30) / 2, 40);
                openVideo.addActionListener(e1 -> NetworkUtil.openUrl("youtube.com/watch?v=" + uuid));
                thumbnailFrame.add(openVideo);

                thumbnailFrame.setVisible(true);
                thumbnailFrame.setLocationRelativeTo(uuidFrame);

                uuidFrame.dispose();
            } catch (Exception exc) {
                uuidFrame.notify("Invalid YouTube UUID");
            }
        });

        uuidFrame.finalizeAndShow();
    }

    // todo not sure this works
    /**
     * Returns a square, 720x720 image of the provided youtube video's thumbnail.
     *
     * @param videoURL  the url of the youtube video to query
     * @param dimension the dimension of the resulting image
     * @return a square image of the thumbnail
     */
    public static BufferedImage getSquareThumbnail(String videoURL, Dimension dimension) {
        String uuid = getUuid(videoURL);

        BufferedImage ret;
        BufferedImage save = null;

        try {
            save = ImageIO.read(new URL(buildMaxResThumbnailUrl(uuid)));
        } catch (Exception e) {
            ExceptionHandler.silentHandle(e);

            try {
                save = ImageIO.read(new URL(buildSdDefThumbnailUrl(uuid)));
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }

        if (save == null) {
            return null;
        }

        int w = save.getWidth();
        int h = save.getHeight();

        if (w > dimension.getWidth()) {
            //crop to middle of w
            int cropWStart = (int) ((w - dimension.getWidth()) / 2.0);
            save = save.getSubimage(cropWStart, 0, (int) dimension.getWidth(), h);
        }

        w = save.getWidth();
        h = save.getHeight();

        if (h > dimension.getHeight()) {
            //crop to middle of h
            int cropHStart = (int) ((h - dimension.getHeight()) / 2);
            save = save.getSubimage(0, cropHStart, w, (int) dimension.getHeight());
        }

        ret = save;

        return ret;
    }

    /**
     * Returns whether the provided url is a playlist url.
     *
     * @param url the url
     * @return whether the provided url references a YouTube playlist
     */
    public static boolean isPlaylistUrl(String url) {
        if (StringUtil.isNull(url))
            throw new IllegalArgumentException("Provided url is null");

        return url.startsWith(CyderUrls.YOUTUBE_PLAYLIST_HEADER);
    }

    /**
     * Extracts the YouTube playlist id from the provided playlist url.
     *
     * @param url the url
     * @return the youtube playlist url
     */
    public static String extractPlaylistId(String url) {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!isPlaylistUrl(url));

        return url.replace(CyderUrls.YOUTUBE_PLAYLIST_HEADER, "").trim();
    }

    /**
     * Returns a url for the youtube video with the provided uuid.
     *
     * @param uuid the uuid of the video
     * @return a url for the youtube video with the provided uuid
     * @throws IllegalArgumentException if the provided uuid is not 11 chars long
     */
    public static String buildYoutubeVideoUrl(String uuid) {
        Preconditions.checkNotNull(uuid);
        Preconditions.checkArgument(uuid.length() == 11);

        return CyderUrls.YOUTUBE_VIDEO_HEADER + uuid;
    }

    /**
     * Returns a URL for the maximum resolution version of the youtube video's thumbnail.
     *
     * @param uuid the uuid of the video
     * @return a URL for the maximum resolution version of the youtube video's thumbnail
     */
    public static String buildMaxResThumbnailUrl(String uuid) {
        Preconditions.checkNotNull(uuid);
        Preconditions.checkArgument(uuid.length() == 11);

        return CyderUrls.YOUTUBE_THUMBNAIL_BASE + uuid + "/maxresdefault.jpg";
    }

    /**
     * Returns a url for the default thumbnail of a youtube video.
     *
     * @param uuid the uuid of the video
     * @return a url for the default youtube video's thumbnail
     */
    public static String buildSdDefThumbnailUrl(String uuid) {
        Preconditions.checkNotNull(uuid);
        Preconditions.checkArgument(uuid.length() == 11);

        return CyderUrls.YOUTUBE_THUMBNAIL_BASE + uuid + "/sddefault.jpg";
    }

    /**
     * Extracts the uuid for the youtube video from the url
     *
     * @param url the youtube url to extract the uuid from
     * @return the extracted uuid
     */
    public static String getUuid(String url) {
        Preconditions.checkNotNull(url);
        Matcher matcher = CyderRegexPatterns.extractYoutubeUuidPattern.matcher(url);

        if (matcher.find()) {
            return matcher.group();
        }

        throw new IllegalArgumentException("No UUID found in provided string: " + url);
    }

    /**
     * The range of valid values for the number of results a youtube api 3 search query.
     */
    private static final Range<Integer> searchQueryResultsRange = Range.closed(1, 20);

    /**
     * Constructs the url to query YouTube with a specific string for video results.
     *
     * @param numResults the number of results to return (max 20 results per page)
     * @param query   the search query such as "black parade"
     * @return the constructed url to match the provided parameters
     */
    @SuppressWarnings("ConstantConditions") // unit test asserts throws for query of null
    public static String buildYouTubeApiV3SearchQuery(int numResults, String query) {
        Preconditions.checkNotNull(query);
        Preconditions.checkArgument(searchQueryResultsRange.contains(numResults));
        Preconditions.checkArgument(!query.isEmpty());

        // load props if not loaded (probably a Jenkins build)
        if (!PropLoader.arePropsLoaded()) {
            throw new IllegalMethodException("Cannot build search query because props are not loaded");
        }

        String key = PropLoader.getString("youtube_api_3_key");
        Preconditions.checkArgument(!StringUtil.isNull(key));

        String[] parts = query.split("\\s+");

        StringBuilder builder = new StringBuilder();

        for (int i = 0 ; i < parts.length ; i++) {
            String append = parts[i].replaceAll("[^0-9A-Za-z\\-._~%]+", "");
            builder.append(append.trim());

            if (i != parts.length - 1 && !append.isEmpty()) {
                builder.append("%20");
            }
        }

        return CyderUrls.YOUTUBE_API_V3_SEARCH_BASE + "&maxResults=" + numResults + "&q="
                + builder + "&type=video" + "&key=" + key;
    }

    /**
     * Returns the maximum resolution thumbnail for the youtube video with the provided uuid.
     *
     * @param uuid the uuid of the video
     * @return the maximum resolution thumbnail for the youtube video
     */
    public static Optional<BufferedImage> getMaxResolutionThumbnail(String uuid) {
        Preconditions.checkNotNull(uuid);
        Preconditions.checkArgument(uuid.length() == 11);

        String thumbnailURL = buildMaxResThumbnailUrl(uuid);

        BufferedImage thumbnail;

        try {
            thumbnail = ImageIO.read(new URL(thumbnailURL));
        } catch (Exception ignored) {
            try {
                thumbnailURL = buildSdDefThumbnailUrl(uuid);
                thumbnail = ImageIO.read(new URL(thumbnailURL));
            } catch (Exception ignored1) {
                thumbnail = null;
            }
        }

        return thumbnail == null ? Optional.empty() : Optional.of(thumbnail);
    }
}