package com.github.natche.cyderutils.video;

import com.github.natche.cyderutils.exceptions.CyderException;

/** An exception thrown by {@link CyderVideoFile} instances. */
public final class CyderVideoException extends CyderException {
    /** Constructs a new CyderVideoException exception using the provided error message. */
    public CyderVideoException(String errorMessage) {
        super(errorMessage);
    }

    /** Constructs a new CyderVideoException exception from the provided exception. */
    public CyderVideoException(Exception e) {
        super(e);
    }
}
