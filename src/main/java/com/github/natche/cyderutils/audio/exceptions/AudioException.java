package com.github.natche.cyderutils.audio.exceptions;

import com.github.natche.cyderutils.exceptions.CyderExceptionMixin;

/**
 * An exception thrown by methods throughout the audio package.
 */
// todo use the mixin more
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
        throw new AudioException(errorMessage);
    }
}
