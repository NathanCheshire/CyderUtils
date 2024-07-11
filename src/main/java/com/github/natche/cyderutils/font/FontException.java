package com.github.natche.cyderutils.font;

import com.github.natche.cyderutils.audio.exceptions.AudioException;
import com.github.natche.cyderutils.exceptions.CyderExceptionMixin;

/** An exception thrown by {@link FontUtil} */
public class FontException extends RuntimeException implements CyderExceptionMixin {
    /** Constructs a new FontException exception using the provided error message. */
    public FontException(String errorMessage) {
        super(errorMessage);
    }

    /** Constructs a new FontException exception from the provided exception. */
    public FontException(Exception e) {
        super(e);
    }

    /** {@inheritDoc} */
    @Override
    public void throwFromMessage(String errorMessage) {
        throw new AudioException(errorMessage);
    }
}
