package com.github.natche.cyderutils.audio.cplayer;

/** An exception for {@link CPlayer}s. */
public class CPlayerException extends RuntimeException {
    /** Constructs a new CPlayerException using the provided error message. */
    public CPlayerException(String errorMessage) {
        super(errorMessage);
    }

    /** Constructs a new CPlayerException from the provided exception. */
    public CPlayerException(Exception e) {
        super(e);
    }
}
