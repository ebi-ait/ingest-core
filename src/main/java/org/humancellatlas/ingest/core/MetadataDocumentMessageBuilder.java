package org.humancellatlas.ingest.core;

import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.web.BiomaterialController;
import org.humancellatlas.ingest.core.web.LinkGenerator;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.web.FileController;
import org.humancellatlas.ingest.messaging.model.AssaySubmittedMessage;
import org.humancellatlas.ingest.messaging.model.MetadataDocumentMessage;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.web.ProcessController;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.web.ProjectController;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.web.ProtocolController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.data.rest.webmvc.BaseUri;
import org.springframework.data.rest.webmvc.support.RepositoryLinkBuilder;
import org.springframework.hateoas.Link;

import java.net.URI;
import java.util.Collection;
import java.util.List;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 12/09/17
 */
public class MetadataDocumentMessageBuilder {

    private final String DUMMY_BASE_URI = "http://localhost:8080";

    private ResourceMappings mappings;

    //TODO this is unused, dead code
    private RepositoryRestConfiguration config;

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

    //TODO deprecate this constructor
    private MetadataDocumentMessageBuilder(ResourceMappings mappings,
            RepositoryRestConfiguration config) {
        this.mappings = mappings;
        this.config = config;
    }

    private MetadataDocumentMessageBuilder(LinkGenerator linkGenerator) {
        this.linkGenerator = linkGenerator;
    }

    public static MetadataDocumentMessageBuilder using(LinkGenerator linkGenerator) {
        return new MetadataDocumentMessageBuilder(linkGenerator);
    }

    public static MetadataDocumentMessageBuilder using(ResourceMappings mappings,
            RepositoryRestConfiguration config) {
        return new MetadataDocumentMessageBuilder(mappings, config);
    }

    protected Logger getLog() {
        return log;
    }

    public MetadataDocumentMessageBuilder messageFor(MetadataDocument metadataDocument) {
        MetadataDocumentMessageBuilder builder = withDocumentType(metadataDocument.getClass()).withId(metadataDocument.getId());
        if (metadataDocument.getUuid() != null) {
            builder = builder.withUuid(metadataDocument.getUuid().toString());
        }

        return builder;
    }

    private <T extends MetadataDocument> MetadataDocumentMessageBuilder withDocumentType(Class<T> documentClass) {
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
        // todo - here, we make link with DUMMY_BASE_URI and then take it out again so clients can fill in domain - must be a better way of doing this!
        RepositoryLinkBuilder rlb = new RepositoryLinkBuilder(mappings.getMetadataFor(documentType),
                                                              new BaseUri(URI.create(DUMMY_BASE_URI)));
        Link link = rlb
                .slash(metadataDocId)
                .withRel(mappings.getMetadataFor(documentType).getItemResourceRel());
        String callbackLink = link.withSelfRel().getHref().replace(DUMMY_BASE_URI, "");

        return new MetadataDocumentMessage(documentType.getSimpleName().toLowerCase(), metadataDocId, metadataDocUuid, callbackLink, envelopeIds);
    }

    public AssaySubmittedMessage buildAssaySubmittedMessage() {
        String callbackLink = null;
        if (linkGenerator == null) {
            RepositoryLinkBuilder rlb = new RepositoryLinkBuilder(
                    mappings.getMetadataFor(documentType), new BaseUri(URI.create(DUMMY_BASE_URI)));
            Link link = rlb
                    .slash(metadataDocId)
                    .withRel(mappings.getMetadataFor(documentType).getItemResourceRel());
            callbackLink = link.withSelfRel().getHref().replace(DUMMY_BASE_URI, "");
        }
        return new AssaySubmittedMessage(metadataDocId, metadataDocUuid, callbackLink, documentType.getSimpleName(), envelopeId, envelopeUuid, assayIndex, totalAssays);
    }
}
