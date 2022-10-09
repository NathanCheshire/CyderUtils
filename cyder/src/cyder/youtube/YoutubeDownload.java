package cyder.youtube;

import com.google.common.base.Preconditions;
import cyder.audio.AudioPlayer;
import cyder.audio.AudioUtil;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.constants.CyderRegexPatterns;
import cyder.enums.Dynamic;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.input.BaseInputHandler;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.props.PropLoader;
import cyder.threads.CyderThreadRunner;
import cyder.ui.button.CyderButton;
import cyder.ui.progress.CyderProgressBar;
import cyder.ui.progress.CyderProgressUI;
import cyder.user.UserFile;
import cyder.utils.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;

import static cyder.youtube.YoutubeConstants.*;

/**
 * A utility class for downloading a video's audio and/or video from YouTube.
 */
public class YoutubeDownload {
    /**
     * The name of this download object.
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
     * Whether this download has completed, not necessarily downloaded.
     */
    private boolean done;

    /**
     * Whether this download is currently underway.
     */
    private boolean downloading;

    /**
     * Whether this download was canceled externally.
     */
    private boolean canceled;

    /**
     * The label this class will print and update with statistics about the download.
     */
    private JLabel downloadProgressLabel;

    /**
     * The button used to cancel the download.
     */
    private CyderButton cancelButton;

    /**
     * The url of the youtube video to download.
     */
    private final String url;

    /**
     * The runnable to invoke when the download is canceled.
     */
    private Runnable onCanceledCallback;

    /**
     * The runnable to invoke when the download completes successfully.
     */
    private Runnable onDownloadedCallback;

    /**
     * The file this object downloaded from YouTube.
     */
    private File downloadFile;

    /**
     * The exit code for the internal download process.
     */
    private int processExitCode = DOWNLOAD_NOT_FINISHED;

    /**
     * Suppress default constructor.
     */
    private YoutubeDownload() {
        throw new IllegalMethodException("Illegal use of constructor without url");
    }

    /**
     * Constructs a new YoutubeDownload object.
     *
     * @param url the url of the video to download
     */
    public YoutubeDownload(String url) {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());
        Preconditions.checkState(AudioUtil.youtubeDlInstalled());

