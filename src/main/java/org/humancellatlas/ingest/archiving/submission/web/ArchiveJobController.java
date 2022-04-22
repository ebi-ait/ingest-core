package org.humancellatlas.ingest.archiving.submission.web;

import org.humancellatlas.ingest.archiving.entity.ArchiveJob;
import org.humancellatlas.ingest.archiving.entity.ArchiveJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Instant;
import java.util.UUID;

@RepositoryRestController
@ExposesResourceFor(ArchiveJob.class)
public class ArchiveJobController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveJobController.class);

    private final ArchiveJobRepository archiveJobRepository;

    public ArchiveJobController(ArchiveJobRepository archiveJobRepository) {
        this.archiveJobRepository = archiveJobRepository;
    }

    @PostMapping("/archiveJobs")
    ResponseEntity<?> createArchiveJob(@RequestBody ArchiveJob archiveJob) {
        initResource(archiveJob);

        return ResponseEntity.ok(archiveJobRepository.save(archiveJob));
    }

    private void initResource(ArchiveJob archiveJob) {
        archiveJob.setUuid(UUID.randomUUID());
        archiveJob.setCreatedDate(Instant.now());
        archiveJob.setOverallStatus(ArchiveJob.ArchiveJobStatus.PENDING);
    }


}
