package org.humancellatlas.ingest.archiving.submission.web;

import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.archiving.entity.ArchiveJob;
import org.humancellatlas.ingest.archiving.entity.ArchiveJob.ArchiveJobStatus;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;

@RepositoryRestController
@ExposesResourceFor(ArchiveJob.class)
@BasePathAwareController
@RequiredArgsConstructor
public class ArchiveJobController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveJobController.class);

    private final ArchiveJobRepository archiveJobRepository;

    @PostMapping("/archiveJobs")
    ResponseEntity<?> createArchiveJob(@RequestBody ArchiveJob archiveJob,
                                       PersistentEntityResourceAssembler resourceAssembler) {
        initResource(archiveJob);

        final ArchiveJob persistedArchiveJob = archiveJobRepository.save(archiveJob);
        final PersistentEntityResource entityResource = resourceAssembler.toFullResource(persistedArchiveJob);
        return ResponseEntity.created(URI.create(entityResource.getId().getHref()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(entityResource);
    }

    private void initResource(ArchiveJob archiveJob) {
        archiveJob.setCreatedDate(Instant.now());
        archiveJob.setOverallStatus(ArchiveJobStatus.PENDING);
    }

    @GetMapping("/archiveJobs/{id}")
    ResponseEntity<?> getArchiveJob(@PathVariable String id,
                                    PersistentEntityResourceAssembler resourceAssembler) {
        Optional<ArchiveJob> archiveJob = archiveJobRepository.findById(id);

        if (archiveJob.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().body(resourceAssembler.toFullResource(archiveJob.get()));
    }

}
