package cyderutils.video;

import cyderutils.exceptions.CyderExceptionMixin;

/**
 * An exception thrown by {@link CyderVideoFile} instances.
 */
public final class CyderVideoException extends RuntimeException implements CyderExceptionMixin {
    /**
     * Constructs a new CyderVideoException exception using the provided error message.
     */
    public CyderVideoException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Constructs a new CyderVideoException exception from the provided exception.
     */
    public CyderVideoException(Exception e) {
        super(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void throwFromMessage(String errorMessage) {
        throw new CyderVideoException(errorMessage);
    }
}
