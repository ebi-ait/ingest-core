package org.humancellatlas.ingest.core;

import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.core.web.LinkGenerator;
import org.humancellatlas.ingest.messaging.model.ExportMessage;
import org.humancellatlas.ingest.messaging.model.MessageProtocol;
import org.humancellatlas.ingest.messaging.model.MetadataDocumentMessage;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.UUID;

public class MetadataDocumentMessageBuilder {

    private LinkGenerator linkGenerator;

    private MessageProtocol messageProtocol;
    private Class<?> documentType;
    private String metadataDocId;
    private String metadataDocUuid;
    private String envelopeId;
    private String envelopeUuid;
    private ValidationState validationState;
    private int assayIndex;
    private int totalAssays;
    private Collection<String> envelopeIds;

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
        MetadataDocumentMessageBuilder builder = withDocumentType(metadataDocument.getClass())
                .withId(metadataDocument.getId());
        Uuid metadataDocumentUuid = metadataDocument.getUuid();
        if (metadataDocumentUuid != null && metadataDocumentUuid.getUuid() != null) {
            builder = builder.withUuid(metadataDocument.getUuid().getUuid().toString());
        }

        return builder;
    }

    public MetadataDocumentMessageBuilder withMessageProtocol(MessageProtocol messageProtocol) {
        this.messageProtocol = messageProtocol;

        return this;
    }

    private <T extends MetadataDocument> MetadataDocumentMessageBuilder withDocumentType(
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

    public MetadataDocumentMessageBuilder withValidationState(ValidationState validationState) {
        this.validationState = validationState;

        return this;
    }

    public MetadataDocumentMessageBuilder withEnvelopeIds(Collection<String> envelopeIds) {
        this.envelopeIds = envelopeIds;

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

    public MetadataDocumentMessageBuilder withAssayIndex(int assayIndex) {
        this.assayIndex = assayIndex;

        return this;
    }

    public MetadataDocumentMessageBuilder withTotalAssays(int totalAssays) {
        this.totalAssays = totalAssays;

        return this;
    }

    public MetadataDocumentMessage build() {
        String callbackLink = linkGenerator.createCallback(documentType, metadataDocId);
        return new MetadataDocumentMessage(messageProtocol, documentType.getSimpleName().toLowerCase(),
                metadataDocId, metadataDocUuid, validationState, callbackLink, envelopeIds);
    }

    public ExportMessage buildAssaySubmittedMessage() {
        String callbackLink = linkGenerator.createCallback(documentType, metadataDocId);
        return new ExportMessage(UUID.randomUUID(), DateTime.now().toString(), messageProtocol, metadataDocId, metadataDocUuid, callbackLink,
                documentType.getSimpleName(), envelopeId, envelopeUuid, assayIndex, totalAssays);
    }

    public ExportMessage buildUpdateExportMessage(BundleManifest bundleManifest) {
        String callbackLink = linkGenerator.createCallback(bundleManifest.getClass(),bundleManifest.getId());
        return new ExportMessage(UUID.fromString(bundleManifest.getBundleUuid()), DateTime.now().toString(), messageProtocol, metadataDocId, metadataDocUuid, callbackLink,
                bundleManifest.getClass().getSimpleName(), envelopeId, envelopeUuid, assayIndex, totalAssays);
    }

}
