package org.humancellatlas.ingest.messaging.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ManifestMessage {
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