        this.url = url;
    }

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

    /**
     * Returns whether this download has ended. Not necessarily whether it downloaded.
     * Use {@link #isDownloaded()} to check for downloaded.
     *
     * @return whether this download has ended
     */
    public boolean isDone() {
        return done;
    }

    /**
     * Returns whether this download is currently downloading.
     *
     * @return whether this download is currently downloading
     */
    public boolean isDownloading() {
        return downloading;
    }

    /**
     * Returns whether this download was canceled.
     *
     * @return whether this download was canceled
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Cancels this download if downloading.
     */
    public void cancel() {
        if (isDownloading()) {
            canceled = true;
        }
    }

    /**
     * Returns the on cancel callback.
     *
     * @return the on cancel callback
     */
    public Runnable getOnCanceledCallback() {
        return onCanceledCallback;
    }

    /**
     * Sets the callback to invoke when/if a cancel action is invoked.
     *
     * @param onCanceledCallback the callback to invoke when/if a cancel action is invoked
     */
    public void setOnCanceledCallback(Runnable onCanceledCallback) {
        this.onCanceledCallback = Preconditions.checkNotNull(onCanceledCallback);
    }

    /**
     * Returns the on download callback.
     *
     * @return the on download callback
     */
    public Runnable getOnDownloadedCallback() {
        return onDownloadedCallback;
    }

    /**
     * Sets the callback to invoke when a download completes.
     *
     * @param onDownloadedCallback the callback to invoke when a download completes
     */
    public void setOnDownloadedCallback(Runnable onDownloadedCallback) {
        this.onDownloadedCallback = Preconditions.checkNotNull(onDownloadedCallback);
    }

    /**
     * Returns the file this object downloaded from YouTube.
     *
     * @return the file this object downloaded from YouTube
     */
    public File getDownloadFile() {
        Preconditions.checkState(!isCanceled());
        Preconditions.checkState(isDone());
        Preconditions.checkState(isDownloaded());
        Preconditions.checkNotNull(downloadFile);

        return downloadFile;

    }

    /**
     * Returns the exit code of the internal download process if completed.
     *
     * @return the exit code of the internal download process if completed
     */
    public int getProcessExitCode() {
        Preconditions.checkState(processExitCode != DOWNLOAD_NOT_FINISHED,
                "Process not yet finished");

        return processExitCode;
    }

    /**
     * Updates the download progress label text.
     */
    public void updateProgressLabelText() {
        downloadProgressLabel.setText(
                BoundsUtil.OPENING_HTML_TAG + downloadableName
                        + "<br/>File size: " + downloadableFileSize
                        + "<br/>Progress: " + downloadableProgress + "%"
                        + "<br/>Rate: " + downloadableRate
                        + "<br/>Eta: " + downloadableEta
                        + BoundsUtil.CLOSING_HTML_TAG);
        downloadProgressLabel.revalidate();
        downloadProgressLabel.repaint();
        downloadProgressLabel.setHorizontalAlignment(JLabel.LEFT);
    }

    /**
     * Refreshes the font of the label containing the download information.
     */
    public void refreshLabelFont() {
        if (isDone()) return;

        Font font = Console.INSTANCE.generateUserFont();
        downloadProgressLabel.setFont(font);
        cancelButton.setFont(font);
    }

    /**
     * The download progress bar to print and update if a valid input handler is provided.
     */
    private CyderProgressBar downloadProgressBar;

    /**
     * The progress bar ui to use for the download progress bar.
     */
    private CyderProgressUI downloadProgressBarUi;

    /**
     * The input handler to print updates to if not null;
     */
    private BaseInputHandler inputHandler;

    /**
     * Returns the input handler set for this YouTube download.
     *
     * @return the input handler set for this YouTube download
     */
    public BaseInputHandler getInputHandler() {
        return inputHandler;
    }

    /**
     * Sets the input handler set for this YouTube download.
     *
     * @param inputHandler the input handler set for this YouTube download
     */
    public void setInputHandler(BaseInputHandler inputHandler) {
        this.inputHandler = Preconditions.checkNotNull(inputHandler);
    }

    /**
     * Downloads this object's YouTube video audio.
     */
    public void download() {
        Preconditions.checkState(!done, "Object attempted to download previously");

        boolean shouldPrintUpdates = inputHandler != null;

        String userMusicDir = OsUtil.buildPath(
                Dynamic.PATH,
                Dynamic.USERS.getDirectoryName(),
                Console.INSTANCE.getUuid(),
                UserFile.MUSIC.getName());

        String ffmpegAudioOutputFormat = PropLoader.getString(FFMPEG_AUDIO_OUTPUT_FORMAT);
        String extension = "." + ffmpegAudioOutputFormat;

        AtomicReference<String> parsedSaveName = new AtomicReference<>(
                StringUtil.parseNonAscii(NetworkUtil.getUrlTitle(url))
                        .replace(YOUTUBE_VIDEO_URL_TITLE_SUFFIX, "")
                        .replaceAll(CyderRegexPatterns.windowsInvalidFilenameChars.pattern(), "").trim());

        // Remove trailing periods
        while (parsedSaveName.get().endsWith(".")) {
            parsedSaveName.set(parsedSaveName.get().substring(0, parsedSaveName.get().length() - 1));
        }

        // If for some reason this case happens, account for it
        if (parsedSaveName.get().isEmpty()) {
            parsedSaveName.set(SecurityUtil.generateUuid());
        }

        String[] command = {
                AudioUtil.getYoutubeDlCommand(), url,
                FFMPEG_EXTRACT_AUDIO_FLAG,
                FFMPEG_AUDIO_FORMAT_FLAG, ffmpegAudioOutputFormat,
                FFMPEG_OUTPUT_FLAG, new File(userMusicDir).getAbsolutePath() + OsUtil.FILE_SEP
                + parsedSaveName + ".%(ext)s"
        };

        YoutubeUtil.addActiveDownload(this);
        downloadableName = parsedSaveName.get();

        String threadName = "YouTube Downloader, saveName=" + parsedSaveName.get()
                + ", uuid=" + YoutubeUtil.getUuid(url);
        CyderThreadRunner.submit(() -> {
            try {
                if (shouldPrintUpdates) {
                    inputHandler.println("Downloading audio as: " + parsedSaveName + extension);
                    constructAndPrintUiElements();
                }

                downloading = true;
                Process proc = Runtime.getRuntime().exec(command);

                BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String outputString;

                while ((outputString = stdInput.readLine()) != null) {
                    if (isCanceled()) {
                        proc.destroy();

                        cleanUpFromCancel(new File(userMusicDir), parsedSaveName.get());
                        if (onDownloadedCallback != null) {
                            onCanceledCallback.run();
                        }

                        break;
                    }

                    Matcher updateMatcher = CyderRegexPatterns.updatePattern.matcher(outputString);

                    if (updateMatcher.find()) {
                        String progressPart = updateMatcher.group(progressIndex);
                        float progress = Float.parseFloat(progressPart
                                .replaceAll(CyderRegexPatterns.nonNumberRegex, ""));

                        downloadableProgress = progress;
                        downloadableFileSize = updateMatcher.group(sizeIndex);
                        downloadableRate = updateMatcher.group(rateIndex);
                        downloadableEta = updateMatcher.group(etaIndex);

                        if (shouldPrintUpdates && downloadProgressBar != null) {
                            int value = (int) ((progress / 100.0f) * downloadProgressBar.getMaximum());
                            downloadProgressBar.setValue(value);
                            updateProgressLabelText();
                        }
                    }
                }

                processExitCode = proc.waitFor();

                if (processExitCode != SUCCESSFUL_EXIT_CODE) {
                    if (shouldPrintUpdates) {
                        if (isCanceled()) {
                            inputHandler.println("Canceled download due to user request");
                        } else {
                            inputHandler.println("Failed to download audio");
                        }
                    }
                } else if (!isCanceled()) {
                    downloadFile = OsUtil.buildFile(userMusicDir, parsedSaveName + extension);
                    downloaded = true;

                    YoutubeUtil.downloadThumbnail(url);
                    AudioPlayer.addAudioNext(downloadFile);

                    if (onDownloadedCallback != null) {
                        onDownloadedCallback.run();
                    }

                    if (shouldPrintUpdates) {
                        inputHandler.println("Download complete: saved as "
                                + downloadableName + " and added to audio queue");
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);

                if (shouldPrintUpdates) {
                    inputHandler.println("An exception occurred while attempting to download, url=" + url);
                }
            } finally {
                YoutubeUtil.removeActiveDownload(this);
                done = true;
                downloading = false;

                if (shouldPrintUpdates) {
                    cleanUpUi();
                }
            }
        }, threadName);
    }

    /**
     * Constructs and prints the progress bar and label to the linked input handler.
     */
    private void constructAndPrintUiElements() {
        downloadProgressBar = new CyderProgressBar(CyderProgressBar.HORIZONTAL,
                downloadProgressMin, downloadProgressMax);

        downloadProgressBarUi = new CyderProgressUI();
        downloadProgressBarUi.setAnimationColors(CyderColors.regularPink, CyderColors.regularBlue);
        downloadProgressBarUi.setAnimationDirection(CyderProgressUI.AnimationDirection.LEFT_TO_RIGHT);

        downloadProgressBar.setUI(downloadProgressBarUi);
        downloadProgressBar.setMinimum(downloadProgressMin);
        downloadProgressBar.setMaximum(downloadProgressMax);
        downloadProgressBar.setBorder(new LineBorder(Color.black, 2));
        downloadProgressBar.setBounds(0, 0, 400, 40);
        downloadProgressBar.setVisible(true);
        downloadProgressBar.setValue(0);
        downloadProgressBar.setOpaque(false);
        downloadProgressBar.setFocusable(false);
        downloadProgressBar.repaint();

        downloadProgressLabel = new JLabel("\"" + downloadableName + "\"");
        downloadProgressLabel.setFont(Console.INSTANCE.generateUserFont());
        downloadProgressLabel.setForeground(CyderColors.vanilla);
        downloadProgressLabel.setHorizontalAlignment(JLabel.LEFT);
        downloadProgressLabel.setForeground(Console.INSTANCE.getInputField().getForeground());
        downloadProgressLabel.setFont(Console.INSTANCE.getInputField().getFont());

        inputHandler.println(downloadProgressBar);
        inputHandler.println(downloadProgressLabel);
        inputHandler.println(getCancelDownloadButton());
    }

    /**
     * Returns a button which can be used to cancel and clean up the download.
     *
     * @return a button which can be used to cancel and clean up the download
     */
    private CyderButton getCancelDownloadButton() {
        cancelButton = new CyderButton();
        cancelButton.setLeftTextPadding(StringUtil.generateNSpaces(5));
        cancelButton.setRightTextPadding(StringUtil.generateNSpaces(4));
        cancelButton.setText(CANCEL);
        cancelButton.setFont(Console.INSTANCE.getInputField().getFont());
        cancelButton.addActionListener(e -> {
            if (!isCanceled()) {
                cancel();
                cancelButton.setText(CANCELED);
            }
        });

        return cancelButton;
    }

    /**
     * Cleans up the printed ui elements.
     */
    private void cleanUpUi() {
        Color resultColor = downloaded ? CyderColors.regularBlue : CyderColors.regularRed;

        downloadProgressBarUi.setAnimationColor(resultColor);
        downloadProgressBarUi.stopAnimationTimer();

        downloadProgressBar.repaint();

        String buttonText;
        if (downloaded) {
            buttonText = DOWNLOADED;
        } else if (canceled) {
            buttonText = CANCELED;
        } else {
            buttonText = FAILED;
        }
        cancelButton.setText(buttonText);
    }

    /**
     * Cleans up .part or any other files left over by youtube-dl after the user canceled the download.
     *
     * @param parentDirectory      the directory the file would have been downloaded to
     * @param nameWithoutExtension the file name without the extension, anything that starts with this will be deleted
     */
    private void cleanUpFromCancel(File parentDirectory, String nameWithoutExtension) {
        Preconditions.checkNotNull(parentDirectory);
        Preconditions.checkNotNull(nameWithoutExtension);
        Preconditions.checkNotNull(nameWithoutExtension);
        Preconditions.checkArgument(!nameWithoutExtension.isEmpty());

        File[] children = parentDirectory.listFiles();
        if (children == null || children.length == 0) return;

        Arrays.stream(children).filter(child -> FileUtil.getFilename(child).startsWith(nameWithoutExtension))
                .forEach(child -> {
                    if (!OsUtil.deleteFile(child)) {
                        Logger.log(LogTag.DEBUG, "Could not delete file resulting from youtube "
                                + "download operation canceled, location=" + parentDirectory.getAbsolutePath()
                                + ", name=" + nameWithoutExtension);
                    }
                });
    }
}
