package uk.ac.ebi.subs.ingest.export.job.web;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.core.web.Links;
import uk.ac.ebi.subs.ingest.export.ExportState;
import uk.ac.ebi.subs.ingest.export.destination.ExportDestinationName;
import uk.ac.ebi.subs.ingest.export.job.ExportJob;
import uk.ac.ebi.subs.ingest.export.job.ExportJobRepository;
import uk.ac.ebi.subs.ingest.export.job.ExportJobService;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

@RepositoryRestController
@RequiredArgsConstructor
@ExposesResourceFor(ExportJob.class)
public class ExportJobController {
  private final ExportJobService exportJobService;
  private final ExportJobRepository exportJobRepository;
  private final PagedResourcesAssembler pagedResourcesAssembler;

  @GetMapping(path = "/submissionEnvelopes/{id}" + Links.EXPORT_JOBS_URL)
  ResponseEntity<?> getExportJobsForSubmission(
      @PathVariable("id") SubmissionEnvelope submission,
      Pageable pageable,
      PersistentEntityResourceAssembler resourceAssembler) {
    if (submission == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(
        pagedResourcesAssembler.toResource(
            exportJobRepository.findBySubmission(submission, pageable), resourceAssembler));
  }

  @PostMapping(path = "/submissionEnvelopes/{id}" + Links.EXPORT_JOBS_URL)
  ResponseEntity<PersistentEntityResource> createExportJob(
      @PathVariable("id") SubmissionEnvelope submission,
      @RequestBody ExportJobRequest exportJobRequest,
      PersistentEntityResourceAssembler resourceAssembler) {
    if (submission == null) {
      return ResponseEntity.notFound().build();
    }
    ExportJob newExportJob = exportJobService.createExportJob(submission, exportJobRequest);
    PersistentEntityResource newExportJobResource = resourceAssembler.toFullResource(newExportJob);
    return ResponseEntity.created(URI.create(newExportJobResource.getId().getHref()))
        .body(newExportJobResource);
  }

  @GetMapping(path = Links.EXPORT_JOBS_URL + "/search" + Links.EXPORT_JOB_FIND_URL)
  ResponseEntity<PagedResources<ExportJob>> findExportJobs(
      @RequestParam("submissionUuid") UUID submissionUuid,
      @RequestParam("status") ExportState exportState,
      @RequestParam("destination") ExportDestinationName exportDestinationName,
      @RequestParam("version") String destinationVersion,
      Pageable pageable,
      PersistentEntityResourceAssembler resourceAssembler) {
    String version = destinationVersion.isEmpty() ? null : destinationVersion;
    Page<ExportJob> searchResults =
        exportJobService.find(
            submissionUuid, exportState, exportDestinationName, version, pageable);
    return ResponseEntity.ok(pagedResourcesAssembler.toResource(searchResults, resourceAssembler));
  }

  @PatchMapping(Links.EXPORT_JOBS_URL + "/{id}/context")
  ResponseEntity<PersistentEntityResource> patchExportJobContext(
      @PathVariable("id") ExportJob exportJob,
      @RequestBody Map<String, Object> context,
      PersistentEntityResourceAssembler assembler) {
    ExportJob updatedExportJob = exportJobService.updateContext(exportJob, context);
    PersistentEntityResource resource = assembler.toFullResource(updatedExportJob);
    return ResponseEntity.accepted().body(resource);
  }
}
