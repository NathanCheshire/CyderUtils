package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.annotations.ForReadability;
import cyder.audio.ffmpeg.*;
import cyder.audio.parsers.ShowStreamOutput;
import cyder.audio.validation.SupportedAudioFileType;
import cyder.constants.CyderRegexPatterns;
import cyder.files.CyderTemporaryFile;
import cyder.files.FileUtil;
import cyder.process.*;
import cyder.strings.StringUtil;
import cyder.threads.CyderThreadFactory;
import cyder.time.TimeUtil;
import cyder.utils.SerializationUtil;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * The methods Cyder supports for extracting the audio length from a {@link SupportedAudioFileType}.
 */
public enum DetermineAudioLengthMethod {
    /**
     * Determine an audio file's length using ffmpeg.
     */
    FFMPEG(DetermineAudioLengthMethod::getLengthViaFfmpeg),

    /**
     * Determine an audio file's length using the Python package Mutagen.
     */
    PYTHON_MUTAGEN(DetermineAudioLengthMethod::getLengthViaMutagen);

    private final Function<File, Future<Duration>> audioLengthComputationFunction;

    DetermineAudioLengthMethod(Function<File, Future<Duration>> audioLengthComputationFunction) {
        this.audioLengthComputationFunction = audioLengthComputationFunction;
    }

    /**
     * Determines the audio length of the provided audio file using this method.
     *
     * @param audioFile the audio file
     * @return the duration of the audio file
     * @throws NullPointerException     if the provided file is null
     * @throws IllegalArgumentException if the provided file does not exist,
     *                                  is not a file, or is not a supported audio type
     */
    public Future<Duration> determineAudioLength(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.isFile());
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(SupportedAudioFileType.isSupported(audioFile));

