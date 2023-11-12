package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.audio.parsers.ShowStreamOutput;
import cyder.constants.CyderRegexPatterns;
import cyder.enumerations.Dynamic;
import cyder.enumerations.Extension;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.network.NetworkUtil;
import cyder.process.ProcessResult;
import cyder.process.ProcessUtil;
import cyder.process.Program;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.threads.CyderThreadFactory;
import cyder.time.TimeUtil;
import cyder.utils.OsUtil;
import cyder.utils.SerializationUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static cyder.strings.CyderStrings.*;

/**
 * Utilities related to supported audio files.
 */
public final class AudioUtil {

    /**
     * The users keyword.
     */
    private static final String USERS = "users";

    /**
     * The music keyword.
     */
    private static final String MUSIC = "Music";

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
     * The lowpass value for dreamifying an audio file.
     */
    private static final int LOWPASS = 1500;

    /**
     * The audio dreamifier thread name prefix.
     */
    private static final String audioDreamifierThreadNamePrefix = "Audio Dreamifier: ";

    /**
     * The -filter:a flag for setting high and low pass data.
     */
    private static final String FILTER_DASH_A = "-filter:a";

    /**
     * The high and low pass argument string.
     */
    private static final String HIGHPASS_LOWPASS_ARGS = quote + "highpass=f="
            + HIGHPASS + comma + space + "lowpass=f=" + LOWPASS + quote;

    /**
     * The thread name for the ffmpeg downloader
     */
    private static final String FFMPEG_DOWNLOADER_THREAD_NAME = "FFMPEG Downloader";

    /**
     * A record to associate a destination file with a url to download the file, typically a zip archive, from.
     */
    private record PairedFile(File file, String url) {}

    /**
     * The name of the thread that downloads YouTube-dl if missing and needed.
     */
    private static final String YOUTUBE_DL_DOWNLOADER_THREAD_NAME = "YouTubeDl Downloader";

    /**
     * A cache of previously computed millisecond times from audio files.
     */
    private static final ConcurrentHashMap<File, Integer> milliTimes = new ConcurrentHashMap<>();

    /**
     * Suppress default constructor.
     */
    private AudioUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

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
            File tmpDir = Dynamic.buildDynamic(Dynamic.TEMP.getFileName());
            if (!tmpDir.exists()) {
                tmpDir.mkdir();
            }

            String builtPath = Dynamic.buildDynamic(Dynamic.TEMP.getFileName(),
                    FileUtil.getFilename(mp3File) + Extension.WAV.getExtension()).getAbsolutePath();
            String safePath = quote + builtPath + quote;

