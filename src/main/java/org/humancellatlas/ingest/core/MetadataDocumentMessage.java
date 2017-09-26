package org.humancellatlas.ingest.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 12/09/17
 */
@Getter
@AllArgsConstructor
public class MetadataDocumentMessage implements AbstractEntityMessage {
    private final String documentType;
    private final String documentId;
    private final String documentUuid;
    private final String callbackLink;
}
