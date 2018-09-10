package org.humancellatlas.ingest.messaging.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.humancellatlas.ingest.messaging.model.AbstractEntityMessage;
import org.humancellatlas.ingest.state.ValidationState;

import java.util.Collection;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 12/09/17
 */
@Getter
@AllArgsConstructor
public class MetadataDocumentMessage implements AbstractEntityMessage {
    private final MessageProtocol messageProtocol;
    private final String documentType;
    private final String documentId;
    private final String documentUuid;
    private final ValidationState validationState;
    private final String callbackLink;
    private final Collection<String> envelopeIds;
}
