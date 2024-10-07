package org.humancellatlas.ingest.study;

import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.dataset.Dataset;
import org.humancellatlas.ingest.dataset.DatasetRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Getter
public class StudyService {
  private static class StudyBag {
    private final Set<Study> studies;
    private final Set<SubmissionEnvelope> submissionEnvelopes;

    public StudyBag(final Set<Study> studies, final Set<SubmissionEnvelope> submissionEnvelopes) {
      this.studies = Collections.unmodifiableSet(new HashSet<>(studies));
      this.submissionEnvelopes = Collections.unmodifiableSet(new HashSet<>(submissionEnvelopes));
    }
  }

  private final MongoTemplate mongoTemplate;
  private final @NonNull StudyRepository studyRepository;
  private final @NotNull DatasetRepository datasetRepository;
  private final @NonNull MetadataCrudService metadataCrudService;
  private final @NonNull MetadataUpdateService metadataUpdateService;
  private final @NonNull StudyEventHandler studyEventHandler;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final Logger log = LoggerFactory.getLogger(getClass());

  protected final Logger getLog() {
    return log;
  }

  public final Study register(final Study study) {
    final Study persistentStudy = studyRepository.save(study);
    studyEventHandler.registeredStudy(persistentStudy);

    return persistentStudy;
  }

  public final Study update(final Study study, final ObjectNode patch) {
    final String studyId = study.getId();
    final Optional<Study> existingStudyOptional = studyRepository.findById(studyId);

    if (existingStudyOptional.isEmpty()) {
      log.warn("Attempted to update study with ID: {} but not found.", studyId);
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Study not found with ID: " + studyId);
    }

    final Study existingStudy = existingStudyOptional.get();
    final Study updatedStudy = metadataUpdateService.update(existingStudy, patch);

    studyEventHandler.updatedStudy(updatedStudy);

    return updatedStudy;
  }

  public Study findById(String studyId) {
    Optional<Study> studyOptional = studyRepository.findById(studyId);
    return studyOptional.orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Study not found with ID: " + studyId));
  }

  public final Study replace(final String studyId, final Study updatedStudy) {
    final Optional<Study> existingStudyOptional = studyRepository.findById(studyId);

    if (existingStudyOptional.isEmpty()) {
      log.warn("Study not found with ID: {}", studyId);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    studyRepository.save(updatedStudy);
    studyEventHandler.updatedStudy(updatedStudy);

    return updatedStudy;
  }

  public final void delete(final String studyId) {
    final Optional<Study> deleteStudyOptional = studyRepository.findById(studyId);

    if (deleteStudyOptional.isEmpty()) {
      log.warn("Attempted to delete study with ID: {} but not found.", studyId);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    final Study deleteStudy = deleteStudyOptional.get();

    metadataCrudService.deleteDocument(deleteStudy);
    studyEventHandler.deletedStudy(studyId);
  }

  public final Study addStudyToSubmissionEnvelope(
      final SubmissionEnvelope submissionEnvelope, final Study study) {
    if (!study.getIsUpdate()) {
      return metadataCrudService.addToSubmissionEnvelopeAndSave(study, submissionEnvelope);
    } else {
      return metadataUpdateService.acceptUpdate(study, submissionEnvelope);
    }
  }

  public final Study linkStudySubmissionEnvelope(
      final SubmissionEnvelope submissionEnvelope, final Study study) {
    final String studyId = study.getId();
    study.addToSubmissionEnvelopes(submissionEnvelope);
    studyRepository.save(study);

    studyRepository
        .findByUuidUuidAndIsUpdateFalse(study.getUuid().getUuid())
        .ifPresent(
            studyByUuid -> {
              if (!studyByUuid.getId().equals(studyId)) {
                studyByUuid.addToSubmissionEnvelopes(submissionEnvelope);
                studyRepository.save(studyByUuid);
              }
            });

    return study;
  }

  public final Study linkDatasetToStudy(final Study study, final Dataset dataset) {
    final String studyId = study.getId();
    final String datasetId = dataset.getId();

    studyRepository
        .findById(studyId)
        .orElseThrow(() -> new ResourceNotFoundException("Study: " + studyId));
    datasetRepository
        .findById(datasetId)
        .orElseThrow(() -> new ResourceNotFoundException("Dataset: " + datasetId));

    study.addDataset(dataset);

    return studyRepository.save(study);
  }

  public final Set<SubmissionEnvelope> getSubmissionEnvelopes(final Study study) {
    return gather(study).submissionEnvelopes;
  }

  private StudyService.StudyBag gather(final Study study) {
    final Set<SubmissionEnvelope> envelopes = new HashSet<>();
    final Set<Study> studies = this.studyRepository.findByUuid(study.getUuid()).collect(toSet());
    studies.forEach(
        copy -> {
          envelopes.addAll(copy.getSubmissionEnvelopes());
          envelopes.add(copy.getSubmissionEnvelope());
        });

    // ToDo: Find a better way of ensuring that DBRefs to deleted objects aren't returned.
    envelopes.removeIf(env -> env == null || env.getSubmissionState() == null);

    return new StudyService.StudyBag(studies, envelopes);
  }
}
