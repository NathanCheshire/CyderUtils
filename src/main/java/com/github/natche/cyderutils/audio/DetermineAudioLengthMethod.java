package com.github.natche.cyderutils.audio;

import com.github.natche.cyderutils.annotations.ForReadability;
import com.github.natche.cyderutils.audio.ffmpeg.FfmpegArgument;
import com.github.natche.cyderutils.audio.ffmpeg.FfmpegCommandBuilder;
import com.github.natche.cyderutils.audio.ffmpeg.FfmpegPrintFormat;
import com.github.natche.cyderutils.audio.ffmpeg.FfmpegStreamEntry;
import com.github.natche.cyderutils.audio.validation.SupportedAudioFileType;
import com.github.natche.cyderutils.constants.CyderRegexPatterns;
import com.github.natche.cyderutils.files.temporary.CyderTemporaryFile;
import com.github.natche.cyderutils.files.FileUtil;
import com.github.natche.cyderutils.process.*;
import com.github.natche.cyderutils.strings.StringUtil;
import com.github.natche.cyderutils.threads.CyderThreadFactory;
import com.github.natche.cyderutils.time.TimeUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** The methods Cyder supports for extracting the audio length from a {@link SupportedAudioFileType}. */
public enum DetermineAudioLengthMethod {
    /** Determine an audio file's length using ffmpeg. */
    FFMPEG(DetermineAudioLengthMethod::getLengthViaFfmpeg),

    /** Determine an audio file's length using the Python package Mutagen. */
    PYTHON_MUTAGEN(DetermineAudioLengthMethod::getLengthViaMutagen);

    // todo add wave file duration clip? Maybe just clip if it'll work for other audio file types
    //  since we can get duration from a wave directly so conv to wave and then get that from the cyder wav file

    /**
     * The pattern used to extract the duration seconds floating point number from the
     * show streams command output.
     */
    private static final Pattern showStreamsDurationPattern
            = Pattern.compile("\"duration\":\\s*\"(\\d+\\.\\d+)\"");

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

            String command = new FfmpegCommandBuilder(FfmpegArgument.FFPROBE)
                    .addArgument(FfmpegArgument.PRINT_FORMAT.getArgument(), FfmpegPrintFormat.JSON.getFormatName())
                    .addArgument(FfmpegArgument.SHOW_STREAMS.getArgument())
                    .addArgument(FfmpegArgument.SHOW_ENTRIES.getArgument(),
                            FfmpegStreamEntry.DURATION.getStreamCommand())
                    .addArgument(absolutePath)
                    .build();

            Future<ProcessResult> futureResult = ProcessUtil.getProcessOutput(command);
            while (!futureResult.isDone()) Thread.onSpinWait();

            ProcessResult result;

            try {
                result = futureResult.get();
                if (futureResult.isCancelled()) throw new RuntimeException("Future was canceled");
            } catch (Exception e) {
                throw new CyderProcessException(e);
            }

            String joinedOutput = StringUtil.joinParts(result.getStandardOutput(), "");
            String trimmedOutput = joinedOutput.replaceAll(CyderRegexPatterns.multipleWhiteSpaceRegex, "");

            /*
                Here we used to have parser classes and use Gson to serialize into the proper JSON structure
                and obtain the duration from the output, this was a lot of boilerplate code just to find the
                "duration" key and its value. Therefore, I have opted to just use a regex to get the value.
             */

            Matcher matcher = showStreamsDurationPattern.matcher(trimmedOutput);
            if (!matcher.find()) throw new CyderProcessException("Failed to find duration in show streams output: "
                    + trimmedOutput);
            String millisPropertyString = matcher.group(1);
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
     * @throws CyderProcessException    if {@link PythonPackage#MUTAGEN} is not and cannot be
     *                                  installed or a valid working Python command cannot be found
     */
    private static Future<Duration> getLengthViaMutagen(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.isFile());
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(SupportedAudioFileType.isSupported(audioFile));

        CyderThreadFactory threadFactory = getThreadFactory(DetermineAudioLengthMethod.PYTHON_MUTAGEN, audioFile);
        return Executors.newSingleThreadExecutor(threadFactory).submit(() -> {
            Future<Boolean> mutagenInstalled = PythonPackage.MUTAGEN.isInstalled();
            while (!mutagenInstalled.isDone()) Thread.onSpinWait();

            if (!mutagenInstalled.get()) {
                Future<Boolean> installationRequest = PythonPackage.MUTAGEN.install();
                while (!installationRequest.isDone()) Thread.onSpinWait();
                if (!installationRequest.get()) throw new CyderProcessException("Failed to install Mutagen");
            }

            ImmutableList<String> script = ImmutableList.of(
                    "import sys",
                    "from mutagen import File",
                    "audio_file = File(r\"" + audioFile.getAbsolutePath() + "\")",
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
            if (firstCommandOptional.isEmpty())
                throw new CyderProcessException("Failed to find working Python command");

            Future<ProcessResult> mutagenLengthResult = ProcessUtil.getProcessOutput(
                    ImmutableList.of(firstCommandOptional.get(), scriptFile.getAbsolutePath()));
            while (!mutagenInstalled.isDone()) Thread.onSpinWait();
            ProcessResult result = mutagenLengthResult.get();

            if (result.hasErrors()) {
                throw new CyderProcessException(
                        "Mutagen length process result contains errors: " + result.getErrorOutput());
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
