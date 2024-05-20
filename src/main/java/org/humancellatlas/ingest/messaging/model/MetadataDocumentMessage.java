package org.humancellatlas.ingest.messaging.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.humancellatlas.ingest.state.ValidationState;


/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 12/09/17
 */
@Getter
@AllArgsConstructor
@ToString
public class MetadataDocumentMessage {
    private final String documentType;
    private final String documentId;
    private final String documentUuid;
    private final ValidationState validationState;
    private final String callbackLink;
    private final String envelopeId;
}
