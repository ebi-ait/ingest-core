package org.humancellatlas.ingest.core;

import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.core.web.LinkGenerator;
import org.humancellatlas.ingest.export.job.ExportJob;
import org.humancellatlas.ingest.messaging.model.BundleUpdateMessage;
import org.humancellatlas.ingest.messaging.model.ExportMessage;
import org.humancellatlas.ingest.messaging.model.MessageProtocol;
import org.humancellatlas.ingest.messaging.model.MetadataDocumentMessage;
import org.humancellatlas.ingest.state.ValidationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Identifiable;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
                metadataDocId, metadataDocUuid, validationState, callbackLink, envelopeId);
    }

    public ExportMessage buildExperimentSubmittedMessage(ExportJob exportJob) {
        String callbackLink = linkGenerator.createCallback(documentType, metadataDocId);
        return new ExportMessage(UUID.fromString(metadataDocUuid), Instant.now().toString(), messageProtocol, exportJob.getId(), metadataDocId, metadataDocUuid, callbackLink,
                documentType.getSimpleName(), envelopeId, envelopeUuid, assayIndex, totalAssays);
    }

    public ExportMessage buildManifestSubmittedMessage() {
        String callbackLink = linkGenerator.createCallback(documentType, metadataDocId);
        return new ExportMessage(UUID.randomUUID(), Instant.now().toString(), messageProtocol, null, metadataDocId, metadataDocUuid, callbackLink,
                documentType.getSimpleName(), envelopeId, envelopeUuid, assayIndex, totalAssays);
    }

    public BundleUpdateMessage buildBundleUpdateMessage(BundleManifest bundleManifest, Set<MetadataDocument> documentList) {
        List<String> callbackLinks = documentList
                                        .stream()
                                        .map(document -> linkGenerator.createCallback(document.getClass(),document.getId()))
                                        .collect(Collectors.toList());
        return new BundleUpdateMessage(UUID.fromString(bundleManifest.getBundleUuid()), Instant.now().toString(),
                                       bundleManifest.getId(), bundleManifest.getBundleUuid(), BundleManifest.class.getSimpleName(),
                                       callbackLinks, envelopeId, envelopeUuid, assayIndex, totalAssays, null);
    }

}
