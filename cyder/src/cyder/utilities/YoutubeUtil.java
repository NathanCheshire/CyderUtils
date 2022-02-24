package cyder.utilities;

import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.enums.AnimationDirection;
import cyder.genesis.CyderCommon;
import cyder.handlers.external.AudioPlayer;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.ui.*;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods related to youtube videos.
 */
public class YoutubeUtil {

    /**
     * The maximum number of chars that can be used for a filename from a youtube video's title.
     */
    public static final int MAX_THUMBNAIL_CHARS = 20;

    /**
     * The header for individual youtube videos without their uuid.
     */
    public static final String YOUTUBE_VIDEO_HEADER = "https://www.youtube.com/watch?v=";

    /**
     * The header for individual youtube video thumbnails without their uuid
     */
    public static final String YOUTUBE_THUMBNAIL_MAX_HEADER = "https://img.youtube.com/vi/";

    /**
     * Restrict instantiation of class.
     */
    private YoutubeUtil() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    /**
     * Downloads the youtube video with the provided url.
     *
     * @param url the url of the video to download
     */
    public static void downloadVideo(String url) {
        if (ffmpegInstalled() && youtubedlInstalled()) {
            String saveDir = OSUtil.buildPath("dynamic",
                    "users",ConsoleFrame.getConsoleFrame().getUUID(), "Music");
            String extension = ".mp3";

            Runtime rt = Runtime.getRuntime();

            String parsedAsciiSaveName =
                    StringUtil.parseNonAscii(NetworkUtil.getURLTitle(url))
                            .replace("- YouTube","").trim();

            ConsoleFrame.getConsoleFrame().getInputHandler()
                    .println("Downloading audio as: " + parsedAsciiSaveName + extension);

            // remove trailing periods
            while (parsedAsciiSaveName.endsWith("."))
                parsedAsciiSaveName = parsedAsciiSaveName.substring(0, parsedAsciiSaveName.length() - 1);

            // if for some reason this case happens, account for it
            if (parsedAsciiSaveName.length() == 0)
                parsedAsciiSaveName = SecurityUtil.generateUUID();

            final String finalParsedAsciiSaveName = parsedAsciiSaveName;

            String[] commands = {
                    "youtube-dl",
                    url,
                    "--extract-audio",
                    "--audio-format","mp3",
                    "--output", new File(saveDir).getAbsolutePath()
                    + OSUtil.FILE_SEP + finalParsedAsciiSaveName + ".%(ext)s"
            };

            CyderThreadRunner.submit(() -> {
                try {
                    Process proc = rt.exec(commands);

                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

                    // progress label for this download to update
                    CyderProgressBar audioProgress = new CyderProgressBar(CyderProgressBar.HORIZONTAL, 0, 10000);
                    CyderProgressUI ui = new CyderProgressUI();
                    ui.setColors(new Color[]{CyderColors.regularPink, CyderColors.regularBlue});
                    ui.setAnimationDirection(AnimationDirection.LEFT_TO_RIGHT);
                    ui.setShape(CyderProgressUI.Shape.SQUARE);
                    audioProgress.setUI(ui);
                    audioProgress.setMinimum(0);
                    audioProgress.setMaximum(10000);
                    audioProgress.setBorder(new LineBorder(Color.black, 2));
                    audioProgress.setBounds(0,0,400, 40);
                    audioProgress.setVisible(true);
                    audioProgress.setValue(0);
                    audioProgress.setOpaque(false);
                    audioProgress.setFocusable(false);
                    audioProgress.repaint();
                    ConsoleFrame.getConsoleFrame().getInputHandler()
                            .printlnComponent(audioProgress);

                    Pattern updatePattern  = Pattern.compile(
                            "\\s*\\[download]\\s*([0-9]{1,3}.[0-9]%)\\s*of\\s*([0-9A-Za-z.]+)" +
                                    "\\s*at\\s*([0-9A-Za-z./]+)\\s*ETA\\s*([0-9:]+)");

                    String fileSize = null;

                    String outputString;

                    while ((outputString = stdInput.readLine()) != null) {
                        Matcher updateMatcher = updatePattern.matcher(outputString);

                        if (updateMatcher.find()) {
                            float progress = Float.parseFloat(updateMatcher.group(1)
                                    .replaceAll("[^0-9.]",""));
                            audioProgress.setValue((int) ((progress / 100.0) * audioProgress.getMaximum()));

                            if (fileSize == null) {
                                fileSize = updateMatcher.group(2);
                                ConsoleFrame.getConsoleFrame().getInputHandler()
                                        .println("Download size: " + fileSize);
                            }

                            // todo try and display in a better way
                            audioProgress.setToolTipText("Progress: " + progress + "%, Rate: "
                                    + updateMatcher.group(3) + ", ETA: " + updateMatcher.group(4));
                        }
                    }

                    downloadThumbnail(url);
                    ConsoleFrame.getConsoleFrame().getInputHandler()
                            .println("Download complete: saved as " + finalParsedAsciiSaveName + extension
                            + " and added to mp3 queue");
                    AudioPlayer.addToMp3Queue(new File(OSUtil.buildPath(
                            saveDir, finalParsedAsciiSaveName + extension)));

                    ui.stopAnimationTimer();
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                    ConsoleFrame.getConsoleFrame().getInputHandler()
                            .println("An exception occured while attempting to download: " + url);
                }
            }, "YouTube Download Progress Updater");
        } else {
            noFfmpegOrYoutubedl();
        }
    }

