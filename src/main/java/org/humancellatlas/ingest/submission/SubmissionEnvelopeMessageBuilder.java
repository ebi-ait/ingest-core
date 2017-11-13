package org.humancellatlas.ingest.submission;

import org.humancellatlas.ingest.submission.web.SubmissionController;
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
 * @author tburdett
 * @date 12/09/2017
 */
public class SubmissionEnvelopeMessageBuilder {
    public static SubmissionEnvelopeMessageBuilder using(ResourceMappings mappings,
                                                         RepositoryRestConfiguration config) {
        return new SubmissionEnvelopeMessageBuilder(mappings, config);
    }

    private final String DUMMY_BASE_URI = "http://localhost:8080";

    private final ResourceMappings mappings;
    private final RepositoryRestConfiguration config;

    private Class<?> controllerClass;
    private Class<?> documentType;
    private String submissionEnvelopeId;
    private String submissionEnvelopeUuid;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    private SubmissionEnvelopeMessageBuilder(ResourceMappings mappings, RepositoryRestConfiguration config) {
        this.mappings = mappings;
        this.config = config;
    }

    public SubmissionEnvelopeMessageBuilder messageFor(SubmissionEnvelope submissionEnvelope) {
        withControllerClass(SubmissionController.class)
                .withDocumentType(submissionEnvelope.getClass())
                .withId(submissionEnvelope.getId())
                .withUuid(submissionEnvelope.getUuid().getUuid().toString());

        return this;
    }

    private SubmissionEnvelopeMessageBuilder withControllerClass(Class<?> controllerClass) {
        this.controllerClass = controllerClass;

        return this;
    }

    private <T extends SubmissionEnvelope> SubmissionEnvelopeMessageBuilder withDocumentType(Class<T> documentClass) {
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
        // todo - here, we make link with DUMMY_BASE_URI and then take it out again so clients can fill in domain - must be a better way of doing this!
        RepositoryLinkBuilder rlb = new RepositoryLinkBuilder(mappings.getMetadataFor(documentType),
                                                              new BaseUri(URI.create(DUMMY_BASE_URI)));
        Link link = rlb
                .slash(submissionEnvelopeId)
                .withRel(mappings.getMetadataFor(documentType).getItemResourceRel());
        String callbackLink = link.withSelfRel().getHref().replace(DUMMY_BASE_URI, "");

        return new SubmissionEnvelopeMessage(
                documentType.getSimpleName().toLowerCase(),
                submissionEnvelopeId,
                submissionEnvelopeUuid,
                callbackLink);
    }
}
