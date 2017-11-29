package org.humancellatlas.ingest.project.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.Event;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectService;
import org.humancellatlas.ingest.state.StateEngine;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 05/09/17
 */
@RepositoryRestController
@ExposesResourceFor(Project.class)
@RequiredArgsConstructor
@Getter
public class ProjectController {
    private final @NonNull ProjectService projectService;
    private final @NonNull StateEngine stateEngine;

    @RequestMapping(path = "submissionEnvelopes/{sub_id}/projects", method = RequestMethod.POST)
    ResponseEntity<Resource<?>> addProjectToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                                     @RequestBody Project project,
                                                     PersistentEntityResourceAssembler assembler) {
        Project entity = getProjectService().addProjectToSubmissionEnvelope(submissionEnvelope, project);
        PersistentEntityResource resource = assembler.toFullResource(entity);
        return ResponseEntity.accepted().body(resource);
    }

    @RequestMapping(path = "submissionEnvelopes/{sub_id}/projects/{id}", method = RequestMethod.PUT)
    ResponseEntity<Resource<?>> linkProjectToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                                      @PathVariable("id") Project project,
                                                     PersistentEntityResourceAssembler assembler) {
        Project entity = getProjectService().addProjectToSubmissionEnvelope(submissionEnvelope, project);
        PersistentEntityResource resource = assembler.toFullResource(entity);
        return ResponseEntity.accepted().body(resource);
    }

    @RequestMapping(path = "/projects/{id}" + Links.VALIDATING_URL, method = RequestMethod.PUT)
    HttpEntity<?> validatingProject(@PathVariable("id") Project project) {
        Event event = this.getStateEngine().advanceStateOfMetadataDocument(
                getProjectService().getProjectRepository(),
                project,
                ValidationState.VALIDATING);

        return ResponseEntity.accepted().body(event);
    }

    @RequestMapping(path = "/projects/{id}" + Links.VALID_URL, method = RequestMethod.PUT)
    HttpEntity<?> validateProject(@PathVariable("id") Project project) {
        Event event = this.getStateEngine().advanceStateOfMetadataDocument(
                getProjectService().getProjectRepository(),
                project,
                ValidationState.VALID);

        return ResponseEntity.accepted().body(event);
    }

    @RequestMapping(path = "/projects/{id}" + Links.INVALID_URL, method = RequestMethod.PUT)
    HttpEntity<?> invalidateProject(@PathVariable("id") Project project) {
        Event event = this.getStateEngine().advanceStateOfMetadataDocument(
                getProjectService().getProjectRepository(),
                project,
                ValidationState.INVALID);

        return ResponseEntity.accepted().body(event);
    }

    @RequestMapping(path = "/projects/{id}" + Links.PROCESSING_URL, method = RequestMethod.PUT)
    HttpEntity<?> processingProject(@PathVariable("id") Project project) {
        Event event = this.getStateEngine().advanceStateOfMetadataDocument(
                getProjectService().getProjectRepository(),
                project,
                ValidationState.PROCESSING);

        return ResponseEntity.accepted().body(event);
    }

    @RequestMapping(path = "/projects/{id}" + Links.COMPLETE_URL, method = RequestMethod.PUT)
    HttpEntity<?> completeProject(@PathVariable("id") Project project) {
        Event event = this.getStateEngine().advanceStateOfMetadataDocument(
                getProjectService().getProjectRepository(),
                project,
                ValidationState.COMPLETE);

        return ResponseEntity.accepted().body(event);
    }
}
