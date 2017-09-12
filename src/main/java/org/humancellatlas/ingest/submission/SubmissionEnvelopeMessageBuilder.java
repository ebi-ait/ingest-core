package org.humancellatlas.ingest.submission;

import org.humancellatlas.ingest.submission.web.SubmissionController;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;

/**
 * Javadocs go here!
 *
 * @author tburdett
 * @date 12/09/2017
 */
public class SubmissionEnvelopeMessageBuilder {
    public static SubmissionEnvelopeMessageBuilder usingLinkBuilder(RepositoryEntityLinks repositoryEntityLinks) {
        return new SubmissionEnvelopeMessageBuilder(repositoryEntityLinks);
    }

    private final RepositoryEntityLinks repositoryEntityLinks;

    private Class<?> controllerClass;
    private Class<?> documentType;
    private String submissionEnvelopeId;
    private String submissionEnvelopeUuid;

    private SubmissionEnvelopeMessageBuilder(RepositoryEntityLinks repositoryEntityLinks) {
        this.repositoryEntityLinks = repositoryEntityLinks;
    }

    public SubmissionEnvelopeMessageBuilder messageFor(SubmissionEnvelope submissionEnvelope) {
        withControllerClass(SubmissionController.class)
                .withDocumentType(submissionEnvelope.getClass())
                .withId(submissionEnvelope.getId());

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
        String callbackLink = repositoryEntityLinks.linkToSingleResource(documentType, submissionEnvelopeId).withSelfRel().getHref();

        // todo make link relative so clients can fill in domain - must be a better way of doing this!
        callbackLink = callbackLink.replace("http://localhost:8080", "");

        return new SubmissionEnvelopeMessage(
                documentType.getSimpleName().toLowerCase(),
                submissionEnvelopeId,
                submissionEnvelopeUuid,
                callbackLink);
    }
}
