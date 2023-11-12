package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.files.FileUtil;

import java.io.File;
import java.time.Duration;

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

    /**
     * Converts the internal audio file from the current format to the provided format.
     * The file will be named the same as the original file but with the new extension.
     *
     * @param audioFileType the audio file type to convert to
     * @return the converted file
     * @throws NullPointerException if the provided audio file type is null
     * @throws IllegalArgumentException if the internal audio file is already of the provided type
     */
    @CanIgnoreReturnValue
    public File convertTo(SupportedAudioFileType audioFileType) {
        Preconditions.checkNotNull(audioFileType);
        Preconditions.checkArgument(!audioFileType.isAudioTypeofFile(audioFile));

        return convertTo(audioFileType, FileUtil.getFilename(audioFile));
    }

    /**
     * Converts the internal audio file from the current format to the provided format.
     * The file wil lbe named to the new name plus the new extension.
     *
     * @param audioFileType the audio file type to convert to
     * @param newName the new filename
     * @return the converted file
     * @throws NullPointerException if either the provided audio file type or new name are null
     * @throws IllegalArgumentException if the provided new name is invalid
     */
    @CanIgnoreReturnValue
    public File convertTo(SupportedAudioFileType audioFileType, String newName) {
        Preconditions.checkNotNull(audioFileType);
        Preconditions.checkNotNull(newName);
        Preconditions.checkArgument(FileUtil.isValidFilename(newName));

        // todo using ffmpeg, convert

        return null;
    }

    public Duration getAudioLength(DetermineAudioLengthMethod method) {

    }

    // todo get length via some method (enum for method)
    public File dreamify() {

    }

    public File dreamify(String filename) {

    }
}
