package com.github.natche.cyderutils.audio.wav;

import com.github.natche.cyderutils.exceptions.CyderExceptionMixin;

/** An exception thrown by, or operations related to, {@link WaveFile}s. */
public class WaveFileException extends RuntimeException implements CyderExceptionMixin {
    /** Constructs a new WaveFileException using the provided error message. */
    public WaveFileException(String errorMessage) {
        super(errorMessage);
    }

    /** Constructs a new WaveFileException from the provided exception. */
    public WaveFileException(Exception e) {
        super(e);
    }

    /** {@inheritDoc} */
    @Override
    public void throwFromMessage(String errorMessage) {
        throw new WaveFileException(errorMessage);
    }
}
