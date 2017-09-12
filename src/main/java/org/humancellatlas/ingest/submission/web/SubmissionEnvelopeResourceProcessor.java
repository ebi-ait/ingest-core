package org.humancellatlas.ingest.submission.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.state.SubmissionState;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import java.util.Optional;

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

    private Optional<Link> getStateTransitionLink(SubmissionEnvelope submissionEnvelope, SubmissionState targetState) {
        Optional<String> transitionResourceName = getSubresourceNameForSubmissionState(targetState);
        if (transitionResourceName.isPresent()) {
            Optional<String> rel = getRelNameForSubmissionState(targetState);
            if (rel.isPresent()) {
                return Optional.of(entityLinks.linkForSingleResource(submissionEnvelope)
                        .slash(transitionResourceName.get())
                        .withRel(rel.get()));
            } else {
                throw new RuntimeException(String.format("Unexpected link/rel mismatch exception (link = '%s', rel = " +
                        "'%s')", transitionResourceName.toString(), rel.toString()));
            }
        } else {
            return Optional.empty();
        }
    }

    private Optional<String> getRelNameForSubmissionState(SubmissionState submissionState) {
        switch (submissionState) {
            case SUBMITTED:
                return Optional.of(Links.SUBMIT_REL);
            case PROCESSING:
                return Optional.of(Links.PROCESSING_REL);
            case CLEANUP:
                return Optional.of(Links.CLEANUP_REL);
            case COMPLETE:
                return Optional.of(Links.COMPLETE_REL);
            default:
                // default returns no links (not expecting external user interaction)
                return Optional.empty();
        }
    }

    private Optional<String> getSubresourceNameForSubmissionState(SubmissionState submissionState) {
        switch (submissionState) {
            case SUBMITTED:
                return Optional.of(Links.SUBMIT_URL);
            case PROCESSING:
                return Optional.of(Links.PROCESSING_URL);
            case CLEANUP:
                return Optional.of(Links.CLEANUP_URL);
            case COMPLETE:
                return Optional.of(Links.COMPLETE_URL);
            default:
                // default returns no subresource name (not expecting external user interaction)
                return Optional.empty();
        }
    }

    public Resource<SubmissionEnvelope> process(Resource<SubmissionEnvelope> resource) {
        SubmissionEnvelope submissionEnvelope = resource.getContent();

        // add subresource links for each type of metadata document in a submission envelope
        resource.add(getAnalysesLink(submissionEnvelope));
        resource.add(getAssaysLink(submissionEnvelope));
        resource.add(getFilesLink(submissionEnvelope));
        resource.add(getProjectsLink(submissionEnvelope));
        resource.add(getProtocolsLink(submissionEnvelope));
        resource.add(getSamplesLink(submissionEnvelope));

        // add subresource links for events that occur in response to state transitions
        submissionEnvelope.allowedStateTransitions().stream()
                .map(submissionState -> getStateTransitionLink(submissionEnvelope, submissionState))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(resource::add);

        return resource;
    }
}
