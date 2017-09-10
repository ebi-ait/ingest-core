package org.humancellatlas.ingest.submission.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionState;
import org.humancellatlas.ingest.submission.state.InvalidSubmissionStateException;
import org.humancellatlas.ingest.submission.state.SubmissionEnvelopeStateEngine;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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

    private Link getStateTransitionLink(SubmissionEnvelope submissionEnvelope, SubmissionState targetState) {
        String transitionResourceName = getSubresourceNameForSubmissionState(targetState);
        String rel = getRelNameForSubmissionState(targetState);
        return entityLinks.linkForSingleResource(submissionEnvelope)
                .slash(transitionResourceName)
                .withRel(rel);
    }

    private String getRelNameForSubmissionState(SubmissionState submissionState) {
        switch (submissionState) {
            case DRAFT:
                return Links.DRAFT_REL;
            case VALIDATING:
                return Links.VALIDATING_REL;
            case VALID:
                return Links.VALID_REL;
            case INVALID:
                return Links.INVALID_REL;
            case SUBMITTED:
                return Links.SUBMIT_REL;
            case PROCESSING:
                return Links.PROCESSING_REL;
            case CLEANUP:
                return "mark-cleaning";
            case COMPLETE:
                return "mark-complete";
            default:
                throw new InvalidSubmissionStateException(String.format("The submission state '%s' is not recognised " +
                        "as a submission envelope state that can be set", submissionState.name()));
        }
    }

    private String getSubresourceNameForSubmissionState(SubmissionState submissionState) {
        switch (submissionState) {
            case DRAFT:
                return "/draftState";
            case VALIDATING:
                return "/validatingState";
            case VALID:
                return "/validState";
            case INVALID:
                return "/invalidState";
            case SUBMITTED:
                return "/submittedState";
            case PROCESSING:
                return "/processingState";
            case CLEANUP:
                return "/cleanupState";
            case COMPLETE:
                return "/completeState";
            default:
                throw new InvalidSubmissionStateException(String.format("The submission state '%s' is not recognised " +
                        "as a submission envelope state that can be set", submissionState.name()));
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
                .forEach(resource::add);

        return resource;
    }
}
