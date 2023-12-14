package cyder.audio;

import com.google.common.base.Preconditions;
import cyder.enumerations.Extension;
import cyder.files.FileUtil;
import cyder.threads.CyderThreadFactory;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Utilities related to supported audio files.
 */
public final class AudioUtil {
    /**
     * The ffmpeg input flag.
     */
    private static final String INPUT_FLAG = "-i";

    /**
     * The dreamified file suffix to append to music files after dreamifying them.
     */
    public static final String DREAMY_SUFFIX = "_Dreamy";

    /**
     * The highpass value for dreamifying an audio file.
     */
    private static final int HIGHPASS = 200;

    /**
     * The low pass value for dreamifying an audio file.
     */
    private static final int LOW_PASS = 1500;

    /**
     * The audio dreamier thread name prefix.
     */
    private static final String audioDreamifierThreadNamePrefix = "Audio Dreamifier: ";

    /**
     * The -filter:a flag for setting high and low pass data.
     */
    private static final String FILTER_DASH_A = "-filter:a";

    /**
     * The high and low pass argument string.
     */
    private static final String HIGHPASS_LOWPASS_ARGS = "\"" + "highpass=f="
            + HIGHPASS + ", " + "lowpass=f=" + LOW_PASS + "\"";

    /**
     * Dreamifies the provided wav or mp3 audio file.
     * The optional may be empty if the file could not
     * be converted if required and processed.
     *
     * @param wavOrMp3File the old file to dreamify
     * @return the dreamified wav or mp3 file
     */
    public static Future<Optional<File>> dreamifyAudio(File wavOrMp3File) {
        Preconditions.checkNotNull(wavOrMp3File);
        Preconditions.checkArgument(wavOrMp3File.exists());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(wavOrMp3File));

        String executorThreadName = audioDreamifierThreadNamePrefix + FileUtil.getFilename(wavOrMp3File);

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory(executorThreadName)).submit(() -> {

            // in case the audio wav name contains spaces, surround with quotes
            String safeFilename = "\"" + wavOrMp3File.getAbsolutePath() + "\"";

            File outputFile = new File(FileUtil.getFilename(wavOrMp3File)
                    + DREAMY_SUFFIX + Extension.MP3.getExtension());
            String safeOutputFilename = "\"" + outputFile.getAbsolutePath() + "\"";

            String[] command = {
                    "ffmpeg",
                    INPUT_FLAG,
                    safeFilename,
                    FILTER_DASH_A,
                    HIGHPASS_LOWPASS_ARGS,
                    safeOutputFilename};
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            /*
            Audio length might change from ffmpeg high and low pass filters.
            Thus we don't check for the same length, todo this seems wrong we should use Mutagen
             */
            while (!outputFile.exists()) Thread.onSpinWait();
            process.waitFor();

            int exitValue = process.exitValue();
            if (exitValue != 0) {
                return Optional.empty();
            }

            return Optional.of(outputFile);
        });
    }

    /**
     * Return the first mp3 file found on the host operating system if present. Empty optional else.
     * Note this method will check the Music directory which varies in absolute path location depending
     * on the host operating system.
     *
     * @return the first mp3 file found on the host operating system if present. Empty optional else
     */
    public static Optional<File> getFirstMp3File() {
        return Optional.empty(); // todo
    }
}
