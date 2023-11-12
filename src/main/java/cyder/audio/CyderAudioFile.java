package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.io.File;

/**
 * A Cyder wrapper class around a {@link java.io.File} of a supported audio type, as defined by
 * {@link SupportedAudioFileType#isSupported(File)}, for performing certain operations or mutations.
 */
public final class CyderAudioFile {
    /**
     * The encapsulated audio file.
     */
    private final File audioFile;

    /**
     * Constructs a new CyderAudioFile.
     *
     * @param audioFile the audio file
     * @throws NullPointerException     if the provided audio file is null
     * @throws IllegalArgumentException if the provided audio file does not
     *                                  exist or is not a file or is not a supported audio type
     */
    public CyderAudioFile(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(audioFile.isFile());
        Preconditions.checkArgument(SupportedAudioFileType.isSupported(audioFile));

        this.audioFile = audioFile;
    }


    @CanIgnoreReturnValue
    public File convertTo(SupportedAudioFileType audioFileType) {
        String newName = "todo";
        return convertTo(audioFileType, newName);
    }

    @CanIgnoreReturnValue
    public File convertTo(SupportedAudioFileType audioFileType, String newName) {

    }


    public enum AudioLen

    // todo get length via some method (enum for method)
    public File dreamify() {

    }

    public File dreamify(String filename) {

    }
}