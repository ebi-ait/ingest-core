package org.humancellatlas.ingest.messaging.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExportJobMessage {
    private final String exportJobId;
    private final String callbackLink;
    private final String envelopeUuid;
    private final String projectUuid;
}
