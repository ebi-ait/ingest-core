package org.humancellatlas.ingest.state;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 12/09/17
 */
public class MetadataDocumentStateException extends RuntimeException {
    public MetadataDocumentStateException() {
        super();
    }

    public MetadataDocumentStateException(String message) {
        super(message);
    }

    public MetadataDocumentStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetadataDocumentStateException(Throwable cause) {
        super(cause);
    }

    protected MetadataDocumentStateException(String message,
                                             Throwable cause,
                                             boolean enableSuppression,
                                             boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
