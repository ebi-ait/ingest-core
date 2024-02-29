package org.humancellatlas.ingest.submission.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submissionmanifest.SubmissionManifest;
import org.humancellatlas.ingest.submissionmanifest.SubmissionManifestRepository;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
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
    private final @NonNull SubmissionManifestRepository submissionManifestRepository;

    private Link getBiomaterialsLink(SubmissionEnvelope submissionEnvelope) {
        return entityLinks.linkForSingleResource(submissionEnvelope)
            .slash(Links.BIOMATERIALS_URL)
            .withRel(Links.BIOMATERIALS_REL);
    }

    private Link getProcessesLink(SubmissionEnvelope submissionEnvelope) {
        return entityLinks.linkForSingleResource(submissionEnvelope)
            .slash(Links.PROCESSES_URL)
            .withRel(Links.PROCESSES_REL);
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

    private Link getStudiesLink(SubmissionEnvelope submissionEnvelope) {
        return entityLinks.linkForSingleResource(submissionEnvelope)
                .slash(Links.STUDIES_URL)
                .withRel(Links.STUDIES_REL);
    }

    private Link getProtocolsLink(SubmissionEnvelope submissionEnvelope) {
        return entityLinks.linkForSingleResource(submissionEnvelope)
                .slash(Links.PROTOCOLS_URL)
                .withRel(Links.PROTOCOLS_REL);
    }

    private Link getBundleManifestsLink(SubmissionEnvelope submissionEnvelope) {
        return entityLinks.linkForSingleResource(submissionEnvelope)
                          .slash(Links.BUNDLE_MANIFESTS_URL)
                          .withRel(Links.BUNDLE_MANIFESTS_REL);
    }

    private Link getSubmissionManifestsLink(SubmissionEnvelope submissionEnvelope) {
        return entityLinks.linkForSingleResource(submissionEnvelope)
                          .slash(Links.SUBMISSION_MANIFEST_URL)
                          .withRel(Links.SUBMISSION_MANIFEST_REL);
    }

    private Link getExportJobsLink(SubmissionEnvelope submissionEnvelope) {
        return entityLinks.linkForSingleResource(submissionEnvelope)
            .slash(Links.EXPORT_JOBS_URL)
            .withRel(Links.EXPORT_JOBS_REL);
    }

    private Link getSubmissionErrorsLink(SubmissionEnvelope submissionEnvelope) {
        return entityLinks.linkForSingleResource(submissionEnvelope)
                .slash(Links.SUBMISSION_ERRORS_URL)
                .withRel(Links.SUBMISSION_ERRORS_REL);
    }

    private Link getSubmissionSummary(SubmissionEnvelope submissionEnvelope) {
        return entityLinks.linkForSingleResource(submissionEnvelope)
                .slash(Links.SUBMISSION_SUMMARY_URL)
                .withRel(Links.SUBMISSION_SUMMARY_REL);
    }

    private Link getSubmissionLinkingMap(SubmissionEnvelope submissionEnvelope) {
        return entityLinks.linkForSingleResource(submissionEnvelope)
            .slash(Links.SUBMISSION_LINKING_MAP_URL)
            .withRel(Links.SUBMISSION_LINKING_MAP_REL);
    }

    private Link getSubmissionContentLastUpdatedLink(SubmissionEnvelope submissionEnvelope) {
        return entityLinks.linkForSingleResource(submissionEnvelope)
                .slash(Links.SUBMISSION_CONTENT_LAST_UPDATED_URL)
                .withRel(Links.SUBMISSION_CONTENT_LAST_UPDATED_REL);
    }

    private Link getSubmissionRelatedProjectLink(SubmissionEnvelope submissionEnvelope) {
        return entityLinks.linkForSingleResource(submissionEnvelope)
                .slash(Links.SUBMISSION_RELATED_PROJECTS_URL)
                .withRel(Links.SUBMISSION_RELATED_PROJECTS_REL);
    }

    private Link getSubmissionDocumentStateLink(SubmissionEnvelope submissionEnvelope) {
        return entityLinks.linkForSingleResource(submissionEnvelope)
                          .slash(Links.SUBMISSION_DOCUMENTS_SM_URL)
                          .withRel(Links.SUBMISSION_DOCUMENTS_SM_REL);
    }

    private Optional<Link> getStateTransitionLink(SubmissionEnvelope submissionEnvelope, SubmissionState targetState) {
        Optional<String> transitionResourceName = getSubresourceNameForRequestSubmissionState(submissionEnvelope, targetState);
        if (transitionResourceName.isPresent()) {
            Optional<String> rel = getRelNameForRequestSubmissionState(targetState);
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

    private Optional<Link> getCommitStateTransitionLink(SubmissionEnvelope submissionEnvelope, SubmissionState targetState) {
        Optional<String> transitionResourceName = getSubresourceNameForCommitSubmissionState(targetState);
        if (transitionResourceName.isPresent()) {
            Optional<String> rel = getRelNameForCommitSubmissionState(targetState);
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

    private Optional<String> getRelNameForRequestSubmissionState(SubmissionState submissionState) {
        switch (submissionState) {
            case GRAPH_VALIDATION_REQUESTED:
                return Optional.of(Links.GRAPH_VALIDATION_REQUESTED_REL);
            case GRAPH_VALIDATING:
                return Optional.of(Links.GRAPH_VALIDATING_REL);
            case GRAPH_VALID:
                return Optional.of(Links.GRAPH_VALID_REL);
            case GRAPH_INVALID:
                return Optional.of(Links.GRAPH_INVALID_REL);
            case SUBMITTED:
                return Optional.of(Links.SUBMIT_REL);
            case ARCHIVED:
                return Optional.of(Links.ARCHIVED_REL);
            case EXPORTING:
                return Optional.of(Links.EXPORT_REL);
            case CLEANUP:
                return Optional.of(Links.CLEANUP_REL);
            case COMPLETE:
                return Optional.of(Links.COMPLETE_REL);
            default:
                // default returns no links (not expecting external user interaction)
                return Optional.empty();
        }
    }

    private Optional<String> getSubmitLink(SubmissionEnvelope submissionEnvelope){
        SubmissionManifest submissionManifest = this.submissionManifestRepository.findBySubmissionEnvelopeId(submissionEnvelope.getId());

        if(submissionManifest == null)
            return Optional.of(Links.SUBMIT_URL);
        else if(submissionManifest.getExpectedLinks() !=null && submissionManifest.getExpectedLinks().equals(submissionManifest.getActualLinks())){
            return Optional.of(Links.SUBMIT_URL);
        } else {
            return Optional.empty();
        }
    }

    private Optional<String> getSubresourceNameForRequestSubmissionState(SubmissionEnvelope submissionEnvelope, SubmissionState submissionState) {
        switch (submissionState) {
            case SUBMITTED:
                return this.getSubmitLink(submissionEnvelope);
            case ARCHIVED:
                return Optional.of(Links.ARCHIVED_URL);
            case EXPORTING:
                return Optional.of(Links.EXPORT_URL);
            case CLEANUP:
                return Optional.of(Links.CLEANUP_URL);
            case COMPLETE:
                return Optional.of(Links.COMPLETE_URL);
            default:
                // default returns no subresource name (not expecting external user interaction)
                return Optional.empty();
        }
    }

    private Optional<String> getRelNameForCommitSubmissionState(SubmissionState submissionState) {
        switch (submissionState) {
            case DRAFT:
                return Optional.of(Links.COMMIT_DRAFT_REL);
            case METADATA_VALIDATING:
                return Optional.of(Links.COMMIT_METADATA_VALIDATING_REL);
            case METADATA_INVALID:
                return Optional.of(Links.COMMIT_METADATA_INVALID_REL);
            case METADATA_VALID:
                return Optional.of(Links.COMMIT_METADATA_VALID_REL);
            case GRAPH_VALIDATION_REQUESTED:
                return Optional.of(Links.COMMIT_GRAPH_VALIDATION_REQUESTED_REL);
            case GRAPH_VALIDATING:
                return Optional.of(Links.COMMIT_GRAPH_VALIDATING_REL);
            case GRAPH_VALID:
                return Optional.of(Links.COMMIT_GRAPH_VALID_REL);
            case GRAPH_INVALID:
                return Optional.of(Links.COMMIT_GRAPH_INVALID_REL);
            case SUBMITTED:
                return Optional.of(Links.COMMIT_SUBMIT_REL);
            case PROCESSING:
                return Optional.of(Links.COMMIT_PROCESSING_REL);
            case ARCHIVING:
                return Optional.of(Links.COMMIT_ARCHIVING_REL);
            case ARCHIVED:
                return Optional.of(Links.COMMIT_ARCHIVED_REL);
            case EXPORTING:
                return Optional.of(Links.COMMIT_EXPORTING_REL);
            case EXPORTED:
                return Optional.of(Links.COMMIT_EXPORTED_REL);
            case CLEANUP:
                return Optional.of(Links.COMMIT_CLEANUP_REL);
            case COMPLETE:
                return Optional.of(Links.COMMIT_COMPLETE_REL);
            default:
                // default returns no links (not expecting external user interaction)
                return Optional.empty();
        }
    }

    private Optional<String> getSubresourceNameForCommitSubmissionState(SubmissionState submissionState) {
        switch (submissionState) {
            case DRAFT:
                return Optional.of(Links.COMMIT_DRAFT_URL);
            case METADATA_VALIDATING:
                return Optional.of(Links.COMMIT_METADATA_VALIDATING_URL);
            case METADATA_INVALID:
                return Optional.of(Links.COMMIT_METADATA_INVALID_URL);
            case METADATA_VALID:
                return Optional.of(Links.COMMIT_METADATA_VALID_URL);
            case GRAPH_VALIDATION_REQUESTED:
                return Optional.of(Links.COMMIT_GRAPH_VALIDATION_REQUESTED_URL);
            case GRAPH_VALIDATING:
                return Optional.of(Links.COMMIT_GRAPH_VALIDATING_URL);
            case GRAPH_VALID:
                return Optional.of(Links.COMMIT_GRAPH_VALID_URL);
            case GRAPH_INVALID:
                return Optional.of(Links.COMMIT_GRAPH_INVALID_URL);
            case SUBMITTED:
                return Optional.of(Links.COMMIT_SUBMIT_URL);
            case PROCESSING:
                return Optional.of(Links.COMMIT_PROCESSING_URL);
            case ARCHIVING:
                return Optional.of(Links.COMMIT_ARCHIVING_URL);
            case ARCHIVED:
                return Optional.of(Links.COMMIT_ARCHIVED_URL);
            case EXPORTING:
                return Optional.of(Links.COMMIT_EXPORTING_URL);
            case EXPORTED:
                return Optional.of(Links.COMMIT_EXPORTED_URL);
            case CLEANUP:
                return Optional.of(Links.COMMIT_CLEANUP_URL);
            case COMPLETE:
                return Optional.of(Links.COMMIT_COMPLETE_URL);
            default:
                // default returns no subresource name (not expecting external user interaction)
                return Optional.empty();
        }
    }

    @Override
    public Resource<SubmissionEnvelope> process(Resource<SubmissionEnvelope> resource) {
        SubmissionEnvelope submissionEnvelope = resource.getContent();

        // add subresource links for each type of metadata document in a submission envelope
        resource.add(getBiomaterialsLink(submissionEnvelope));
        resource.add(getProcessesLink(submissionEnvelope));
        resource.add(getFilesLink(submissionEnvelope));
        resource.add(getProjectsLink(submissionEnvelope));
        resource.add(getStudiesLink(submissionEnvelope));
        resource.add(getProtocolsLink(submissionEnvelope));
        resource.add(getBundleManifestsLink(submissionEnvelope));
        resource.add(getSubmissionManifestsLink(submissionEnvelope));
        resource.add(getExportJobsLink(submissionEnvelope));
        resource.add(getSubmissionErrorsLink(submissionEnvelope));
        resource.add(getSubmissionDocumentStateLink(submissionEnvelope));
        resource.add(getSubmissionSummary(submissionEnvelope));
        resource.add(getSubmissionLinkingMap(submissionEnvelope));
        resource.add(getSubmissionContentLastUpdatedLink(submissionEnvelope));
        resource.add(getSubmissionRelatedProjectLink(submissionEnvelope));

        // add subresource links for allowed state transition requests
        submissionEnvelope.allowedSubmissionStateTransitions().stream()
                .map(submissionState -> getStateTransitionLink(submissionEnvelope, submissionState))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(resource::add);

        // add subresource links for state tracker to commit state transitions
        Arrays.stream(SubmissionState.values())
              .map(submissionState -> getCommitStateTransitionLink(submissionEnvelope, submissionState))
              .filter(Optional::isPresent)
              .map(Optional::get)
              .forEach(resource::add);

        return resource;
    }
}
