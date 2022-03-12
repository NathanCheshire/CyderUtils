package cyder.exceptions;

/**
 * An exception fatal to the operaton of Cyder such that Cyder should likely exit.
 */
public class FatalException extends RuntimeException {
    public FatalException(String errorMessage) {
        super(errorMessage);
    }
}

