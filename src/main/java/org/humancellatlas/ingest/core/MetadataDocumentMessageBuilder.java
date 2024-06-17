package org.humancellatlas.ingest.core;

import java.time.Instant;

import org.humancellatlas.ingest.core.web.LinkGenerator;
import org.humancellatlas.ingest.messaging.model.MetadataDocumentMessage;
import org.humancellatlas.ingest.state.ValidationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Identifiable;

public class MetadataDocumentMessageBuilder {

  private LinkGenerator linkGenerator;

  private Class<?> documentType;
  private String metadataDocId;
  private String metadataDocUuid;
  private String envelopeId;
  private String envelopeUuid;
  private Instant metadataDocVersion;
  private ValidationState validationState;

  private final Logger log = LoggerFactory.getLogger(getClass());

  private MetadataDocumentMessageBuilder(LinkGenerator linkGenerator) {
    this.linkGenerator = linkGenerator;
  }

  public static MetadataDocumentMessageBuilder using(LinkGenerator linkGenerator) {
    return new MetadataDocumentMessageBuilder(linkGenerator);
  }

  protected Logger getLog() {
    return log;
  }

  public MetadataDocumentMessageBuilder messageFor(MetadataDocument metadataDocument) {
    MetadataDocumentMessageBuilder builder =
        withDocumentType(metadataDocument.getClass()).withId(metadataDocument.getId());
    Uuid metadataDocumentUuid = metadataDocument.getUuid();
    if (metadataDocumentUuid != null && metadataDocumentUuid.getUuid() != null) {
      builder = builder.withUuid(metadataDocument.getUuid().getUuid().toString());
    }
    builder = builder.withVersion(metadataDocument.getDcpVersion());

    return builder;
  }

  private <T extends Identifiable> MetadataDocumentMessageBuilder withDocumentType(
      Class<T> documentClass) {
    this.documentType = documentClass;
    return this;
  }

  private MetadataDocumentMessageBuilder withId(String metadataDocId) {
    this.metadataDocId = metadataDocId;

    return this;
  }

  private MetadataDocumentMessageBuilder withUuid(String metadataDocUuid) {
    this.metadataDocUuid = metadataDocUuid;

    return this;
  }

  private MetadataDocumentMessageBuilder withVersion(Instant metadataDocVersion) {
    this.metadataDocVersion = metadataDocVersion;

    return this;
  }

  public MetadataDocumentMessageBuilder withValidationState(ValidationState validationState) {
    this.validationState = validationState;

    return this;
  }

  public MetadataDocumentMessageBuilder withEnvelopeId(String envelopeId) {
    this.envelopeId = envelopeId;

    return this;
  }

  public MetadataDocumentMessageBuilder withEnvelopeUuid(String envelopeUuid) {
    this.envelopeUuid = envelopeUuid;

    return this;
  }

  public MetadataDocumentMessage build() {
    String callbackLink = linkGenerator.createCallback(documentType, metadataDocId);
    return new MetadataDocumentMessage(
        documentType.getSimpleName().toLowerCase(),
        metadataDocId,
        metadataDocUuid,
        validationState,
        callbackLink,
        envelopeId);
  }
}
