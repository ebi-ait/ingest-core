package org.humancellatlas.ingest.messaging.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class BundleUpdateMessage implements AbstractEntityMessage{
    private final UUID bundleUuid;
    private final String versionTimestamp;

    private final String documentId;
    private final String documentUuid;
    private final String documentType;

    private final List<String> callbackLinks;
    private final String envelopeId;
    private final String envelopeUuid;
    private final int index;
    private final int total;
    private MessageProtocol messageProtocol;

}
