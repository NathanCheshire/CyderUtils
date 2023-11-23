package cyder.video;

import com.google.common.base.Preconditions;
import cyder.audio.validation.SupportedAudioFileType;

import java.io.File;

public final class CyderVideoFile {
    private final File videoFile;

    public CyderVideoFile(File videoFile) {
        Preconditions.checkNotNull(videoFile);
        Preconditions.checkArgument(videoFile.exists());
        Preconditions.checkArgument(videoFile.isFile());
        Preconditions.checkArgument();

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
