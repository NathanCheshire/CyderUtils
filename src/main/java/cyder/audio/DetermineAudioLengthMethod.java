package cyder.audio;

import com.google.common.base.Preconditions;

import java.io.File;
import java.time.Duration;
import java.util.function.Function;

/**
 * The methods Cyder supports for extracting the audio length from a {@link SupportedAudioFileType}.
 */
public enum DetermineAudioLengthMethod {
    FFPROBE((file) -> Duration.ofSeconds(0)),
    PYTHON_MUTAGEN((file) -> Duration.ofSeconds(0));

    private final Function<File, Duration> audioLengthComputationFunction;

    DetermineAudioLengthMethod(Function<File, Duration> audioLengthComputationFunction) {
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
    public Duration determineAudioLength(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.isFile());
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(SupportedAudioFileType.isSupported(audioFile));

        return audioLengthComputationFunction.apply(audioFile);
    }
}
