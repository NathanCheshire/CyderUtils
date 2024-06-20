package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.audio.validation.SupportedAudioFileType;
import cyder.audio.wav.WaveFile;
import cyder.enumerations.Extension;
import cyder.files.CyderTemporaryFile;
import cyder.files.FileUtil;
import cyder.process.CyderProcessException;
import cyder.process.ProcessResult;
import cyder.process.ProcessUtil;
import cyder.strings.StringUtil;
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
     * The default highpass value for dreamifying an audio file.
     */
    private static final int DEFAULT_DREAMIFY_HIGH_PASS = 1500;

    /**
     * The default low pass value for dreamifying an audio file.
     */
    private static final int DEFAULT_DREAMIFY_LOW_PASS = 200;

    /**
     * The encapsulated audio file.
     */
    private final File audioFile;

    /**
     * The highpass value for the dreamify ffmpeg filter.
     */
    private int dreamifyHighPass;

    /**
     * The lowpass value for the dreamify ffmpeg filter.
     */
    private int dreamifyLowPass;

    /**
     * The directory to output files to such as converted and dreamified files.
     */
    private File outputDirectory;

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

    private CyderAudioFile(Builder builder) {
        this(builder.audioFie, builder.highpass, builder.lowpass);
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
        this(audioFile, DEFAULT_DREAMIFY_HIGH_PASS, DEFAULT_DREAMIFY_LOW_PASS);
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
        this.outputDirectory = audioFile.getParentFile();
    }

    /**
     * Sets the dreamify highpass filter value.
     *
     * @param highPass the dreamify highpass filter value
     * @throws IllegalArgumentException if the provided highpass is less than or equal to the lowpass
     */
    public void setDreamifyHighPass(int highPass) {
        Preconditions.checkArgument(highPass > dreamifyLowPass);
        this.dreamifyHighPass = highPass;
    }

    /**
     * Sets the dreamify lowpass filter value.
     *
     * @param lowpass the dreamify highpass filter value
     * @throws IllegalArgumentException if the provided lowpass is greater than or equal to the highpass
     */
    public void setDreamifyLowPass(int lowpass) {
        Preconditions.checkArgument(lowpass < dreamifyHighPass);
        this.dreamifyLowPass = lowpass;
    }

    /**
     * Sets the directory to output files to such as in the case of dreamify or convert to.
     * See {@link #dreamify()} and {@link #convertTo(SupportedAudioFileType)}.
     *
     * @param outputDirectory the directory to output files to
     * @throws NullPointerException if the provided file is null
     * @throws IllegalArgumentException if the provided file is not a directory or does not exist
     */
    public void setOutputDirectory(File outputDirectory) {
        Preconditions.checkNotNull(outputDirectory);
        Preconditions.checkArgument(outputDirectory.isDirectory());
        Preconditions.checkArgument(outputDirectory.exists());

        this.outputDirectory = outputDirectory;
    }

    /**
     * Converts the internal audio file from the current format to the provided format.
     * The file will be named to the new name plus the new extension and will exist
     * in the same directory as this file.
     *
     * @param audioFileType the audio file type to convert to
     * @return the converted file
     * @throws NullPointerException if the provided audio file new name is null
     */
    @CanIgnoreReturnValue
    public Future<CyderAudioFile> convertTo(SupportedAudioFileType audioFileType) {
        Preconditions.checkNotNull(audioFileType);

        CyderTemporaryFile temporaryConversionFile = new CyderTemporaryFile.Builder()
                .setOutputFilename(FileUtil.getFilename(audioFile))
                .setOutputExtension(audioFileType.getExtension())
                .setOutputDirectory(outputDirectory)
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
                // FFmpeg uses the error stream for process output,
                // so we do not care about it containing errors here
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
     * @throws NullPointerException if the provided method is null
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

            String safeFilename = StringUtil.surroundWithQuotes(audioFile.getAbsolutePath());

            String filename = FileUtil.getFilename(audioFile)
                    + "_dreamy" + FileUtil.getExtension(audioFile);
            File outputFile = new File(outputDirectory, filename);
            String safeOutputFilename = StringUtil.surroundWithQuotes(outputFile.getAbsolutePath());

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
     * Returns a new instance of {@link WaveFile} from the internal audio file.
     *
     * @return a new WaveFile instance
     * @throws IllegalStateException if this audio file is not a wave file
     */
    public WaveFile toWaveFile() {
        Preconditions.checkState(FileUtil.validateExtension(audioFile, Extension.WAV.getExtension()));

        return new WaveFile(audioFile);
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

    /**
     * A builder for constructing new instances of a {@link CyderAudioFile}.
     */
    public static class Builder {
        /**
         * The highpass value for the dreamify ffmpeg filter.
         */
        private int highpass = DEFAULT_DREAMIFY_HIGH_PASS;

        /**
         * The lowpass value for the dreamify ffmpeg filter.
         */
        private int lowpass = DEFAULT_DREAMIFY_LOW_PASS;

        /**
         * The encapsulated audio file.
         */
        private final File audioFie;

        /**
         * The directory to output files to such as converted and dreamified files.
         */
        private File outputDirectory;

        /**
         * Constructs a new CyderAudioFileBuilder.
         *
         * @param audioFile the audio file
         * @throws NullPointerException if the audio file is null
         * @throws IllegalArgumentException if the audio file does not exist, is not a file, or is not supported
         */
        public Builder(File audioFile) {
            Preconditions.checkNotNull(audioFile);
            Preconditions.checkArgument(audioFile.exists());
            Preconditions.checkArgument(audioFile.isFile());
            Preconditions.checkArgument(SupportedAudioFileType.isSupported(audioFile));

            this.audioFie = audioFile;
        }

        /**
         * Sets the highpass value for the dreamify ffmpeg filter.
         *
         * @param highpass the highpass value for the dreamify ffmpeg filter
         * @return this builder
         * @throws IllegalArgumentException if the provided highpass is less than or equal to the lowpass
         */
        public Builder setHighpass(int highpass) {
            Preconditions.checkArgument(highpass > lowpass);
            this.highpass = highpass;
            return this;
        }

        /**
         * Sets the lowpass value for the dreamify ffmpeg filter.
         *
         * @param lowpass the lowpass value for the dreamify ffmpeg filter
         * @return this builder
         * @throws IllegalArgumentException if the provided lowpass is greater than or equal to the highpass
         */
        public Builder setLowpass(int lowpass) {
            Preconditions.checkArgument(lowpass < highpass);
            this.lowpass = lowpass;
            return this;
        }

        /**
         * Sets the directory to output files to such as converted and dreamified files.
         *
         * @param outputDirectory the directory to output files to such as converted and dreamified files
         * @return this builder
         * @throws NullPointerException if the provided file is null
         * @throws IllegalArgumentException if the provided file is a directory or does not exist
         */
        public Builder setOutputDirectory(File outputDirectory) {
            Preconditions.checkNotNull(outputDirectory);
            Preconditions.checkArgument(outputDirectory.isDirectory());
            Preconditions.checkArgument(outputDirectory.exists());

            this.outputDirectory = outputDirectory;
            return this;
        }

        /**
         * Constructs a new CyderAudioFile using this builder.
         *
         * @return a new CyderAudioFile
         */
        public CyderAudioFile build() {
            CyderAudioFile ret = new CyderAudioFile(this);
            ret.setOutputDirectory(outputDirectory);
            return ret;
        }
    }
}
