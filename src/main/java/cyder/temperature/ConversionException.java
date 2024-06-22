package cyder.temperature;

/**
 * An exception for when a conversion error occurs.
 */
public class ConversionException extends RuntimeException {
    /**
     * Constructs a new ConversionException using the provided error message.
     */
    public ConversionException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Constructs a new ConversionException from the provided exception.
     */
    public ConversionException(Exception e) {
        super(e);
    }
}

