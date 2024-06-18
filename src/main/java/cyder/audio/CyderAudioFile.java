package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.audio.validation.SupportedAudioFileType;
import cyder.files.CyderTemporaryFile;
import cyder.files.FileUtil;
import cyder.process.CyderProcessException;
import cyder.process.ProcessResult;
import cyder.process.ProcessUtil;
import cyder.threads.CyderThreadFactory;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A Cyder wrapper class around a {@link java.io.File} of a supported audio type, as defined by
 * {@link SupportedAudioFileType#isSupported(File)}, for performing certain operations or mutations.
 */
public final class CyderAudioFile {
    /**
     * The highpass value for dreamifying an audio file.
     */
    private static final int DREAMIFY_HIGH_PASS = 200;

    /**
     * The low pass value for dreamifying an audio file.
     */
    private static final int DREAMIFY_LOW_PASS = 1500;

    /**
     * The encapsulated audio file.
     */
    private final File audioFile;

    /**
     * The highpass value for the dreamify ffmpeg filter.
     */
    private final int dreamifyHighPass;

    /**
     * The lowpass value for the dreamify ffmpeg filter.
     */
    private final int dreamifyLowPass;

    /**
     * Returns a new instance of a CyderAudioFile from the provided filepath.
     *
     * @param filepath the filepath to construct the audio file from
     * @return a new instance of a CyderAudioFile from the provided filepath
     * @throws NullPointerException if the provided filepath is null
     * @throws IllegalArgumentException if the provided filepath is empty
     */
    public static CyderAudioFile from(String filepath) {
        Preconditions.checkNotNull(filepath);
        Preconditions.checkArgument(!filepath.isEmpty());

        return new CyderAudioFile(new File(filepath));
    }

    /**
     * Returns a new instance of a CyderAudioFile from the provided file.
     *
     * @param audioFile the audio file to use
     * @return a new instance of a CyderAudioFile from the provided file
     * @throws NullPointerException if the provided file is empty
     * @throws IllegalArgumentException if the provided file does not exist
     */
    public static CyderAudioFile from(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.isFile());

        return new CyderAudioFile(audioFile);
    }

    /**
     * Constructs a new CyderAudioFile.
     *
     * @param audioFile the audio file
     * @throws NullPointerException     if the provided audio file is null
     * @throws IllegalArgumentException if the provided audio file does not
     *                                  exist or is not a file or is not a supported audio type
     */
    public CyderAudioFile(File audioFile) {
        this(audioFile, DREAMIFY_HIGH_PASS, DREAMIFY_LOW_PASS);
    }

    /**
     * Constructs a new CyderAudioFile.
     *
     * @param audioFile the audio file
     * @throws NullPointerException     if the provided audio file is null
     * @throws IllegalArgumentException if the provided audio file does not
     *                                  exist or is not a file or is not a supported audio type
     *                                  or the high pass is less than or equal to the low pass
     */
    public CyderAudioFile(File audioFile, int dreamifyHighPass, int dreamifyLowPass) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(audioFile.isFile());
        Preconditions.checkArgument(SupportedAudioFileType.isSupported(audioFile));
        Preconditions.checkArgument(dreamifyHighPass > dreamifyLowPass);

        this.audioFile = audioFile;
        this.dreamifyHighPass = dreamifyHighPass;
        this.dreamifyLowPass = dreamifyLowPass;
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

        CyderTemporaryFile temporaryConversionFile = new CyderTemporaryFile.Builder()
                .setOutputFilename(newName)
                .setOutputExtension(audioFileType.getExtension())
                .build();

        List<String> process = ImmutableList.of(
                "ffmpeg", "-y", "-i", this.audioFile.getAbsolutePath(),
                temporaryConversionFile.buildFile().getAbsolutePath());

        String threadName = "CyderAudioFile.convertTo process";
        return Executors.newSingleThreadExecutor(new CyderThreadFactory(threadName)).submit(() -> {
            Future<ProcessResult> futureResult = ProcessUtil.getProcessOutput(process);
            while (!futureResult.isDone()) Thread.onSpinWait();

            try {
                ProcessResult result = futureResult.get();

                if (result.hasErrors()) throw new CyderProcessException("CyderAudioFile.convertTo process"
                        + " result contains errors: " + result.getErrorOutput());
                return new CyderAudioFile(temporaryConversionFile.buildFile());
            } catch (Exception e) {
                throw new CyderProcessException(e);
            }
        });
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
     * Returns the highpass lowpass filter commands to use in combination with ffmpeg
     * to "dreamify" an audio file.
     *
     * @return the highpass lowpass filter commands string
     */
    private String constructHighpassLowpassFilter() {
        return "\"" + "highpass=f=" + dreamifyHighPass + ", " + "lowpass=f=" + dreamifyLowPass + "\"";
    }

    /**
     * Dreamifies this audio file and returns a new audio file representing the newly created dreamified audio file.
     *
     * @return the dreamified audio file
     */
    public Future<Optional<CyderAudioFile>> dreamify() {
        String executorThreadName = "CyderAudioFile dreamifier, " + this;
        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory(executorThreadName)).submit(() -> {

            String safeFilename = "\"" + audioFile.getAbsolutePath() + "\"";

            File outputFile = FileUtil.addSuffixToFilename(audioFile, "_dreamy");
            String safeOutputFilename = "\"" + outputFile.getAbsolutePath() + "\"";

            String[] command = {
                    "ffmpeg",
                    "-i",
                    safeFilename,
                    "-filter:a",
                    constructHighpassLowpassFilter(),
                    safeOutputFilename
            };
            Future<ProcessResult> result = ProcessUtil.getProcessOutput(command);
            while (!result.isDone()) Thread.onSpinWait();

            CyderAudioFile ret = new CyderAudioFile(outputFile);
            return Optional.of(ret);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof CyderAudioFile)) {
            return false;
        }

        CyderAudioFile other = (CyderAudioFile) o;
        return this.audioFile.equals(other.audioFile)
                && this.dreamifyLowPass == other.dreamifyLowPass
                && this.dreamifyHighPass == other.dreamifyHighPass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "CyderAudioFile{"
                + "audioFile=" + audioFile + ", "
                + "dreamifyLowPass=" + dreamifyLowPass + ", "
                + "dreamifyHighPass=" + dreamifyHighPass
                + "}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = audioFile.hashCode();
        ret = 31 * ret + Integer.hashCode(dreamifyLowPass);
        ret = 31 * ret + Integer.hashCode(dreamifyHighPass);
        return ret;
    }
}
