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
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.data.rest.webmvc.support.RepositoryLinkBuilder;
import org.springframework.hateoas.Link;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 12/09/17
 */
public class MetadataDocumentMessageBuilder {
    public static MetadataDocumentMessageBuilder usingLinkBuilder(RepositoryEntityLinks repositoryEntityLinks) {
        return new MetadataDocumentMessageBuilder(repositoryEntityLinks);
    }

    private final RepositoryEntityLinks repositoryEntityLinks;

    private Class<?> controllerClass;
    private Class<?> documentType;
    private String metadataDocId;

    private MetadataDocumentMessageBuilder(RepositoryEntityLinks repositoryEntityLinks) {
        this.repositoryEntityLinks = repositoryEntityLinks;
    }

    public MetadataDocumentMessageBuilder messageFor(MetadataDocument metadataDocument) {
        withDocumentType(metadataDocument.getClass()).withId(metadataDocument.getId());
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

    public MetadataDocumentMessage build() {
        String callbackLink = repositoryEntityLinks.linkToSingleResource(
                documentType, metadataDocId).withSelfRel().getHref();

        // todo make link relative so clients can fill in domain - must be a better way of doing this!
        callbackLink = callbackLink.replace("http://localhost:8080", "");

        return new MetadataDocumentMessage(documentType.getSimpleName().toLowerCase(), metadataDocId, callbackLink);
    }
}
