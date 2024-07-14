package com.github.natche.cyderutils.files.exceptions;

import com.github.natche.cyderutils.exceptions.CyderException;

/**
 * An exception used to indicate the host's operating system could not be validated
 * as a supported one.
 */
public class UnsupportedOsException extends CyderException {
    /** Constructs a new UnsupportedOs exception using the provided error message. */
    public UnsupportedOsException(String errorMessage) {
        super(errorMessage);
    }

    /** Constructs a new UnsupportedOs exception from the provided exception. */
    public UnsupportedOsException(Exception e) {
        super(e);
    }

    public static void main(String[] args) {
        UnsupportedOsException.throwFromMessage("adf");
    }
}
