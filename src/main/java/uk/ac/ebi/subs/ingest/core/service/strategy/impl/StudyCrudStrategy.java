package uk.ac.ebi.subs.ingest.core.service.strategy.impl;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import uk.ac.ebi.subs.ingest.core.service.strategy.MetadataCrudStrategy;
import uk.ac.ebi.subs.ingest.study.Study;
import uk.ac.ebi.subs.ingest.study.StudyRepository;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

@Component
@AllArgsConstructor
public class StudyCrudStrategy implements MetadataCrudStrategy<Study> {
  private final @NonNull StudyRepository studyRepository;

  @Override
  public Study saveMetadataDocument(Study document) {
    return studyRepository.save(document);
  }

  @Override
  public Study findMetadataDocument(String id) {
    return studyRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
  }

  @Override
  public Study findOriginalByUuid(String uuid) {
    return studyRepository
        .findByUuidUuidAndIsUpdateFalse(UUID.fromString(uuid))
        .orElseThrow(ResourceNotFoundException::new);
  }

  @Override
  public Stream<Study> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
    return studyRepository.findBySubmissionEnvelope(submissionEnvelope);
  }

  @Override
  public Collection<Study> findAllBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
    return studyRepository.findAllBySubmissionEnvelope(submissionEnvelope);
  }

  @Override
  public void removeLinksToDocument(Study document) {}

  @Override
  public void deleteDocument(Study document) {
    // TODO: Check what links need to be removed when deleting a Study document
    // removeLinksToDocument(document);
    studyRepository.delete(document);
  }
}