package com.github.natche.cyderutils.image;

/** An exception thrown by {@link CyderImage} instances. */
public final class CyderImageException extends RuntimeException {
    /** Constructs a new CyderImageException exception using the provided error message. */
    public CyderImageException(String errorMessage) {
        super(errorMessage);
    }

    /** Constructs a new CyderImageException exception from the provided exception. */
    public CyderImageException(Exception e) {
        super(e);
    }
}

