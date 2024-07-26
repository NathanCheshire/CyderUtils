package com.github.natche.cyderutils.bounds;

/** An exception thrown by {@link BoundsString}s. */
public class BoundsComputationException extends RuntimeException {
    public BoundsComputationException(String errorMessage) {
        super(errorMessage);
    }

    /** Constructs a new BoundsComputationException exception from the provided exception. */
    public BoundsComputationException(Exception e) {
        super(e);
    }
}
