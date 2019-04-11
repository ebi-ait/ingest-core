package org.humancellatlas.ingest.messaging.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Simon Jupp

 */
@AllArgsConstructor
@Getter
public class SubmissionEnvelopeMessage implements AbstractEntityMessage {
    private final MessageProtocol messageProtocol;
    private final String documentType;
    private final String documentId;
    private final String documentUuid;
    private final String callbackLink;
}
