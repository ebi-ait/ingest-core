package org.humancellatlas.ingest.errors.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.errors.SubmissionError;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class SubmissionErrorResourceAssembler implements ResourceAssembler<SubmissionError, Resource<SubmissionError>> {
    private final @NonNull EntityLinks entityLinks;

    @Override
    public Resource<SubmissionError> toResource(SubmissionError submissionError) {
        LinkBuilder self = entityLinks.linkForSingleResource(submissionError);
        submissionError.setInstance(self.toUri());
        ArrayList<Link> links = new ArrayList<Link>();
        links.add(self.withSelfRel());
        links.add(self.withRel("submissionError"));
        links.add(entityLinks.linkToSingleResource(submissionError.getSubmissionEnvelope()));
        links.add(entityLinks.linkForSingleResource(submissionError.getSubmissionEnvelope())
                .slash(Links.SUBMISSION_ERRORS_URL)
                .withRel(Links.SUBMISSION_ERRORS_REL));
        return new Resource<SubmissionError>(submissionError, links);
    }
}
