package com.github.natche.cyderutils.exceptions;

/**
 * An exception thrown when an illegal method is invoked or triggered.
 */
public class IllegalMethodException extends RuntimeException implements CyderExceptionMixin {
    /**
     * Constructs a new IllegalMethod exception using the provided error message.
     */
    public IllegalMethodException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Constructs a new IllegalMethod exception from the provided exception.
     */
    public IllegalMethodException(Exception e) {
        super(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void throwFromMessage(String errorMessage) {
        throw new IllegalMethodException(errorMessage);
    }
}
