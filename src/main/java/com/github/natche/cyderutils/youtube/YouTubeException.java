package com.github.natche.cyderutils.youtube;

import com.github.natche.cyderutils.exceptions.CyderException;

/** An exception used to indicate that an operation involving YouTube failed. */
public class YouTubeException extends CyderException {
    /** Constructs a new YouTube exception using the provided error message. */
    public YouTubeException(String errorMessage) {
        super(errorMessage);
    }

    /** Constructs a new YouTube exception from the provided exception. */
    public YouTubeException(Exception e) {
        super(e);
    }
}
