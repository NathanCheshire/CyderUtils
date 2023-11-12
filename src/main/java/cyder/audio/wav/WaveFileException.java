package cyder.audio.wav;

/**
 * An exception for {@link WaveFile}s.
 */
public class WaveFileException extends RuntimeException {
    /**
     * Constructs a new WaveFileException using the provided error message.
     */
    public WaveFileException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Constructs a new WaveFileException from the provided exception.
     */
    public WaveFileException(Exception e) {
        super(e);
    }
}
