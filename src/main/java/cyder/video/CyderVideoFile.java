package cyder.video;

import com.google.common.base.Preconditions;
import cyder.audio.validation.SupportedAudioFileType;

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
        return null;
    }
}
