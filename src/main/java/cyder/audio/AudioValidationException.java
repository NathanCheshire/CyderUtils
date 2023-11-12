package cyder.audio;

import cyder.audio.wav.WaveFile;

/**
 * An exception thrown by utility classes in {@link AudioValidationUtil}.
 */
public class AudioValidationException extends RuntimeException {
    /**
     * Constructs a new AudioValidationException using the provided error message.
     */
    public AudioValidationException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Constructs a new AudioValidationException from the provided exception.
     */
    public AudioValidationException(Exception e) {
        super(e);
    }
}
