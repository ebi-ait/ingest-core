package uk.ac.ebi.subs.ingest.archiving.submission.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.archiving.entity.ArchiveEntity;
import uk.ac.ebi.subs.ingest.archiving.entity.ArchiveEntityRepository;
import uk.ac.ebi.subs.ingest.archiving.submission.ArchiveSubmission;
import uk.ac.ebi.subs.ingest.archiving.submission.ArchiveSubmissionRepository;

@RepositoryRestController
@RequiredArgsConstructor
@ExposesResourceFor(ArchiveSubmission.class)
@Getter
public class ArchiveSubmissionController {
  private final @NonNull ArchiveSubmissionRepository archiveSubmissionRepository;
  private final @NonNull ArchiveEntityRepository archiveEntityRepository;

  private final @NonNull PagedResourcesAssembler pagedResourcesAssembler;

  @RequestMapping(path = "archiveSubmissions/{sub_id}/entities", method = RequestMethod.POST)
  ResponseEntity<Resource<?>> addEntity(
      @PathVariable("sub_id") ArchiveSubmission archiveSubmission,
      @RequestBody ArchiveEntity entity,
      PersistentEntityResourceAssembler assembler) {
    entity.setArchiveSubmission(archiveSubmission);
    entity = archiveEntityRepository.save(entity);
    PersistentEntityResource resource = assembler.toFullResource(entity);
    return ResponseEntity.accepted().body(resource);
  }

  @RequestMapping(path = "archiveSubmissions/{sub_id}/entities", method = RequestMethod.GET)
  ResponseEntity<?> addEntity(
      @PathVariable("sub_id") ArchiveSubmission archiveSubmission,
      Pageable pageable,
      final PersistentEntityResourceAssembler resourceAssembler) {
    Page<ArchiveEntity> archiveEntities =
        archiveEntityRepository.findByArchiveSubmission(archiveSubmission, pageable);
    return ResponseEntity.ok(
        pagedResourcesAssembler.toResource(archiveEntities, resourceAssembler));
  }

  @RequestMapping(path = "archiveSubmissions/{sub_id}", method = RequestMethod.DELETE)
  ResponseEntity deleteSubmission(@PathVariable("sub_id") ArchiveSubmission archiveSubmission) {
    archiveEntityRepository.deleteByArchiveSubmission(archiveSubmission);
    archiveSubmissionRepository.delete(archiveSubmission);
    return ResponseEntity.accepted().build();
  }
}
