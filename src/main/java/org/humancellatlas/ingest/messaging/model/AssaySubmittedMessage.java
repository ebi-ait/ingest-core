package org.humancellatlas.ingest.messaging.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by rolando on 20/03/2018.
 */
@Getter
@AllArgsConstructor
public class AssaySubmittedMessage implements AbstractEntityMessage {
    private final String documentId;
    private final String documentUuid;
    private final String callbackLink;
    private final String documentType;
    private final String envelopeId;
}
