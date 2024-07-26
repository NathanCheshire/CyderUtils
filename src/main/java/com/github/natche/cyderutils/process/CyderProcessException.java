package com.github.natche.cyderutils.process;

import com.github.natche.cyderutils.exceptions.CyderException;

/**
 * An exception for when a process invoked via the
 * {@link ProcessUtil} encounters an {@link java.io.IOException}.
 */
public class CyderProcessException extends CyderException {
    /** Constructs a new CyderProcessException using the provided error message. */
    public CyderProcessException(String errorMessage) {
        super(errorMessage);
    }

    /** Constructs a new CyderProcessException from the provided exception. */
    public CyderProcessException(Exception e) {
        super(e);
    }
}