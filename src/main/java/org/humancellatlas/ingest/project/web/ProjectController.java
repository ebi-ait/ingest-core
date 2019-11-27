package org.humancellatlas.ingest.project.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.bundle.BundleType;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectService;
import org.humancellatlas.ingest.query.MetadataCriteria;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    private final @NonNull SubmissionEnvelopeService submissionEnvelopeService;
    private final @NonNull PagedResourcesAssembler pagedResourcesAssembler;

    @RequestMapping(path = "submissionEnvelopes/{sub_id}/projects", method = RequestMethod.POST)
    ResponseEntity<Resource<?>> addProjectToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                                     @RequestBody Project project,
                                                     @RequestParam("updatingUuid") Optional<UUID> updatingUuid,
                                                     PersistentEntityResourceAssembler assembler) {
        updatingUuid.ifPresent(uuid -> {
            project.setUuid(new Uuid(uuid.toString()));
            project.setIsUpdate(true);
        });
        Project entity = getProjectService().addProjectToSubmissionEnvelope(submissionEnvelope, project);
        PersistentEntityResource resource = assembler.toFullResource(entity);
        return ResponseEntity.accepted().body(resource);
    }

    @RequestMapping(path = "project/{id}/relatedSubmissionEnvelopes/{sub_id}", method = RequestMethod.PUT)
    ResponseEntity<Resource<?>> linkToProject(@PathVariable("id") Project project,
                                              @PathVariable("sub_id") SubmissionEnvelope envelope,
                                              PersistentEntityResourceAssembler assembler) {

        SubmissionEnvelope submissionEnvelope = submissionEnvelopeService.linkSubmissionToProject(envelope, project);
        PersistentEntityResource resource = assembler.toFullResource(submissionEnvelope);
        return ResponseEntity.accepted().body(resource);
    }

    @RequestMapping(path = "project/{id}/relatedSubmissionEnvelopes", method = RequestMethod.POST)
    ResponseEntity<Resource<?>> createSubmissionAndLinkToProject(@PathVariable("id") Project project,
                                                                 @RequestBody SubmissionEnvelope envelope,
                                                                 PersistentEntityResourceAssembler assembler) {

        SubmissionEnvelope submissionEnvelope = submissionEnvelopeService.createSubmissionAndLinkToProject(envelope, project);
        PersistentEntityResource resource = assembler.toFullResource(submissionEnvelope);
        return ResponseEntity.accepted().body(resource);
    }

    @RequestMapping(path = "/projects/{id}/bundleManifests", method = RequestMethod.GET)
    ResponseEntity<?> getBundleManifests(@PathVariable("id") Project project,
                                         @RequestParam("bundleType") Optional<BundleType> bundleType,
                                         Pageable pageable,
                                         final PersistentEntityResourceAssembler resourceAssembler) {
        Page<BundleManifest> bundleManifests = projectService.getBundleManifestRepository().findBundleManifestsByProjectAndBundleType(project, bundleType.orElse(null), pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toResource(bundleManifests, resourceAssembler));
    }

    @RequestMapping(path = "/projects/query", method = RequestMethod.POST)
    ResponseEntity<?> queryProjects(@RequestBody List<MetadataCriteria> query,
                                    Pageable pageable,
                                    final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Project> projects = projectService.queryByContent(query, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toResource(projects, resourceAssembler));
    }
}