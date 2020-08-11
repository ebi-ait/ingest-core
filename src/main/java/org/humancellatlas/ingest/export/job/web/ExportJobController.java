package org.humancellatlas.ingest.export.job.web;

import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.export.ExportState;
import org.humancellatlas.ingest.export.destination.ExportDestinationName;
import org.humancellatlas.ingest.export.job.ExportJob;
import org.humancellatlas.ingest.export.job.ExportJobRepository;
import org.humancellatlas.ingest.export.job.ExportJobService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RepositoryRestController
@RequiredArgsConstructor
@ExposesResourceFor(ExportJob.class)
public class ExportJobController {
    private final ExportJobService exportJobService;
    private final ExportJobRepository exportJobRepository;
    private final PagedResourcesAssembler pagedResourcesAssembler;

    @GetMapping(path = "/submissionEnvelopes/{id}" + Links.EXPORT_JOBS_URL)
    ResponseEntity<?> getExportJobsForSubmission(@PathVariable("id") SubmissionEnvelope submission,
                                                 Pageable pageable,
                                                 PersistentEntityResourceAssembler resourceAssembler) {
        return ResponseEntity.ok(pagedResourcesAssembler.toResource(
            exportJobRepository.findBySubmission(submission, pageable),
            resourceAssembler
        ));
    }

    @PostMapping(path = "/submissionEnvelopes/{id}" + Links.EXPORT_JOBS_URL)
    ResponseEntity<PersistentEntityResource> createExportJob(@PathVariable("id") SubmissionEnvelope submission,
                                                            @RequestBody ExportJobRequest exportJobRequest,
                                                            PersistentEntityResourceAssembler resourceAssembler) {
        ExportJob newExportJob = exportJobService.createExportJob(submission, exportJobRequest);
        PersistentEntityResource newExportJobResource = resourceAssembler.toFullResource(newExportJob);
        return ResponseEntity.created(URI.create(newExportJobResource.getId().getHref())).body(newExportJobResource);
    }

    @GetMapping(path = Links.EXPORT_JOBS_URL + "/search" + Links.EXPORT_JOB_FIND_URL)
    ResponseEntity<?> findExportJobs(@RequestParam("submissionUuid") UUID submissionUuid,
                                    @RequestParam("status") ExportState exportState,
                                    @RequestParam("destination") ExportDestinationName exportDestinationName,
                                    @RequestParam("version") String destinationVersion,
                                    Pageable pageable,
                                    PersistentEntityResourceAssembler resourceAssembler) {
        String version = destinationVersion.isEmpty() ? null : destinationVersion;
        Page<ExportJob> searchResults = this.exportJobService.find(submissionUuid, exportState, exportDestinationName, version, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toResource(searchResults, resourceAssembler));
    }
}
