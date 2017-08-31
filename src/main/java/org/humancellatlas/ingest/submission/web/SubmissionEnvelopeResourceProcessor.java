package org.humancellatlas.ingest.submission.web;

import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.envelope.SubmissionEnvelope;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
@Component
@RequiredArgsConstructor
public class SubmissionEnvelopeResourceProcessor implements ResourceProcessor<Resource<SubmissionEnvelope>> {
    private final @NotNull EntityLinks entityLinks;

    Link getSubmitLink(SubmissionEnvelope submissionEnvelope) {
        return entityLinks.linkForSingleResource(submissionEnvelope).slash("/submit").withRel("submit");
    }

    public Resource<SubmissionEnvelope> process(Resource<SubmissionEnvelope> envelopeResource) {
        envelopeResource.add(getSubmitLink(envelopeResource.getContent()));
        return envelopeResource;
    }
}
