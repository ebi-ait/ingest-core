package org.humancellatlas.ingest.study;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
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
            log.warn("Study not found with ID: {}", studyId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Study not found with ID: " + studyId);
        }

        Study existingStudy = existingStudyOptional.get();
        /*
         * TODO: have introduced new update method for Study to avoid dependencies with Submission Envelope
         * - Adding Studies to Submission Envelopes is not required at this moment.
         * - However, need to make sure whether this approach has to change in the long term.
         */
        Study updatedStudy = metadataUpdateService.updateStudy(existingStudy, patch);
        studyEventHandler.updatedStudy(updatedStudy);
        return updatedStudy;
    }

    public void delete(String studyId) {
        Optional<Study> deleteStudyOptional = studyRepository.findById(studyId);

        if (deleteStudyOptional.isEmpty()) {
            log.warn("Study not found with ID: {}", studyId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Study not found with ID: " + studyId);
        }

        Study deleteStudy = deleteStudyOptional.get();
        metadataCrudService.deleteDocument(deleteStudy);
        studyEventHandler.deletedStudy(studyId);
    }
}
