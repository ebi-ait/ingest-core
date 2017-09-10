package org.humancellatlas.ingest.submission.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
@Component
@RequiredArgsConstructor
public class SubmissionEnvelopeResourceProcessor implements ResourceProcessor<Resource<SubmissionEnvelope>> {
    private final @NonNull EntityLinks entityLinks;

    private Link getSubmitLink(SubmissionEnvelope submissionEnvelope) {
        return entityLinks.linkForSingleResource(submissionEnvelope).slash(Links.SUBMIT_URL).withRel(Links.SUBMIT_REL);
    }

    private Link getAnalysesLink(SubmissionEnvelope submissionEnvelope) {
        return entityLinks.linkForSingleResource(submissionEnvelope)
                .slash(Links.ANALYSES_URL)
                .withRel(Links.ANALYSES_REL);
    }

    private Link getAssaysLink(SubmissionEnvelope submissionEnvelope) {
        return entityLinks.linkForSingleResource(submissionEnvelope)
                .slash(Links.ASSAYS_URL)
                .withRel(Links.ASSAYS_REL);
    }

    private Link getFilesLink(SubmissionEnvelope submissionEnvelope) {
        return entityLinks.linkForSingleResource(submissionEnvelope)
                .slash(Links.FILES_URL)
                .withRel(Links.FILES_REL);
    }

    private Link getProjectsLink(SubmissionEnvelope submissionEnvelope) {
        return entityLinks.linkForSingleResource(submissionEnvelope)
                .slash(Links.PROJECTS_URL)
                .withRel(Links.PROJECTS_REL);
    }

    private Link getProtocolsLink(SubmissionEnvelope submissionEnvelope) {
        return entityLinks.linkForSingleResource(submissionEnvelope)
                .slash(Links.PROTOCOLS_URL)
                .withRel(Links.PROTOCOLS_REL);
    }

    private Link getSamplesLink(SubmissionEnvelope submissionEnvelope) {
        return entityLinks.linkForSingleResource(submissionEnvelope)
                .slash(Links.SAMPLES_URL)
                .withRel(Links.SAMPLES_REL);
    }

    public Resource<SubmissionEnvelope> process(Resource<SubmissionEnvelope> resource) {
        SubmissionEnvelope submissionEnvelope = resource.getContent();

        resource.add(getAnalysesLink(submissionEnvelope));
        resource.add(getAssaysLink(submissionEnvelope));
        resource.add(getFilesLink(submissionEnvelope));
        resource.add(getProjectsLink(submissionEnvelope));
        resource.add(getProtocolsLink(submissionEnvelope));
        resource.add(getSamplesLink(submissionEnvelope));

        // should be dependent on validation state
        resource.add(getSubmitLink(submissionEnvelope));

        return resource;
    }
}
