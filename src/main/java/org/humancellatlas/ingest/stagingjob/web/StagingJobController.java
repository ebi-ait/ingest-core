package org.humancellatlas.ingest.stagingjob.web;

import java.util.UUID;

import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.stagingjob.StagingJob;
import org.humancellatlas.ingest.stagingjob.StagingJobService;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RepositoryRestController
@ExposesResourceFor(StagingJob.class)
@RequiredArgsConstructor
@RequestMapping("/stagingJobs")
public class StagingJobController {

  private final @NonNull StagingJobService stagingJobService;

  @PostMapping
  public ResponseEntity<?> createStagingJob(
      @RequestBody StagingJob stagingJob, PersistentEntityResourceAssembler resourceAssembler) {
    StagingJob persistentJob = stagingJobService.register(stagingJob);
    return ResponseEntity.ok(resourceAssembler.toFullResource(persistentJob));
  }

  @PatchMapping(path = "/{stagingJob}" + Links.COMPLETE_STAGING_JOB_URL)
  ResponseEntity<?> completeStagingJob(
      @PathVariable("stagingJob") StagingJob stagingJob,
      @RequestBody StagingJobCompleteRequest stagingJobCompleteRequest,
      final PersistentEntityResourceAssembler resourceAssembler) {
    StagingJob completedStagingJob =
        stagingJobService.completeJob(
            stagingJob, stagingJobCompleteRequest.getStagingAreaFileUri());

    return ResponseEntity.ok(resourceAssembler.toFullResource(completedStagingJob));
  }

  @DeleteMapping
  ResponseEntity<?> deleteStagingJobs(@RequestParam("stagingAreaUuid") UUID stagingAreaUuid) {
    stagingJobService.deleteJobsForStagingArea(stagingAreaUuid);
    return new ResponseEntity<StagingJob>(HttpStatus.NO_CONTENT);
  }
}
