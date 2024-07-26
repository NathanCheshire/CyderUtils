package com.github.natche.cyderutils.font;

import com.github.natche.cyderutils.exceptions.CyderException;

/** An exception thrown by {@link FontUtil} */
public class FontException extends CyderException {
    /** Constructs a new FontException exception using the provided error message. */
    public FontException(String errorMessage) {
        super(errorMessage);
    }

    /** Constructs a new FontException exception from the provided exception. */
    public FontException(Exception e) {
        super(e);
    }
}
