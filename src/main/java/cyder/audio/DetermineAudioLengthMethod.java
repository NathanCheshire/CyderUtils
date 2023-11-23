package cyder.audio;

import com.google.common.base.Preconditions;
import cyder.threads.CyderThreadFactory;

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
     * Determine an audio file's length using ffprobe.
     */
    FFPROBE(DetermineAudioLengthMethod::getLengthViaFfprobe),

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

    private static Future<Duration> getLengthViaFfprobe(File audioFile) {
        CyderThreadFactory threadFactory = new CyderThreadFactory("name");
         return Executors.newSingleThreadExecutor(threadFactory).submit(() -> {


            return Duration.ofSeconds(0);
         });
    }

    private static Future<Duration> getLengthViaMutagen(File audioFile) {
        CyderThreadFactory threadFactory = new CyderThreadFactory("name");
        return Executors.newSingleThreadExecutor(threadFactory).submit(() -> {


            return Duration.ofSeconds(0);
        });
    }
}
