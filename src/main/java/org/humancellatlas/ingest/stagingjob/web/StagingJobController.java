package org.humancellatlas.ingest.stagingjob.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.stagingjob.StagingJob;
import org.humancellatlas.ingest.stagingjob.StagingJobService;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RepositoryRestController
@ExposesResourceFor(StagingJob.class)
@RequiredArgsConstructor
public class StagingJobController {
    private final @NonNull StagingJobService stagingJobService;

    @PostMapping(path = "/stagingJobs")
    ResponseEntity<?> createStagingJob(@RequestBody StagingJobCreateRequest stagingJobCreateRequest,
                                       final PersistentEntityResourceAssembler resourceAssembler) {
        StagingJob stagingJob = stagingJobService.registerNewJob(
                stagingJobCreateRequest.getStagingAreaUuid(),
                stagingJobCreateRequest.getStagingAreaFileName()
        );

        return ResponseEntity.ok(resourceAssembler.toFullResource(stagingJob));
    }

    @PatchMapping(path = "/stagingJobs/{stagingJob}" + Links.COMPLETE_STAGING_JOB_URL)
    ResponseEntity<?> completeStagingJob(@PathVariable("stagingJob") StagingJob stagingJob,
                                         @RequestBody StagingJobCompleteRequest stagingJobCompleteRequest,
                                         final PersistentEntityResourceAssembler resourceAssembler) {
        StagingJob completedStagingJob = stagingJobService.completeJob(
                stagingJob,
                stagingJobCompleteRequest.getStagingAreaFileUri()
        );

        return ResponseEntity.ok(resourceAssembler.toFullResource(completedStagingJob));
    }

    @DeleteMapping(path = "/stagingJobs")
    ResponseEntity<?> deleteStagingJobs(@RequestParam("stagingAreaUuid") UUID stagingAreaUuid) {
        stagingJobService.deleteJobsForStagingArea(stagingAreaUuid);
        return new ResponseEntity<StagingJob>(HttpStatus.NO_CONTENT);
    }
}
