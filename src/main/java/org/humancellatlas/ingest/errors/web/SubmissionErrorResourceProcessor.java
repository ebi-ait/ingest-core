package org.humancellatlas.ingest.errors.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.errors.SubmissionError;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class SubmissionErrorResourceProcessor implements ResourceProcessor<Resource<SubmissionError>> {
    private final @NonNull EntityLinks entityLinks;

    @Override
    public Resource<SubmissionError> process(Resource<SubmissionError> resource) {
        resource.getContent().setInstance(URI.create(resource.getId().getHref()));
        resource.add(
            entityLinks.linkForSingleResource(resource.getContent().getSubmissionEnvelope())
                .slash(Links.SUBMISSION_ERRORS_URL)
                .withRel(Links.SUBMISSION_ERRORS_REL)
        );
        return resource;
    }
}
