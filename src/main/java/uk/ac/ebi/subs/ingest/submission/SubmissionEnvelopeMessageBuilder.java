package uk.ac.ebi.subs.ingest.submission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.subs.ingest.core.web.LinkGenerator;
import uk.ac.ebi.subs.ingest.messaging.model.SubmissionEnvelopeMessage;

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
