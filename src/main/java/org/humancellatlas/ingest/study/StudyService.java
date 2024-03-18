package org.humancellatlas.ingest.study;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.dataset.Dataset;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Getter
public class StudyService {
    @Autowired
    private final MongoTemplate mongoTemplate;
    private final @NonNull StudyRepository studyRepository;
    private final @NonNull MetadataCrudService metadataCrudService;
    private final @NonNull MetadataUpdateService metadataUpdateService;
    private final @NonNull StudyEventHandler studyEventHandler;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public Study register(final Study study) {
        Study persistentStudy = studyRepository.save(study);
        studyEventHandler.registeredStudy(persistentStudy);
        return persistentStudy;
    }

    public Study update(String studyId, ObjectNode patch) {
        Optional<Study> existingStudyOptional = studyRepository.findById(studyId);

        if (existingStudyOptional.isEmpty()) {
            log.warn("Attempted to update study with ID: {} but not found.", studyId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Study existingStudy = existingStudyOptional.get();
        Study updatedStudy = metadataUpdateService.update(existingStudy, patch);
        studyEventHandler.updatedStudy(updatedStudy);
        return updatedStudy;
    }

    public Study replace(String studyId, Study updatedStudy) {
        Optional<Study> existingStudyOptional = studyRepository.findById(studyId);

        if (existingStudyOptional.isEmpty()) {
            log.warn("Study not found with ID: {}", studyId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        // Replace the entire entity with the updatedStudy
        Study existingStudy = existingStudyOptional.get();
        existingStudy = updatedStudy; // This line replaces the entire entity

        studyRepository.save(existingStudy);
        studyEventHandler.updatedStudy(existingStudy);

        return existingStudy;
    }

    public void delete(String studyId) {
        Optional<Study> deleteStudyOptional = studyRepository.findById(studyId);

        if (deleteStudyOptional.isEmpty()) {
            log.warn("Attempted to delete study with ID: {} but not found.", studyId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Study deleteStudy = deleteStudyOptional.get();
        metadataCrudService.deleteDocument(deleteStudy);
        studyEventHandler.deletedStudy(studyId);
    }

    public Study addStudyToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Study study) {
        if (!study.getIsUpdate()) {
            return metadataCrudService.addToSubmissionEnvelopeAndSave(study, submissionEnvelope);
        } else {
            return metadataUpdateService.acceptUpdate(study, submissionEnvelope);
        }
    }

    public Study linkStudySubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Study study) {
        final String studyId = study.getId();
        study.addToSubmissionEnvelopes(submissionEnvelope);
        studyRepository.save(study);

        studyRepository.findByUuidUuidAndIsUpdateFalse(study.getUuid().getUuid()).ifPresent(studyByUuid -> {
            if (!studyByUuid.getId().equals(studyId)) {
                studyByUuid.addToSubmissionEnvelopes(submissionEnvelope);
                studyRepository.save(studyByUuid);
            }
        });
        return study;
    }

    public Study linkDatasetToStudy(Dataset dataset, Study study) {
        study.addDataset(dataset);

        return studyRepository.save(study);
    }
}
