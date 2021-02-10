package org.humancellatlas.ingest.messaging.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DataExportMessage implements AbstractEntityMessage {
    private final String submissionUuid;
    private final String projectUuid;
    private final String exportJobId;
}