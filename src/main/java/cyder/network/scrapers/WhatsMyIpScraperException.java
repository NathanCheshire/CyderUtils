package cyder.network.scrapers;

/**
 * An exception thrown by methods inside the network scrapers package.
 */
public class WhatsMyIpScraperException extends RuntimeException {
    /**
     * Constructs a new WhatsMyIpScraperException using the provided error message.
     */
    public WhatsMyIpScraperException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Constructs a new WhatsMyIpScraperException from the provided exception.
     */
    public WhatsMyIpScraperException(Exception e) {
        super(e);
    }
}

