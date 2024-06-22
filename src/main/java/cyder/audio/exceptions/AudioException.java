package cyder.audio.exceptions;

import cyder.exceptions.CyderExceptionMixin;

/**
 * An exception thrown by methods throughout the audio package.
 */
public final class AudioException extends RuntimeException implements CyderExceptionMixin {
    /**
     * Constructs a new AudioException exception using the provided error message.
     */
    public AudioException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Constructs a new AudioException exception from the provided exception.
     */
    public AudioException(Exception e) {
        super(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void throwFromMessage(String errorMessage) {
        throw new cyder.color.CyderColorException(errorMessage);
    }
}
