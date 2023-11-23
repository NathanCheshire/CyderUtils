package cyder.video;

import com.google.common.base.Preconditions;
import cyder.audio.validation.SupportedAudioFileType;
import cyder.ui.frame.CyderFrame;

import java.io.File;

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
    public File extractAudio(SupportedAudioFileType fileType) {
        Preconditions.checkNotNull(fileType);

        switch (fileType) {
            case MP3 -> {
                String command = "ffmpeg -i input.mp4 -q:a 0 -map a output.mp3";
            }
            case WAVE -> {
                String command = "ffmpeg -i input.mp4 -map a output.wav";
            }
            case OGG -> {
                String command = "ffmpeg -i input.mp4 -c:a libvorbis -q:a 4 -map a output.ogg";
            }
            case M4A -> {
                String command = "ffmpeg -i input.mp4 -c:a aac -q:a 100 -map a output.m4a";
            }
        }

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