        return audioLengthComputationFunction.apply(audioFile);
    }

    /**
     * Computes the length of the provided audio file using ffmpeg.
     *
     * @param audioFile the audio file
     * @return the length of the audio file
     * @throws NullPointerException     if the provided audio file is null
     * @throws IllegalArgumentException if the provided audio file is not a file,
     *                                  does not exist, or is not a supported audio type
     * @throws CyderProcessException    if the process fails to determine the audio length
     */
    private static Future<Duration> getLengthViaFfmpeg(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.isFile());
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(SupportedAudioFileType.isSupported(audioFile));

        CyderThreadFactory threadFactory = getThreadFactory(DetermineAudioLengthMethod.FFMPEG, audioFile);
        return Executors.newSingleThreadExecutor(threadFactory).submit(() -> {
            String absolutePath = audioFile.getAbsolutePath();

            String command = new FfmpegCommandBuilder()
                    .addArgument(FfmpegArgument.LOG_LEVEL.getArgument(), FfmpegLogLevel.QUIET.getLogLevelName())
                    .addArgument(FfmpegArgument.PRINT_FORMAT.getArgument(), FfmpegPrintFormat.JSON.getFormatName())
                    .addArgument(FfmpegArgument.SHOW_STREAMS.getArgument())
                    .addArgument(FfmpegArgument.SHOW_ENTRIES.getArgument(),
                            FfmpegStreamEntry.DURATION.getStreamCommand())
                    .addArgument(StringUtil.surroundWithQuotes(absolutePath))
                    .build();

            Future<ProcessResult> futureResult = ProcessUtil.getProcessOutput(command);
            while (!futureResult.isDone()) Thread.onSpinWait();

            ProcessResult result = futureResult.get();
            if (result.hasErrors())
                throw new CyderProcessException("Process result contains errors: " + result.getErrorOutput());

            String joinedOutput = StringUtil.joinParts(result.getStandardOutput(), "");
            String trimmedOutput = joinedOutput.replaceAll(CyderRegexPatterns.multipleWhiteSpaceRegex, "");
            ShowStreamOutput output = SerializationUtil.fromJson(trimmedOutput, ShowStreamOutput.class);

            String millisPropertyString = output.getStreams().get(0).getDuration();
            double seconds = Double.parseDouble(millisPropertyString);
            int millis = (int) (seconds * TimeUtil.millisInSecond);

            return Duration.ofMillis(millis);
        });
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Future<Duration> duration = getLengthViaMutagen(
                new File("/Users/nathancheshire/Downloads/TastyCarrots.mp3"));
        while (!duration.isDone()) Thread.onSpinWait();
        System.out.println(duration.get().getNano());
    }

    /**
     * Computes the length of the provided audio file using Mutagen.
     *
     * @param audioFile the audio file
     * @return the length of the audio file
     * @throws NullPointerException     if the provided audio file is null
     * @throws IllegalArgumentException if the provided audio file is not a file,
     *                                  does not exist, or is not a supported audio type
     * @throws CyderProcessException if {@link PythonPackage#MUTAGEN} is not and cannot be
     *                               installed or a valid working Python command cannot be found
     */
    private static Future<Duration> getLengthViaMutagen(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.isFile());
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(SupportedAudioFileType.isSupported(audioFile));

        CyderThreadFactory threadFactory = getThreadFactory(DetermineAudioLengthMethod.PYTHON_MUTAGEN, audioFile);
        return Executors.newSingleThreadExecutor(threadFactory).submit(() -> {
            Future<Boolean> mutagenInstalled = PythonPackage.MUTAGEN.isInstalled();
            while (!mutagenInstalled.isDone()) Thread.onSpinWait();;
            if (!mutagenInstalled.get()) {
                Future<Boolean> installationRequest = PythonPackage.MUTAGEN.install();
                while (!installationRequest.isDone()) Thread.onSpinWait();
                if (!installationRequest.get()) throw new CyderProcessException("Failed to install Mutagen");
            }

            ImmutableList<String> script = ImmutableList.of(
                    "import sys",
                    "from mutagen import File",
                    "audio_file = File(\"" + audioFile.getAbsolutePath() + "\")",
                    "print(audio_file.info.length)"
            );

            CyderTemporaryFile temporaryPythonScriptFile = new CyderTemporaryFile.Builder()
                    .setFilenameAndExtension("mutagen_length.py")
                    .build();
            temporaryPythonScriptFile.create();
            File scriptFile = temporaryPythonScriptFile.buildFile();
            FileUtil.writeLinesToFile(scriptFile, script, false);

            Future<Optional<String>> firstWorkingPythonInvocableCommand
                    = PythonProgram.PYTHON.getFirstWorkingProgramName();
            while (!firstWorkingPythonInvocableCommand.isDone()) Thread.onSpinWait();
            Optional<String> firstCommandOptional = firstWorkingPythonInvocableCommand.get();
            if (firstCommandOptional.isEmpty()) throw new CyderProcessException("Failed to find working Python command");

            Future<ProcessResult> mutagenLengthResult = ProcessUtil.getProcessOutput(
                    ImmutableList.of(firstCommandOptional.get(), scriptFile.getAbsolutePath()));
            while (!mutagenInstalled.isDone()) Thread.onSpinWait();
            ProcessResult result = mutagenLengthResult.get();

            if (result.hasErrors()) {
                throw new CyderProcessException("Mutagen length process result contains errors: " + result.getErrorOutput());
            }

            List<String> output = result.getStandardOutput();
            if (output.size() > 1) {
                throw new CyderProcessException("Mutagen length process result contains more than one line: " + output);
            }

            float seconds = Float.parseFloat(output.get(0));
            float millis = seconds * 1000.0f;
            return Duration.ofMillis(Math.round(millis));
        });
    }

    /**
     * Returns a new {@link CyderThreadFactory} for the {@link java.util.concurrent.ExecutorService}
     * used to determine the audio length using a particular method.
     *
     * @param method    the method
     * @param audioFile the audio file
     * @return a new CyderThreadFactory
     */
    @ForReadability
    private static CyderThreadFactory getThreadFactory(DetermineAudioLengthMethod method, File audioFile) {
        String absolutePath = audioFile.getAbsolutePath();
        String name = "DetermineAudioLengthMethodThread{"
                + "method=" + method.toString()
                + ", audioFile=\"" + absolutePath + "\""
                + "}";
        return new CyderThreadFactory(name);
    }
}
