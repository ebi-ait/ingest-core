package org.humancellatlas.ingest.core;

import org.humancellatlas.ingest.core.web.LinkGenerator;
import org.humancellatlas.ingest.messaging.model.ExportMessage;
import org.humancellatlas.ingest.messaging.model.MetadataDocumentMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.data.rest.webmvc.BaseUri;
import org.springframework.data.rest.webmvc.support.RepositoryLinkBuilder;
import org.springframework.hateoas.Link;

import java.net.URI;
import java.util.Collection;

public class MetadataDocumentMessageBuilder {

    private LinkGenerator linkGenerator;

    private Class<?> documentType;
    private String metadataDocId;
    private String metadataDocUuid;
    private String envelopeId;
    private String envelopeUuid;
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
        if (metadataDocument.getUuid() != null) {
            builder = builder.withUuid(metadataDocument.getUuid().toString());
        }

        return builder;
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
        return new MetadataDocumentMessage(documentType.getSimpleName().toLowerCase(),
                metadataDocId, metadataDocUuid, callbackLink, envelopeIds);
    }

    public ExportMessage buildAssaySubmittedMessage() {
        String callbackLink = linkGenerator.createCallback(documentType, metadataDocId);
        return new ExportMessage(metadataDocId, metadataDocUuid, callbackLink,
                documentType.getSimpleName(), envelopeId, envelopeUuid, assayIndex, totalAssays);
    }

}
