package org.humancellatlas.ingest.messaging.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExportSubmissionMessage {
  private final String exportJobId;
  private final String submissionUuid;
  private final String projectUuid;
  private final String callbackLink;
  private final Map<String, Object> context;
}
