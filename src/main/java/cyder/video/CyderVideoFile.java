package cyder.video;

import com.google.common.base.Preconditions;
import cyder.audio.ffmpeg.FfmpegArgument;
import cyder.audio.ffmpeg.FfmpegCommandBuilder;
import cyder.audio.validation.SupportedAudioFileType;
import cyder.files.FileUtil;
import cyder.strings.StringUtil;
import cyder.ui.frame.CyderFrame;
import cyder.utils.OsUtil;

import java.io.File;
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

        String absolutePath = StringUtil.surroundWithQuotes(videoFile.getAbsolutePath());
        FfmpegCommandBuilder builder = new FfmpegCommandBuilder()
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

        String suffix = "_audio";
        String outputFileName = FileUtil.getFilename(videoFile) + suffix + fileType.getExtension();
        File uniqueFile = FileUtil.constructUniqueName(outputFileName, videoFile.getParentFile());
        builder.addArgument(uniqueFile.getAbsolutePath());

        String conversionCommand = builder.build();
        // todo process invoke and wait for, then find file with the name and wait return after future is complete

        return null;
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
