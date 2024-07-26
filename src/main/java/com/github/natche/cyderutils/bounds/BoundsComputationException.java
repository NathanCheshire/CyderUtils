package com.github.natche.cyderutils.bounds;

import com.github.natche.cyderutils.exceptions.CyderException;

/** An exception thrown by {@link BoundsString}s. */
public class BoundsComputationException extends CyderException {
    public BoundsComputationException(String errorMessage) {
        super(errorMessage);
    }

    /** Constructs a new BoundsComputationException exception from the provided exception. */
    public BoundsComputationException(Exception e) {
        super(e);
    }
}
