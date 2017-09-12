package org.humancellatlas.ingest.state;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 12/09/17
 */
public class InvalidMetadataDocumentStateException extends RuntimeException {
    public InvalidMetadataDocumentStateException() {
        super();
    }

    public InvalidMetadataDocumentStateException(String message) {
        super(message);
    }

    public InvalidMetadataDocumentStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidMetadataDocumentStateException(Throwable cause) {
        super(cause);
    }

    protected InvalidMetadataDocumentStateException(String message,
                                                    Throwable cause,
                                                    boolean enableSuppression,
                                                    boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
