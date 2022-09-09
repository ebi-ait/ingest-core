package org.humancellatlas.ingest.messaging.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class ExportSubmissionMessage {
    private final String exportJobId;
    private final String submissionUuid;
    private final String projectUuid;
    private final String callbackLink;
    private final Map<String, Object> context;
}
