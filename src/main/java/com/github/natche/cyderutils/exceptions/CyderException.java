package com.github.natche.cyderutils.exceptions;

import com.google.common.base.Preconditions;

/** An abstract exception class for all custom Cyder-defined exceptions to extend from. */
public abstract class CyderException extends RuntimeException {
    /**
     * Constructs a new CyderException from the provided message.
     *
     * @param message the message
     * @throws NullPointerException if the provided message is null
     * @throws IllegalArgumentException if the provided message is empty
     */
    public CyderException(String message) {
        super(preconditions(message));
    }

    /**
     * Constructs a new CyderException from the provided exception.
     *
     * @param e the exception
     * @throws NullPointerException if the provided exception is null
     */
    public CyderException(Exception e) {
        super(Preconditions.checkNotNull(e));
    }

    /**
     * Factory method to create an instance of the specific CyderException subclass.
     *
     * @param errorMessage the error message
     * @return the specific CyderException instance
     */
    private CyderException createException(String errorMessage) {

    }

    /**
     * Static method to throw the specific CyderException with the provided message.
     *
     * @param errorMessage the error message
     * @throws CyderException the custom exception
     * @throws NullPointerException if the provided exception or message are null
     * @throws IllegalArgumentException if the provided message is empty
     */
    public static void throwFromMessage(Class<? extends CyderException> exceptionClass, String errorMessage) throws CyderException {
        Preconditions.checkNotNull(exceptionClass);
        Preconditions.checkNotNull(errorMessage);
        Preconditions.checkArgument(!errorMessage.trim().isEmpty());

        try {
            CyderException exceptionInstance = exceptionClass.getDeclaredConstructor(String.class).newInstance(errorMessage);
            throw exceptionInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Exception instantiation failed", e);
        }
    }

    /**
     * Executes the Preconditions on the provided message.
     *
     * @param message the message
     * @return the message after performing Precondition checks.
     */
    private static String preconditions(String message) {
        Preconditions.checkNotNull(message);
        Preconditions.checkArgument(!message.trim().isEmpty());
        return message;
    }
}
