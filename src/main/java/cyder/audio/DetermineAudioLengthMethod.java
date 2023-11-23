package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.annotations.ForReadability;
import cyder.audio.ffmpeg.FfmpegArgument;
import cyder.audio.ffmpeg.FfmpegLogLevel;
import cyder.audio.ffmpeg.FfmpegPrintFormat;
import cyder.audio.ffmpeg.FfmpegStreamEntry;
import cyder.audio.parsers.ShowStreamOutput;
import cyder.audio.validation.SupportedAudioFileType;
import cyder.constants.CyderRegexPatterns;
import cyder.exceptions.FatalException;
import cyder.process.ProcessResult;
import cyder.process.ProcessUtil;
import cyder.strings.StringUtil;
import cyder.threads.CyderThreadFactory;
import cyder.time.TimeUtil;
import cyder.utils.SerializationUtil;

import java.io.File;
import java.time.Duration;
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
     * @throws FatalException           if the process fails to determine the audio length
     */
    private static Future<Duration> getLengthViaFfmpeg(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.isFile());
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(SupportedAudioFileType.isSupported(audioFile));

        CyderThreadFactory threadFactory = getThreadFactory(DetermineAudioLengthMethod.FFMPEG, audioFile);
        return Executors.newSingleThreadExecutor(threadFactory).submit(() -> {
            String absolutePath = audioFile.getAbsolutePath();
            // todo ffmpeg command builder class?
            ImmutableList<String> command = ImmutableList.of(
                    FfmpegArgument.FFMPEG.getArgumentName(),
                    FfmpegArgument.LOG_LEVEL.getArgument(), FfmpegLogLevel.QUIET.getLogLevelName(),
                    FfmpegArgument.PRINT_FORMAT.getArgument(), FfmpegPrintFormat.JSON.getFormatName(),
                    FfmpegArgument.SHOW_STREAMS.getArgument(),
                    FfmpegArgument.SHOW_ENTRIES.getArgument(), FfmpegStreamEntry.DURATION.getStreamCommand(),
                    StringUtil.surroundWithQuotes(absolutePath)
            );
            String joinedCommand = StringUtil.joinParts(command, " ");
            Future<ProcessResult> futureResult = ProcessUtil.getProcessOutput(joinedCommand);
            while (!futureResult.isDone()) Thread.onSpinWait();

            ProcessResult result = futureResult.get();
            if (result.hasErrors()) throw new FatalException("Process result contains errors");

            String joinedOutput = StringUtil.joinParts(result.getStandardOutput(), "");
            String trimmedOutput = joinedOutput.replaceAll(CyderRegexPatterns.multipleWhiteSpaceRegex, "");
            ShowStreamOutput output = SerializationUtil.fromJson(trimmedOutput, ShowStreamOutput.class);

            String millisPropertyString = output.getStreams().get(0).getDuration();
            double seconds = Double.parseDouble(millisPropertyString);
            int millis = (int) (seconds * TimeUtil.millisInSecond);

            return Duration.ofMillis(millis);
        });
    }

    /**
     * Computes the length of the provided audio file using Mutagen.
     *
     * @param audioFile the audio file
     * @return the length of the audio file
     * @throws NullPointerException     if the provided audio file is null
     * @throws IllegalArgumentException if the provided audio file is not a file,
     *                                  does not exist, or is not a supported audio type
     */
    private static Future<Duration> getLengthViaMutagen(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.isFile());
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(SupportedAudioFileType.isSupported(audioFile));

        CyderThreadFactory threadFactory = getThreadFactory(DetermineAudioLengthMethod.PYTHON_MUTAGEN, audioFile);
        return Executors.newSingleThreadExecutor(threadFactory).submit(() -> {
            // construct python command using wrappers
            // parse response

            return Duration.ofSeconds(0);
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
