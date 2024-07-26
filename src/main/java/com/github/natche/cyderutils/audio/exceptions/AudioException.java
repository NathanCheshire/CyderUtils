package com.github.natche.cyderutils.audio.exceptions;

import com.github.natche.cyderutils.exceptions.CyderException;

/** An exception thrown by methods throughout the audio package. */
public final class AudioException extends CyderException {
    /** Constructs a new AudioException exception using the provided error message. */
    public AudioException(String errorMessage) {
        super(errorMessage);
    }

    /** Constructs a new AudioException exception from the provided exception. */
    public AudioException(Exception e) {
        super(e);
    }
}