    /**
     * Downloads the youtube playlist provided the playlist exists.
     *
     * @param playlist the url of the playlist to download
     */
    public static void downloadPlaylist(String playlist) {
        if (ffmpegInstalled() && youtubedlInstalled()) {
            String playlistID = extractPlaylistId(playlist);

            if (StringUtil.isNull(UserUtil.extractUser().getYouTubeAPI3Key())) {
                ConsoleFrame.getConsoleFrame().getInputHandler().println(
                        "Sorry, your YouTubeAPI3 key has not been set. Visit the user editor " +
                                "to learn how to set this in order to download whole playlists. " +
                                "In order to download individual videos, simply use the same play " +
                                "command followed by a video URL or query");
            } else {
                try {
                    String link = "https://www.googleapis.com/youtube/v3/playlistItems?" +
                            "part=snippet%2C+id&playlistId=" + playlistID + "&key="
                            + UserUtil.extractUser().getYouTubeAPI3Key();

                    String jsonResponse = NetworkUtil.readUrl(link);

                    Pattern p = Pattern.compile(
                            "\"resourceId\":\\s*\\{\\s*\n\\s*\"kind\":\\s*\"youtube#video\",\\s*\n\\s*\"" +
                                    "videoId\":\\s*\"(.*)\"\\s*\n\\s*},");
                    Matcher m = p.matcher(jsonResponse);
                    ArrayList<String> uuids = new ArrayList<>();

                    while (m.find()) {
                        uuids.add(m.group(1));
                    }

                    for (String uuid : uuids) {
                       downloadVideo(buildYoutubeURL(uuid));
                    }
                } catch (Exception e) {
                    ExceptionHandler.silentHandle(e);
                    ConsoleFrame.getConsoleFrame().getInputHandler().println(
                            "An exception occured while downloading playlist: " + playlistID);
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
     * @param url the url of the youtube video to download
     * @param dimension the dimensions to crop the image to
     */
    public static void downloadThumbnail(String url, Dimension dimension) {
        if (ConsoleFrame.getConsoleFrame().getUUID() == null)
            throw new IllegalStateException("No user is associated with Cyder");

        // get thumbnail url and file name to save it as
        BufferedImage save = YoutubeUtil.getSquareThumbnail(url, dimension);
        String parsedAsciiSaveName =
                StringUtil.parseNonAscii(NetworkUtil.getURLTitle(url))
                        .replace("- YouTube","").trim() + ".png";

        // remove trailing periods
        while (parsedAsciiSaveName.endsWith("."))
            parsedAsciiSaveName = parsedAsciiSaveName.substring(0, parsedAsciiSaveName.length() - 1);

        // if for some reason this case happens, account for it
        if (parsedAsciiSaveName.length() == 0)
            parsedAsciiSaveName = SecurityUtil.generateUUID();

        final String finalParsedAsciiSaveName = parsedAsciiSaveName;

        // init album art dir
        File albumArtDir = new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID()
                + "/Music/AlbumArt");

        // create if not there
        if (!albumArtDir.exists())
            albumArtDir.mkdir();

        // create the reference file and save to it
        File saveAlbumArt = new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID()
                + "/Music/AlbumArt/" + parsedAsciiSaveName);
        try {
            ImageIO.write(save, "png", saveAlbumArt);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Returns whether ffmpeg is installed.
     *
     * @return whether ffmpeg is installed
     */
    public static boolean ffmpegInstalled() {
        boolean ret = true;

        try {
            Runtime rt = Runtime.getRuntime();
            String command = "ffmpeg";
            Process proc = rt.exec(command);
        } catch (Exception e) {
            ret = false;
            ExceptionHandler.silentHandle(e);
        }

        return ret;
    }

    /**
     * Returns whether youtube-dl is installed.
     *
     * @return whether youtube-dl is installed
     */
    public static boolean youtubedlInstalled() {
        boolean ret = true;

        try {
            Runtime rt = Runtime.getRuntime();
            String command = "youtube-dl";
            Process proc = rt.exec(command);
        } catch (Exception e) {
            ret = false;
            ExceptionHandler.silentHandle(e);
        }

        return ret;
    }

    /**
     * Retreives the first valid UUID for the provided query (if one exists)
     *
     * @param youtubeQuery the user friendly query on youtube. Example: "Gryffin Digital Mirage"
     * @return the first UUID obtained from the raw html page youtube returns corresponding to the desired query
     */
    public static String getFirstUUID(String youtubeQuery) {
        String ret = null;

        String query = "https://www.youtube.com/results?search_query=" + youtubeQuery.replace(" ", "+");
        String jsonString = NetworkUtil.readUrl(query);

        if (jsonString.contains("\"videoId\":\"")) {
            String[] parts = jsonString.split("\"videoId\":\"");
            ret =  parts[1].substring(0,11);
        }

        return ret;
    }

    /**
     * Outputs instructions to the ConsoleFrame due to youtube-dl or ffmpeg not being installed.
     */
    private static void noFfmpegOrYoutubedl() {
        ConsoleFrame.getConsoleFrame().getInputHandler().println("Sorry, but ffmpeg and/or youtube-dl " +
                "couldn't be located. Please make sure they are both installed and added to your PATH Windows" +
                " variable");
        ConsoleFrame.getConsoleFrame().getInputHandler().println(
                "Remember to also set the path to your youtube-dl executable in the user editor");

        CyderButton environmentVariableHelp = new CyderButton("Learn how to add environment variables");
        environmentVariableHelp.addActionListener(e ->
                NetworkUtil.openUrl("https://www.architectryan.com/2018/03/17/add-to-the-path-on-windows-10/"));
        ConsoleFrame.getConsoleFrame().getInputHandler().printlnComponent(environmentVariableHelp);

        CyderButton downloadFFMPEG = new CyderButton("Learn how to download ffmpeg");
        downloadFFMPEG.addActionListener(e ->
                NetworkUtil.openUrl("https://www.wikihow.com/Install-FFmpeg-on-Windows"));
        ConsoleFrame.getConsoleFrame().getInputHandler().printlnComponent(downloadFFMPEG);

        CyderButton downloadYoutubeDL = new CyderButton("Learn how to download youtube-dl");
        downloadYoutubeDL.addActionListener(e ->
                NetworkUtil.openUrl("https://github.com/ytdl-org/youtube-dl#installation"));
        ConsoleFrame.getConsoleFrame().getInputHandler().printlnComponent(downloadYoutubeDL);
    }

    /**
     * A widget for downloading a youtube video's thumbnail.
     */
    @Widget(triggers = {"youtube", "thumbnail"}, description = "A widget to steal youtube thumbnails")
    public static void showGUI() {
        Logger.log(Logger.Tag.WIDGET_OPENED, "YOUTUBE");

        CyderFrame uuidFrame = new CyderFrame(400,240, CyderIcons.defaultBackground);
        uuidFrame.setTitle("Thumbnail Stealer");
        uuidFrame.setTitlePosition(CyderFrame.TitlePosition.LEFT);

        CyderLabel label = new CyderLabel("Enter any valid YouTube UUID");
        label.setFont(label.getFont().deriveFont(22f));
        int labelWidth = StringUtil.getMinWidth("Enter any valid YouTube UUID", label.getFont());
        label.setBounds(400 / 2 - labelWidth / 2, 60, labelWidth, 30);
        uuidFrame.add(label);

        CyderTextField inputField = new CyderTextField(30);
        inputField.setRegexMatcher("[A-Za-z0-9_\\-]{0,11}");
        inputField.setBounds(200 - labelWidth / 2, 100, labelWidth, 40);
        inputField.setToolTipText("Must be a valid UUID");
        uuidFrame.add(inputField);

        CyderButton stealButton = new CyderButton("Submit");
        stealButton.setBounds(200 - labelWidth / 2, 160, labelWidth, 40);
        uuidFrame.add(stealButton);
        stealButton.setToolTipText("Save image");
        stealButton.addActionListener(e -> {
            try {
                String thumbnailURL = buildThumbnailURL(inputField.getText().trim());
                String videoTitle = NetworkUtil.getURLTitle(YOUTUBE_VIDEO_HEADER + inputField.getText().trim());

                BufferedImage thumbnail = ImageIO.read(new URL(thumbnailURL));
                thumbnail = ImageUtil.resizeImage(thumbnail, thumbnail.getType(),
                        thumbnail.getWidth(), thumbnail.getHeight());

                CyderFrame thumbnailFrame = new CyderFrame(thumbnail.getWidth() + 10,
                        thumbnail.getHeight() + 60, new ImageIcon(thumbnail));
                thumbnailFrame.setBackground(CyderColors.navy);
                thumbnailFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
                thumbnailFrame.setTitle(videoTitle);

                CyderButton addToBackgrounds = new CyderButton("Set as background");
                addToBackgrounds.setBounds(10, thumbnail.getHeight() + 10,
                        (thumbnail.getWidth() - 30) / 2 , 40);
                addToBackgrounds.addActionListener(e1 -> {

                    try {
                        BufferedImage save = ImageIO.read(new URL(thumbnailURL));

                        String title = videoTitle.substring(Math.min(MAX_THUMBNAIL_CHARS, videoTitle.length()));

                        File saveFile = new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID()
                                + "/Backgrounds/" + title + ".png");

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
                openVideo.addActionListener(e1 -> NetworkUtil.openUrl(
                        buildYoutubeURL(inputField.getText().trim())));
                thumbnailFrame.add(openVideo);

                thumbnailFrame.setVisible(true);
                thumbnailFrame.setLocationRelativeTo(uuidFrame);

                uuidFrame.dispose();
            }

            catch (Exception exc) {
                uuidFrame.notify("Invalid YouTube UUID");
            }
        });

        uuidFrame.setVisible(true);
        uuidFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());
    }

    /**
     * Returns a square, 720x720 image of the provided youtube video's thumbnail.
     *
     * @param videoURL the url of the youtube video to query
     * @param dimension the dimension of the resulting image
     * @return a square image of the thumbnail
     */
    public static BufferedImage getSquareThumbnail(String videoURL, Dimension dimension) {
        String uuid = getYoutubeUUID(videoURL);

        String thumbnailURL = buildThumbnailURL(uuid);
        BufferedImage ret = null;

        try {
            BufferedImage save = ImageIO.read(new URL(thumbnailURL));
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

        } catch (IOException ex) {
            ExceptionHandler.handle(ex);
        }

        return ret;
    }

    /**
     * The header that all youtube playlists start with.
     */
    public static final String PLAYLIST_HEADER = "https://www.youtube.com/playlist?list=";

    /**
     * Returns whether the provided url is a playlist url.
     *
     * @param url the url
     * @return whether the provided url references a YouTube playlist
     */
    public static boolean isPlaylistUrl(String url) {
        if (StringUtil.isNull(url))
            throw new IllegalArgumentException("Provided url is null");

        return url.startsWith(PLAYLIST_HEADER);
    }

    /**
     * Extracts the YouTube playlist id from the provided playlist url.
     *
     * @param url the url
     * @return the youtube playlist url
     */
    public static String extractPlaylistId(String url) {
        if (StringUtil.isNull(url))
            throw new IllegalArgumentException("Provided url is null");
        else if (!isPlaylistUrl(url))
            throw new IllegalArgumentException("Provided url is not a youtube playlist");

        return url.replace(PLAYLIST_HEADER, "").trim();
    }

    /**
     * Returns a url for the youtube video with the provided uuid.
     *
     * @param uuid the uuid of the video
     * @return a url for the youtube video with the provided uuid
     * @throws IllegalArgumentException if the provided uuid is not 11 chars long
     */
    public static String buildYoutubeURL(String uuid) {
        if (uuid.length() != 11)
            throw new IllegalArgumentException("Provided uuid is not 11 chars");

        return YOUTUBE_VIDEO_HEADER + uuid;
    }


    /**
     * Returns a URL for the maximum resolution version of the youtube video's thumbnail.
     *
     * @param uuid the uuid of the video
     * @return a URL for the maximum resolution version of the youtube video's thumbnail
     */
    public static String buildThumbnailURL(String uuid) {
        return YOUTUBE_THUMBNAIL_MAX_HEADER + uuid + "/maxresdefault.jpg";
    }

    /**
     * Extracts the uuid for the youtube video from the url
     *
     * @param youtubeURL the youtube url to extract the uuid from
     * @return the extracted uuid
     */
    public static String getYoutubeUUID(String youtubeURL) {
        String pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed/)[^#&?]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(youtubeURL);

        if(matcher.find()){
            return matcher.group();
        } else throw new IllegalArgumentException("No UUID found in provided string: " + youtubeURL);
    }
}
