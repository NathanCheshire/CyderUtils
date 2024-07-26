package com.github.natche.cyderutils.color;

import com.github.natche.cyderutils.exceptions.CyderException;

/** An exception thrown by {@link CyderColor} instances. */
public final class CyderColorException extends CyderException {
    /** Constructs a new CyderColorException exception using the provided error message. */
    public CyderColorException(String errorMessage) {
        super(errorMessage);
    }

    /** Constructs a new CyderColorException exception from the provided exception. */
    public CyderColorException(Exception e) {
        super(e);
    }
}
