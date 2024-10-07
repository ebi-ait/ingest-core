package uk.ac.ebi.subs.ingest.messaging.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExportEntityMessage {
  private final String exportJobId;
  private final String documentId;
  private final String documentUuid;
  private final String callbackLink;
  private final String documentType;
  private final String envelopeId;
  private final String envelopeUuid;
  private final String projectId;
  private final String projectUuid;
  private final int index;
  private final int total;
  private final Map<String, Object> context;
}
