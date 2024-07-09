package com.github.natche.cyderutils.bounds;

import com.github.natche.cyderutils.audio.exceptions.AudioException;
import com.github.natche.cyderutils.exceptions.CyderExceptionMixin;

/** An exception thrown by {@link BoundsString}s. */
public class BoundsComputationException extends RuntimeException implements CyderExceptionMixin {
    public BoundsComputationException(String errorMessage) {
        super(errorMessage);
    }

    /** Constructs a new BoundsComputationException exception from the provided exception. */
    public BoundsComputationException(Exception e) {
        super(e);
    }

    /** {@inheritDoc} */
    @Override
    public void throwFromMessage(String errorMessage) {
        throw new AudioException(errorMessage);
    }
}
