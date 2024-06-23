package com.github.natche.cyderutils.audio.cplayer;

import com.github.natche.cyderutils.exceptions.CyderExceptionMixin;

/**
 * An exception for {@link CPlayer}s.
 */
public class CPlayerException extends RuntimeException implements CyderExceptionMixin {
    /**
     * Constructs a new CPlayerException using the provided error message.
     */
    public CPlayerException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Constructs a new CPlayerException from the provided exception.
     */
    public CPlayerException(Exception e) {
        super(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void throwFromMessage(String errorMessage) {
        throw new CPlayerException(errorMessage);
    }
}
