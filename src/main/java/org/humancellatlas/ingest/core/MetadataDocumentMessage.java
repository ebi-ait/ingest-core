package org.humancellatlas.ingest.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 12/09/17
 */
@AllArgsConstructor
@Getter
public class MetadataDocumentMessage {
    private final String documentType;
    private final String documentId;
    private final String documentUuid;
    private final String callbackLink;
}
