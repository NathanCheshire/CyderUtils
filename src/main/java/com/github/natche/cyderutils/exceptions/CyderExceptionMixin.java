package com.github.natche.cyderutils.exceptions;

/**
 * An interface for forcing implementation of useful methods throughout custom exceptions Cyder uses internally.
 */
public interface CyderExceptionMixin {
    /**
     * Throws this exception with the provided message.
     *
     * @param errorMessage the error message
     */
    void throwFromMessage(String errorMessage);
}
