package cyder.exceptions;

/**
 * An exception fatal to the operation of Cyder such that Cyder should likely exit.
 */
@Deprecated
public class FatalException extends RuntimeException implements CyderExceptionMixin {
    /**
     * Constructs a new Fatal exception using the provided error message.
     */
    public FatalException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Constructs a new Fatal exception from the provided exception.
     */
    public FatalException(Exception e) {
        super(e);
    }

    @Override
    public void throwFromMessage(String errorMessage) {
        throw new FatalException(errorMessage);
    }
}

