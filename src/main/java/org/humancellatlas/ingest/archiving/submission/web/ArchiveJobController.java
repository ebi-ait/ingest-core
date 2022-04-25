package org.humancellatlas.ingest.archiving.submission.web;

import org.humancellatlas.ingest.archiving.entity.ArchiveJob;
import org.humancellatlas.ingest.archiving.entity.ArchiveJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;
import java.time.Instant;

@RepositoryRestController
@ExposesResourceFor(ArchiveJob.class)
@BasePathAwareController
public class ArchiveJobController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveJobController.class);

    private final ArchiveJobRepository archiveJobRepository;

    public ArchiveJobController(ArchiveJobRepository archiveJobRepository) {
        this.archiveJobRepository = archiveJobRepository;
    }

    @PostMapping("/archiveJobs")
    ResponseEntity<?> createArchiveJob(@RequestBody ArchiveJob archiveJob,
                                       PersistentEntityResourceAssembler resourceAssembler) {
        initResource(archiveJob);

        final ArchiveJob persistedArchiveJob = archiveJobRepository.save(archiveJob);
        final PersistentEntityResource entityResource = resourceAssembler.toFullResource(persistedArchiveJob);
        return ResponseEntity.created(URI.create(entityResource.getId().getHref()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(persistedArchiveJob);
    }

    private void initResource(ArchiveJob archiveJob) {
        archiveJob.setCreatedDate(Instant.now());
        archiveJob.setOverallStatus(ArchiveJob.ArchiveJobStatus.PENDING);
    }


}
