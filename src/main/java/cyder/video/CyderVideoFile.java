package cyder.video;

import com.google.common.base.Preconditions;
import cyder.audio.ffmpeg.FfmpegArgument;
import cyder.audio.ffmpeg.FfmpegCommandBuilder;
import cyder.audio.ffmpeg.FfmpegLogLevel;
import cyder.audio.validation.SupportedAudioFileType;
import cyder.exceptions.FatalException;
import cyder.files.FileUtil;
import cyder.process.ProcessResult;
import cyder.process.ProcessUtil;
import cyder.threads.CyderThreadFactory;
import cyder.threads.ThreadUtil;
import cyder.ui.frame.CyderFrame;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * An encapsulated supported video file for performing operations on.
 * See {@link SupportedVideoFormat} for the formats possible for encapsulation.
 */
public final class CyderVideoFile {
    /**
     * The encapsulated video file.
     */
    private final File videoFile;

    /**
     * The suffix to be appended to the filename if the audio is
     * extracted from this video file via {@link #extractAudio(SupportedAudioFileType)}.
     */
    private String audioExtractionSuffix = "_audio";

    /**
     * Constructs a new CyderVideoFileObject.
     *
     * @param videoFile the video file
     * @throws NullPointerException     if the provided file is null
     * @throws IllegalArgumentException if the provided file does not exist, is not a file, or is not a supported type
     */
    public CyderVideoFile(File videoFile) {
        Preconditions.checkNotNull(videoFile);
        Preconditions.checkArgument(videoFile.exists());
        Preconditions.checkArgument(videoFile.isFile());
        Preconditions.checkArgument(SupportedVideoFormat.isSupportedVideoFormat(videoFile));

        this.videoFile = videoFile;
    }

    /**
     * Sets the audio extraction suffix for this CyderVideoFile.
     *
     * @param suffix the audio extraction suffix to use
     */
    public CyderVideoFile setAudioExtractionSuffix(String suffix) {
        Preconditions.checkNotNull(suffix);
        Preconditions.checkArgument(!suffix.trim().isEmpty());

        this.audioExtractionSuffix = suffix;
        return this;
    }

    /**
     * Extracts the encapsulated video file's audio to a new file which will neighbor
     * the encapsulated video file. The file will be suffixed with "_audio" and be of the
     * provided format.
     *
     * @param fileType the audio file type to extract the video's audio to
     * @return the file the video's audio was extracted to
     * @throws NullPointerException if the provided file type is null
     */
    public Future<File> extractAudio(SupportedAudioFileType fileType) {
        Preconditions.checkNotNull(fileType);

        String absolutePath = videoFile.getAbsolutePath();
        FfmpegCommandBuilder builder = new FfmpegCommandBuilder()
                .addArgument(FfmpegArgument.LOG_LEVEL.getArgument(), FfmpegLogLevel.QUIET.getLogLevelName())
                .addArgument(FfmpegArgument.INPUT.getArgument(), absolutePath);

        switch (fileType) {
            case MP3 -> builder.addArgument(FfmpegArgument.AUDIO_QUALITY.getArgument(), "0")
                    .addArgument(FfmpegArgument.MAP.getArgument(), "a");
            case WAVE -> builder.addArgument(FfmpegArgument.MAP.getArgument(), "a");
            case OGG -> builder.addArgument(FfmpegArgument.AUDIO_CODE_C.getArgument(), "libvorbis")
                    .addArgument(FfmpegArgument.AUDIO_QUALITY.getArgument(), "4")
                    .addArgument(FfmpegArgument.MAP.getArgument(), "a");
            case M4A -> builder.addArgument(FfmpegArgument.AUDIO_CODE_C.getArgument(), "acc")
                    .addArgument(FfmpegArgument.AUDIO_QUALITY.getArgument(), "100")
                    .addArgument(FfmpegArgument.MAP.getArgument(), "a");
            default -> throw new IllegalStateException("Invalid file type: " + fileType);
        }

        String outputFileName = FileUtil.getFilename(videoFile) + audioExtractionSuffix + fileType.getExtension();
        File uniqueFile = FileUtil.constructUniqueName(outputFileName, videoFile.getParentFile());
        builder.addArgument(uniqueFile.getAbsolutePath());

        CyderThreadFactory ctf = new CyderThreadFactory("todo");
        return Executors.newSingleThreadExecutor(ctf).submit(() -> {
            Future<ProcessResult> futureResult = ProcessUtil.getProcessOutput(builder.list());
            while (!futureResult.isDone()) Thread.onSpinWait();
            try {
                ProcessResult result = futureResult.get();
                if (result.hasErrors()) {
                    throw new FatalException("Failed to extract audio from "
                            + "\"" + videoFile.getAbsolutePath() + "\""
                            + " to \"" + uniqueFile.getAbsolutePath() + "\"");
                }

                if (uniqueFile.exists()) {
                    return uniqueFile;
                } else {
                    throw new FatalException("");
                }
            } catch (InterruptedException | ExecutionException ignored) {

            }

            throw new FatalException("Failed to extract audio from "
                    + "\"" + videoFile.getAbsolutePath() + "\""
                    + " to \"" + uniqueFile.getAbsolutePath() + "\"");
        });
    }

    /**
     * Shows the video player for the encapsulated video using a {@link CyderFrame}.
     */
    public void showVideoPlayer() {
        // extract frames of video, partitioned or bulk, figure that out
        // ffmpeg -i BadApple.mp4 "%04d.png"
        // extract audio to mp3
        // compute milliseconds per frame needed to make the video last as long as the audio does
        // have audio playing and start moving through frames
        // need to keep track of how far ahead/behind the audio is so that we can sleep for more/less
        // to catch up
    }
}
