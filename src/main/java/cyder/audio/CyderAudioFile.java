package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.audio.validation.SupportedAudioFileType;
import cyder.enumerations.SystemPropertyKey;
import cyder.files.FileUtil;

import java.io.File;
import java.io.IOException;
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
     * The file will be named to the new name plus the new extension and will exist
     * in the same directory as this file.
     *
     * @param audioFileType the audio file type to convert to
     * @param newName the new filename
     * @return the converted file
     * @throws NullPointerException if either the provided audio file type or new name are null
     * @throws IllegalArgumentException if the provided new name is invalid
     */
    @CanIgnoreReturnValue
    public Future<CyderAudioFile> convertTo(SupportedAudioFileType audioFileType, String newName) {
        Preconditions.checkNotNull(audioFileType);
        Preconditions.checkNotNull(newName);
        Preconditions.checkArgument(FileUtil.isValidFilename(newName));

        // todo builder:
        //  need to allow placing in a specific folder with a specific name or generated name

        // todo using ffmpeg, convert

        return null;
    }

    public static void main(String[] args) throws IOException {
        File myFile = new File(SystemPropertyKey.JAVA_IO_TMPDIR.getProperty() + "my_file.txt");
        System.out.println("Exists: " + myFile.exists());
        myFile.createNewFile();
        System.out.println("Exists: " + myFile.exists());
        myFile.delete();
        System.out.println("Exists: " + myFile.exists());
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

    /**
     * Dreamifies this audio file and returns a new audio file representing the newly created dreamified audio file.
     *
     * @return the dreamified audio file
     */
    public Future<CyderAudioFile> dreamify() {
        return null; // todo, should have a dreamifier util that this can invoke?
    }

    /**
     * Dreamifies the encapsulated file and returns a reference to the new dreamified audio file.
     *
     * @return returns the dreamified audio file
     */
    public Future<CyderAudioFile> dreamify(File saveToFile) {
        return Futures.immediateFuture(null);
    }
}
