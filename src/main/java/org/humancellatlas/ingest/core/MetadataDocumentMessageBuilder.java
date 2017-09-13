package org.humancellatlas.ingest.core;

import org.humancellatlas.ingest.analysis.Analysis;
import org.humancellatlas.ingest.analysis.web.AnalysisController;
import org.humancellatlas.ingest.assay.Assay;
import org.humancellatlas.ingest.assay.web.AssayController;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.web.FileController;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.web.ProjectController;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.web.ProtocolController;
import org.humancellatlas.ingest.sample.Sample;
import org.humancellatlas.ingest.sample.web.SampleController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.data.rest.webmvc.BaseUri;
import org.springframework.data.rest.webmvc.support.RepositoryLinkBuilder;
import org.springframework.hateoas.Link;

import java.net.URI;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 12/09/17
 */
public class MetadataDocumentMessageBuilder {
    public static MetadataDocumentMessageBuilder using(ResourceMappings mappings, RepositoryRestConfiguration config) {
        return new MetadataDocumentMessageBuilder(mappings, config);
    }

    private final String DUMMY_BASE_URI = "http://localhost:8080";

    private final ResourceMappings mappings;
    private final RepositoryRestConfiguration config;

    private Class<?> controllerClass;
    private Class<?> documentType;
    private String metadataDocId;
    private String metadataDocUuid;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    private MetadataDocumentMessageBuilder(ResourceMappings mappings, RepositoryRestConfiguration config) {
        this.mappings = mappings;
        this.config = config;
    }

    public MetadataDocumentMessageBuilder messageFor(MetadataDocument metadataDocument) {
        withDocumentType(metadataDocument.getClass()).withId(metadataDocument.getId());
        if (metadataDocument.getUuid() != null) {
            withUuid(metadataDocument.getUuid().toString());
        }
        if (metadataDocument instanceof Analysis) {
            return withControllerClass(AnalysisController.class);
        }
        if (metadataDocument instanceof Assay) {
            return withControllerClass(AssayController.class);
        }
        if (metadataDocument instanceof File) {
            return withControllerClass(FileController.class);
        }
        if (metadataDocument instanceof Project) {
            return withControllerClass(ProjectController.class);
        }
        if (metadataDocument instanceof Protocol) {
            return withControllerClass(ProtocolController.class);
        }
        if (metadataDocument instanceof Sample) {
            return withControllerClass(SampleController.class);
        }

        // couldn't match type
        throw new RuntimeException(String.format(
                "Unable to make metadata document message - unknown type of doc '%s'",
                metadataDocument.getClass()));
    }

    private MetadataDocumentMessageBuilder withControllerClass(Class<?> controllerClass) {
        this.controllerClass = controllerClass;

        return this;
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

    public MetadataDocumentMessage build() {
        // todo - here, we make link with DUMMY_BASE_URI and then take it out again so clients can fill in domain - must be a better way of doing this!
        RepositoryLinkBuilder rlb = new RepositoryLinkBuilder(mappings.getMetadataFor(documentType),
                                                              new BaseUri(URI.create(DUMMY_BASE_URI)));
        Link link = rlb
                .slash(metadataDocId)
                .withRel(mappings.getMetadataFor(documentType).getItemResourceRel());
        String callbackLink = link.withSelfRel().getHref().replace(DUMMY_BASE_URI, "");

        return new MetadataDocumentMessage(documentType.getSimpleName().toLowerCase(), metadataDocId, metadataDocUuid, callbackLink);
    }
}
