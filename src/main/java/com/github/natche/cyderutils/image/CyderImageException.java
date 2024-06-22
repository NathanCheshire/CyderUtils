package com.github.natche.cyderutils.image;

import com.github.natche.cyderutils.exceptions.CyderExceptionMixin;

/**
 * An exception thrown by {@link CyderImage} instances.
 */
public final class CyderImageException extends RuntimeException implements CyderExceptionMixin {
    /**
     * Constructs a new CyderImageException exception using the provided error message.
     */
    public CyderImageException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Constructs a new CyderImageException exception from the provided exception.
     */
    public CyderImageException(Exception e) {
        super(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void throwFromMessage(String errorMessage) {
        throw new CyderImageException(errorMessage);
    }
}

