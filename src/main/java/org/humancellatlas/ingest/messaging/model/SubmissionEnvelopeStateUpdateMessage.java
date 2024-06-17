package org.humancellatlas.ingest.messaging.model;

import org.humancellatlas.ingest.state.SubmissionState;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

public class SubmissionEnvelopeStateUpdateMessage extends SubmissionEnvelopeMessage {
  @Getter @Setter private SubmissionState requestedState;

  public SubmissionEnvelopeStateUpdateMessage(
      String documentType, String documentId, String documentUuid, String callbackLink) {
    super(documentType, documentId, documentUuid, callbackLink);
  }

  @JsonIgnore
  public static SubmissionEnvelopeStateUpdateMessage fromSubmissionEnvelopeMessage(
      SubmissionEnvelopeMessage message) {
    return new SubmissionEnvelopeStateUpdateMessage(
        message.getDocumentType(),
        message.getDocumentId(),
        message.getDocumentUuid(),
        message.getCallbackLink());
  }
}
