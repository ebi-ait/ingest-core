package org.humancellatlas.ingest.export.job.web;

import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.export.job.ExportJob;
import org.humancellatlas.ingest.export.job.ExportJobService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;

@RepositoryRestController
@RequiredArgsConstructor
@ExposesResourceFor(ExportJob.class)
public class ExportJobController {
    private final ExportJobService exportJobService;


    @PostMapping(path = "/submissionEnvelopes/{sub_id}" + Links.EXPORT_JOB_URL)
    ResponseEntity<PersistentEntityResource> createExportJob(
        SubmissionEnvelope submissionEnvelope,
        @RequestBody ExportJobRequest exportJobRequest,
        final PersistentEntityResourceAssembler resourceAssembler) {
        ExportJob newExportJob = exportJobService.createExportJob(submissionEnvelope, exportJobRequest);
        PersistentEntityResource newExportJobResource = resourceAssembler.toFullResource(newExportJob);
        return ResponseEntity.created(URI.create(newExportJobResource.getId().getHref())).body(newExportJobResource);
    }
}
