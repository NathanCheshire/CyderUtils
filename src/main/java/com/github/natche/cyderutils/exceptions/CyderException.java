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
     * Static method to throw the specific CyderException with the provided message.
     * This method is inherited by all subclasses and works correctly for each subclass.
     *
     * @param errorMessage the error message
     * @throws T the specific CyderException subclass
     */
    @SuppressWarnings("unchecked")
    public static <T extends CyderException> void throwFromMessage(String errorMessage) throws T {
        Preconditions.checkNotNull(errorMessage);
        Preconditions.checkArgument(!errorMessage.trim().isEmpty());

        try {
            Class<T> exceptionClass = (Class<T>)
                    Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());
            throw exceptionClass.getDeclaredConstructor(String.class).newInstance(errorMessage);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to instantiate or throw exception", e);
        }
    }

    /**
     * Executes the Preconditions on the provided message.
     *
     * @param message the message
     * @return the message after performing Precondition checks.
     */
    protected static String preconditions(String message) {
        Preconditions.checkNotNull(message);
        Preconditions.checkArgument(!message.trim().isEmpty());
        return message;
    }
}