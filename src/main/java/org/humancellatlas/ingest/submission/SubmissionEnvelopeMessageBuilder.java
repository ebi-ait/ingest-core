package org.humancellatlas.ingest.submission;

import org.humancellatlas.ingest.core.web.LinkGenerator;
import org.humancellatlas.ingest.messaging.model.SubmissionEnvelopeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubmissionEnvelopeMessageBuilder {
  public static SubmissionEnvelopeMessageBuilder using(LinkGenerator linkGenerator) {
    return new SubmissionEnvelopeMessageBuilder(linkGenerator);
  }

  private LinkGenerator linkGenerator;

  private Class<?> documentType;
  private String submissionEnvelopeId;
  private String submissionEnvelopeUuid;

  private final Logger log = LoggerFactory.getLogger(getClass());

  protected Logger getLog() {
    return log;
  }

  private SubmissionEnvelopeMessageBuilder(LinkGenerator linkGenerator) {
    this.linkGenerator = linkGenerator;
  }

  public SubmissionEnvelopeMessageBuilder messageFor(SubmissionEnvelope submissionEnvelope) {
    withDocumentType(submissionEnvelope.getClass())
        .withId(submissionEnvelope.getId())
        .withUuid(submissionEnvelope.getUuid().getUuid().toString());

    return this;
  }

  private <T extends SubmissionEnvelope> SubmissionEnvelopeMessageBuilder withDocumentType(
      Class<T> documentClass) {
    this.documentType = documentClass;

    return this;
  }

  private SubmissionEnvelopeMessageBuilder withId(String metadataDocId) {
    this.submissionEnvelopeId = metadataDocId;

    return this;
  }

  private SubmissionEnvelopeMessageBuilder withUuid(String uuid) {
    this.submissionEnvelopeUuid = uuid;

    return this;
  }

  public SubmissionEnvelopeMessage build() {

    String callbackLink = linkGenerator.createCallback(documentType, submissionEnvelopeId);
    return new SubmissionEnvelopeMessage(
        documentType.getSimpleName().toLowerCase(),
        submissionEnvelopeId,
        submissionEnvelopeUuid,
        callbackLink);
  }
}
