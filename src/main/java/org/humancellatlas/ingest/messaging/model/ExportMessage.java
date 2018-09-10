package org.humancellatlas.ingest.messaging.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExportMessage implements AbstractEntityMessage {
    private final MessageProtocol messageProtocol;
    private final String documentId;
    private final String documentUuid;
    private final String callbackLink;
    private final String documentType;
    private final String envelopeId;
    private final String envelopeUuid;
    private final int index;
    private final int total;

}
