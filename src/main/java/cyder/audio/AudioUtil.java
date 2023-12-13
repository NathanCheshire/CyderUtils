package cyder.audio;

import com.google.common.base.Preconditions;
import cyder.audio.exceptions.AudioException;
import cyder.enumerations.Extension;
import cyder.files.FileUtil;
import cyder.threads.CyderThreadFactory;
import cyder.utils.OsUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
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
     * Converts the mp3 file to a wav file and returns the file object.
     * Note the file is created in the Cyder temporary directory which is
     * removed upon proper Cyder shutdown/startup.
     *
     * @param mp3File the mp3 file to convert to wav
     * @return the mp3 file converted to wav
     */
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public static Future<Optional<File>> mp3ToWav(File mp3File) {
        Preconditions.checkNotNull(mp3File);
        Preconditions.checkArgument(FileUtil.validateExtension(mp3File, Extension.MP3.getExtension()));

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory("Mp3 to wav converter")).submit(() -> {
            File tmpDir = new File(""); // todo
            if (!tmpDir.exists()) {
                tmpDir.mkdir();
            }

            String builtPath = FileUtil.getFilename(mp3File) + Extension.WAV.getExtension();
            String safePath = "\"" + builtPath + "\"";

            File outputFile = new File(builtPath);
            if (outputFile.exists()) {
                if (!OsUtil.deleteFile(outputFile)) {
                    throw new AudioException("Output file already exists in temp directory");
                }
            }

            ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg", INPUT_FLAG,
                    "\"" + mp3File.getAbsolutePath() + "\"", safePath);
            processBuilder.redirectErrorStream();
            Process process = processBuilder.start();

            // another precaution to ensure process is completed before file is returned
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while (reader.readLine() != null) {
                Thread.onSpinWait();
            }

            // wait for file to be created by ffmpeg
            while (!outputFile.exists()) {
                Thread.onSpinWait();
            }

            return Optional.of(outputFile);
        });
    }

    /**
     * Converts the wav file to an mp3 file and returns the file object.
     * Note the file is created in the Cyder temporary directory which is
     * removed upon proper Cyder shutdown/startup.
     *
     * @param wavFile the wav file to convert to mp3
     * @return the wav file converted to mp3
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static Future<Optional<File>> wavToMp3(File wavFile) {
        Preconditions.checkNotNull(wavFile);
        Preconditions.checkArgument(FileUtil.validateExtension(wavFile, Extension.WAV.getExtension()));

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory("Wav to mp3 converter")).submit(() -> {

            String builtPath = ""; // todo temp dir
            String safePath = "\"" + builtPath + "\"";

            File outputFile = new File(builtPath);
            ProcessBuilder pb = new ProcessBuilder("ffmpeg", INPUT_FLAG,
                    "\"" + wavFile.getAbsolutePath() + "\"", safePath);
            pb.redirectErrorStream();
            Process process = pb.start();

            // another precaution to ensure process is completed before file is returned
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while (reader.readLine() != null) Thread.onSpinWait();

            // wait for file to be created by ffmpeg
            while (!outputFile.exists()) {
                Thread.onSpinWait();
            }

            return Optional.of(outputFile);
        });
    }

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
