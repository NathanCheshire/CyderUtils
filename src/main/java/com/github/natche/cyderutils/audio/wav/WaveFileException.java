package com.github.natche.cyderutils.audio.wav;

import com.github.natche.cyderutils.exceptions.CyderException;

/** An exception thrown by, or operations related to, {@link WaveFile}s. */
public class WaveFileException extends CyderException {
    /** Constructs a new WaveFileException using the provided error message. */
    public WaveFileException(String errorMessage) {
        super(errorMessage);
    }

    /** Constructs a new WaveFileException from the provided exception. */
    public WaveFileException(Exception e) {
        super(e);
    }
}
