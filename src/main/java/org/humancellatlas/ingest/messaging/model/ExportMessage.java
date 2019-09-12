package org.humancellatlas.ingest.messaging.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ExportMessage implements AbstractEntityMessage {
    private final UUID bundleUuid;
    private final String versionTimestamp;

    private final String documentId;
    private final String documentUuid;
    private final String callbackLink;
    private final String documentType;
    private final String envelopeId;
    private final String envelopeUuid;
    private final int index;
    private final int total;

}
