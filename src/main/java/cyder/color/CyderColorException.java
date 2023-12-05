package cyder.color;

import cyder.exceptions.CyderExceptionMixin;

/**
 * An exception thrown by {@link CyderColor} instances.
 */
public class CyderColorException extends RuntimeException implements CyderExceptionMixin {
    /**
     * Constructs a new CyderColorException exception using the provided error message.
     */
    public CyderColorException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Constructs a new CyderColorException exception from the provided exception.
     */
    public CyderColorException(Exception e) {
        super(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void throwFromMessage(String errorMessage) {
        throw new CyderColorException(errorMessage);
    }
}
