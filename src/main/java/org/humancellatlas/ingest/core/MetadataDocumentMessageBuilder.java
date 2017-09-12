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
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 12/09/17
 */
public class MetadataDocumentMessageBuilder {
    public static MetadataDocumentMessageBuilder messageFor(MetadataDocument metadataDocument) {
        MetadataDocumentMessageBuilder builder = null;
        if (metadataDocument instanceof Analysis) {
            builder = new MetadataDocumentMessageBuilder(AnalysisController.class);
        }
        if (metadataDocument instanceof Assay) {
            builder = new MetadataDocumentMessageBuilder(AssayController.class);
        }
        if (metadataDocument instanceof File) {
            builder = new MetadataDocumentMessageBuilder(FileController.class);
        }
        if (metadataDocument instanceof Project) {
            builder = new MetadataDocumentMessageBuilder(ProjectController.class);
        }
        if (metadataDocument instanceof Protocol) {
            builder = new MetadataDocumentMessageBuilder(ProtocolController.class);
        }
        if (metadataDocument instanceof Sample) {
            builder = new MetadataDocumentMessageBuilder(SampleController.class);
        }

        if (builder == null) {
            // couldn't match type
            throw new RuntimeException(String.format(
                    "Unable to make metadata document message - unknown type of doc '%s'",
                    metadataDocument.getClass()));
        }
        else {
            return builder.withDocumentType(metadataDocument.getClass()).withId(metadataDocument.getId());
        }
    }

    private final Class<?> controllerClass;

    private Class<?> documentType;
    private String metadataDocId;

    private MetadataDocumentMessageBuilder(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
    }

    private <T extends MetadataDocument> MetadataDocumentMessageBuilder withDocumentType(Class<T> documentClass) {
        this.documentType = documentClass;

        return this;
    }

    private MetadataDocumentMessageBuilder withId(String metadataDocId) {
        this.metadataDocId = metadataDocId;

        return this;
    }

    public MetadataDocumentMessage getCallbackLink() {
        String callbackLink =
                ControllerLinkBuilder.linkTo(controllerClass).slash(metadataDocId).withSelfRel().getHref();
        return new MetadataDocumentMessage(documentType.getName(), metadataDocId, callbackLink);
    }
}
