package org.humancellatlas.ingest.project.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.bundle.BundleType;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.core.service.ValidationStateChangeService;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectEventHandler;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.project.ProjectService;
import org.humancellatlas.ingest.project.exception.NonEmptyProject;
import org.humancellatlas.ingest.project.exception.NotAllowedWithSubmissionInStateException;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.security.CheckAllowed;
import org.humancellatlas.ingest.security.Role;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.exception.NotAllowedDuringSubmissionStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectController.class);

    private final @NonNull ProjectService projectService;

    private final @NonNull ValidationStateChangeService validationStateChangeService;

    private final @NonNull ProjectEventHandler projectEventHandler;

    private final @NonNull PagedResourcesAssembler pagedResourcesAssembler;

    private final @NonNull ProjectRepository projectRepository;
    private final @NonNull BiomaterialRepository biomaterialRepository;
    private final @NonNull ProcessRepository processRepository;
    private final @NonNull ProtocolRepository protocolRepository;
    private final @NonNull FileRepository fileRepository;

    private final @NonNull MetadataUpdateService metadataUpdateService;

    @PostMapping("/projects")
    ResponseEntity<Resource<?>> register(@RequestBody final Project project,
                                         final PersistentEntityResourceAssembler assembler) {
        Project result = projectService.register(project);
        return ResponseEntity.ok().body(assembler.toFullResource(result));
    }

    @PostMapping("/projects/suggestion")
    ResponseEntity<Resource<?>> suggest(@RequestBody final ObjectNode suggestion,
                                         final PersistentEntityResourceAssembler assembler) {
        Project suggestedProject = projectService.createSuggestedProject(suggestion);
        return ResponseEntity.ok().body(assembler.toFullResource(suggestedProject));
    }

    @CheckAllowed(value = "#project.isEditable()", exception = NotAllowedWithSubmissionInStateException.class)
    @PatchMapping("/projects/{id}")
    ResponseEntity<Resource<?>> update(@PathVariable("id") final Project project,
                                       @RequestParam(value = "partial", defaultValue = "false") Boolean partial,
                                       @RequestBody final ObjectNode patch, final PersistentEntityResourceAssembler assembler) {

        List<String> allowedFields = List.of(
                "accessionDate",
                "cellCount",
                "content",
                "dataAccess",
                "identifyingOrganisms",
                "isInCatalogue",
                "organ",
                "primaryWrangler",
                "publicationsInfo",
                "releaseDate",
                "secondaryWrangler",
                "technology",
                "validationErrors",
                "wranglingState",
                "wranglingPriority",
                "wranglingNotes",
                "dcpReleaseNumber",
                "projectLabels",
                "projectNetworks"
        );

        ObjectNode validPatch = patch.retain(allowedFields);
        Project updatedProject = projectService.update(project, validPatch, !partial);
        return ResponseEntity.ok().body(assembler.toFullResource(updatedProject));
    }

    @CheckAllowed(value = "#submissionEnvelope.isSystemEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @PostMapping(path = "submissionEnvelopes/{sub_id}/projects")
    ResponseEntity<Resource<?>> addProjectToEnvelope(
            @PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
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

    @GetMapping(path = "/projects/{id}/bundleManifests")
    ResponseEntity<PagedResources<Resource<BundleManifest>>> getBundleManifests(
            @PathVariable("id") Project project,
            @RequestParam("bundleType") Optional<BundleType> bundleType,
            Pageable pageable,
            final PersistentEntityResourceAssembler resourceAssembler) {
        Page<BundleManifest> bundleManifests = projectService.getBundleManifestRepository().findBundleManifestsByProjectAndBundleType(project, bundleType.orElse(null), pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toResource(bundleManifests, resourceAssembler));
    }

    @GetMapping(path = "/projects/{id}/submissionEnvelopes")
    ResponseEntity<PagedResources<Resource<SubmissionEnvelope>>> getProjectSubmissionEnvelopes(
            @PathVariable("id") Project project, Pageable pageable,
            final PersistentEntityResourceAssembler resourceAssembler) {

        if (hasRoleWranglerOrService()) {
            var envelopes = projectService.getSubmissionEnvelopes(project);
            var resultPage = new PageImpl<>(new ArrayList<>(envelopes), pageable, envelopes.size());
            return ResponseEntity.ok(pagedResourcesAssembler.toResource(resultPage, resourceAssembler));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    private boolean hasRoleWranglerOrService() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            LOGGER.error("Authentication is null");
        } else {
            LOGGER.info("Authenticated user: " + authentication.getName());
            LOGGER.info("Authenticated user details: " + authentication.getDetails());
            LOGGER.info("Authenticated user principal: " + authentication.getPrincipal());
            LOGGER.info("Authenticated user authorities: " + authentication.getAuthorities());
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        (authority instanceof Role &&
                                (authority.equals(Role.WRANGLER) || authority.equals(Role.SERVICE))) ||
                                authority.getAuthority().equals("ROLE_WRANGLER") ||
                                authority.getAuthority().equals("ROLE_SERVICE")

                );
    }

    @RequestMapping(path = "/projects/{project_id}/biomaterials", method = RequestMethod.GET)
    ResponseEntity<?> getBiomaterials(@PathVariable("project_id") Project project,
                                      Pageable pageable,
                                      final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Biomaterial> biomaterials = getBiomaterialRepository().findByProject(project, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(biomaterials, resourceAssembler));
    }

    @RequestMapping(path = "/projects/{project_id}/processes", method = RequestMethod.GET)
    ResponseEntity<?> getProcesses(@PathVariable("project_id") Project project,
                                   Pageable pageable,
                                   final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Process> processes = getProcessRepository().findByProject(project, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(processes, resourceAssembler));
    }

    @RequestMapping(path = "/projects/{project_id}/protocols", method = RequestMethod.GET)
    ResponseEntity<?> getProtocols(@PathVariable("project_id") Project project,
                                   Pageable pageable,
                                   final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Protocol> protocols = getProtocolRepository().findByProject(project, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(protocols, resourceAssembler));
    }

    @RequestMapping(path = "/projects/{project_id}/files", method = RequestMethod.GET)
    ResponseEntity<?> getFiles(@PathVariable("project_id") Project project,
                               Pageable pageable,
                               final PersistentEntityResourceAssembler resourceAssembler) {
        Page<File> files = getFileRepository().findByProject(project, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(files, resourceAssembler));
    }

    @CheckAllowed(value = "#submissionEnvelope.isSystemEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @PutMapping(path = "projects/{proj_id}/submissionEnvelopes/{sub_id}")
    ResponseEntity<Resource<?>> linkSubmissionToProject(
            @PathVariable("proj_id") Project project,
            @PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
            PersistentEntityResourceAssembler assembler) {
        Project savedProject = getProjectService().linkProjectSubmissionEnvelope(submissionEnvelope, project);
        PersistentEntityResource projectResource = assembler.toFullResource(savedProject);
        return ResponseEntity.accepted().body(projectResource);
    }

    @CheckAllowed(value = "#project.isEditable()", exception = NotAllowedWithSubmissionInStateException.class)
    @DeleteMapping(path = "projects/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Project project) {
        try {
            projectService.delete(project);
            return ResponseEntity.noContent().build();
        } catch (NonEmptyProject nonEmptyProject) {
            String message = nonEmptyProject.getMessage();
            LOGGER.debug(message);
            Map<String, String> errorResponse = Map.of("message", message);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
    }

    @GetMapping(path = "projects/filter")
    public ResponseEntity<PagedResources<Resource<Project>>> filterProjects(
            @ModelAttribute SearchFilter searchFilter,
            Pageable pageable,
            final PersistentEntityResourceAssembler resourceAssembler) {
        var projects = projectService.filterProjects(searchFilter, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toResource(projects, resourceAssembler));
    }

    @GetMapping(path="projects/{id}/auditLogs")
    public ResponseEntity<?> getProjectAuditLogs(@PathVariable("id") Project project) {
        if (project == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(projectService.getProjectAuditEntries(project));
    }
}