            File outputFile = new File(builtPath);
            if (outputFile.exists()) {
                if (!OsUtil.deleteFile(outputFile)) {
                    throw new FatalException("Output file already exists in temp directory");
                }
            }

            ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg", INPUT_FLAG,
                    quote + mp3File.getAbsolutePath() + quote, safePath);
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

            String builtPath = Dynamic.buildDynamic(Dynamic.TEMP.getFileName(),
                    FileUtil.getFilename(wavFile) + Extension.MP3.getExtension()).getAbsolutePath();
            String safePath = quote + builtPath + quote;

            File outputFile = new File(builtPath);
            ProcessBuilder pb = new ProcessBuilder("ffmpeg", INPUT_FLAG,
                    quote + wavFile.getAbsolutePath() + quote, safePath);
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
            String safeFilename = quote + wavOrMp3File.getAbsolutePath() + quote;

            File outputFile = Dynamic.buildDynamic(Dynamic.TEMP.getFileName(),
                    FileUtil.getFilename(wavOrMp3File) + DREAMY_SUFFIX + Extension.MP3.getExtension());
            String safeOutputFilename = quote + outputFile.getAbsolutePath() + quote;

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
     * Downloads ffmpeg, ffplay, and ffprobe to the exes dynamic
     * directory and sets the user path for ffmpeg to the one in dynamic.
     *
     * @return whether the download was successful
     */
    public static Future<Boolean> downloadFfmpegStack() {
        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory(FFMPEG_DOWNLOADER_THREAD_NAME)).submit(() -> {
            ImmutableList<PairedFile> downloadZips = ImmutableList.of(
                    new PairedFile(Dynamic.buildDynamic(
                            Dynamic.EXES.getFileName(), Program.FFMPEG.getProgramName()
                                    + Extension.ZIP.getExtension()), ffmpegResourceDownload),
                    new PairedFile(Dynamic.buildDynamic(
                            Dynamic.EXES.getFileName(), Program.FFPROBE.getProgramName()
                                    + Extension.ZIP.getExtension()), ffprobeResourceDownload),
                    new PairedFile(Dynamic.buildDynamic(
                            Dynamic.EXES.getFileName(), Program.FFPLAY.getProgramName()
                                    + Extension.ZIP.getExtension()), ffplayResourceDownload)
            );

            for (PairedFile pairedZipFile : downloadZips) {
                NetworkUtil.downloadResource(pairedZipFile.url(), pairedZipFile.file());

                while (!pairedZipFile.file().exists()) {
                    Thread.onSpinWait();
                }

                File extractFolder = Dynamic.buildDynamic(Dynamic.EXES.getFileName());
                FileUtil.unzip(pairedZipFile.file(), extractFolder);
                OsUtil.deleteFile(pairedZipFile.file());
            }

            ImmutableList<File> resultingFiles = ImmutableList.of(
                    Dynamic.buildDynamic(
                            Dynamic.EXES.getFileName(), Program.FFMPEG.getFilename()),
                    Dynamic.buildDynamic(
                            Dynamic.EXES.getFileName(), Program.FFPROBE.getFilename()),
                    Dynamic.buildDynamic(
                            Dynamic.EXES.getFileName(), Program.FFPLAY.getFilename())
            );

            return resultingFiles.stream().filter(File::exists).count() == downloadZips.size();
        });
    }

    /**
     * Downloads the YouTube-dl binary from the remote resources.
     * Returns whether the download was successful.
     *
     * @return whether YouTube-dl could be downloaded from the remote resources
     */
    public static Future<Boolean> downloadYoutubeDl() {
        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory(YOUTUBE_DL_DOWNLOADER_THREAD_NAME)).submit(() -> {
            File downloadZip = Dynamic.buildDynamic(
                    Dynamic.EXES.getFileName(), Program.YOUTUBE_DL.getProgramName()
                            + Extension.ZIP.getExtension());

            NetworkUtil.downloadResource(youtubeDlResourceDownload, downloadZip);
            while (!downloadZip.exists()) Thread.onSpinWait();

            File extractFolder = Dynamic.buildDynamic(Dynamic.EXES.getFileName());

            FileUtil.unzip(downloadZip, extractFolder);
            OsUtil.deleteFile(downloadZip);

            return Dynamic.buildDynamic(Dynamic.EXES.getFileName(),
                    Program.YOUTUBE_DL.getProgramName() + Extension.EXE.getExtension()).exists();
        });
    }

    /**
     * Returns the milliseconds of the provided audio file using ffprobe's -show_format command.
     * Note, this method is blocking. Callers should surround invocation of this method in a separate thread.
     *
     * @param audioFile the audio file
     * @return the milliseconds of the provided file
     * @throws ExecutionException   if the future task does not complete properly
     * @throws FatalException       if the process result contains errors
     * @throws InterruptedException if the thread was interrupted while waiting
     */
    public static Duration getLengthViaFfprobe(File audioFile) throws ExecutionException, InterruptedException {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(audioFile));

        if (milliTimes.containsKey(audioFile)) return Duration.ofMillis(milliTimes.get(audioFile));

        ImmutableList<String> command = ImmutableList.of(
                getFfprobeCommand(),
                "-v", "quiet",
                "-print_format", "json",
                "-show_streams",
                "-show_entries", "stream=duration",
                CyderStrings.quote + audioFile.getAbsolutePath() + CyderStrings.quote
        );
        Future<ProcessResult> futureResult = ProcessUtil.getProcessOutput(StringUtil.joinParts(command, space));
        while (!futureResult.isDone()) Thread.onSpinWait();
        ProcessResult result = futureResult.get();
        if (result.hasErrors()) throw new FatalException("Process result contains errors");
        String joinedOutput = StringUtil.joinParts(result.getStandardOutput(), "");
        String trimmedOutput = joinedOutput.replaceAll(CyderRegexPatterns.multipleWhiteSpaceRegex, "");
        ShowStreamOutput output = SerializationUtil.fromJson(trimmedOutput, ShowStreamOutput.class);
        String millisPropertyString = output.getStreams().get(0).getDuration();
        double seconds = Double.parseDouble(millisPropertyString);
        int millis = (int) (seconds * TimeUtil.millisInSecond);
        milliTimes.put(audioFile, millis);
        return Duration.ofMillis(millis);
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
