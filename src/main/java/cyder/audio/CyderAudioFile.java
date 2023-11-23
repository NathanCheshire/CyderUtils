package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.audio.validation.SupportedAudioFileType;
import cyder.files.FileUtil;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.Future;

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

    /**
     * Returns the length of the encapsulated audio file using the provided method.
     *
     * @param method the audio length computation method
     * @return the length of the audio file
     */
    public Future<Duration> getAudioLength(DetermineAudioLengthMethod method) {
        Preconditions.checkNotNull(method);
        return method.determineAudioLength(audioFile);
    }

    public File dreamify() {
        return null; // todo, should have a dreamifier util that this can invoke?
    }

    /**
     * Dreamifies the encapsulated file and returns a reference to the new dreamified audio file.
     *
     * @return returns the dreamified audio file
     */
    public Future<File> dreamify(File saveToFile) {
        return Futures.immediateFuture(null);

        // todo figure out name and pass to other method which accepts an empty file pointer for
        //  which we should use create the file for and save it to
    }
}
