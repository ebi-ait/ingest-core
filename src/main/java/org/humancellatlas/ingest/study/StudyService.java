package org.humancellatlas.ingest.study;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.IOException;
import java.util.*;

import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
@Getter
public class StudyService {

    //Helper class for capturing copies of a Study and all Submission Envelopes related to them.
    private static class StudyBag {

        private final Set<Study> studies;
        private final Set<SubmissionEnvelope> submissionEnvelopes;

        public StudyBag(Set<Study> studies, Set<SubmissionEnvelope> submissionEnvelopes) {
            this.studies = studies;
            this.submissionEnvelopes = submissionEnvelopes;
        }

    }

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

    private final ObjectMapper objectMapper = new ObjectMapper(); // For JSON string to Map conversion if necessary

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
        Map<String, Object> contentMap = convertAndMergeStudyContent(study.getContent());
        study.setContent(contentMap);

        if (!study.getIsUpdate()) {
            return metadataCrudService.addToSubmissionEnvelopeAndSave(study, submissionEnvelope);
        } else {
            return metadataUpdateService.acceptUpdate(study, submissionEnvelope);
        }
    }

    private Map<String, Object> convertAndMergeStudyContent(Object contentObject) {
        Map<String, Object> contentMap = convertContentToObjectMap(contentObject);
        contentMap.putAll(createBaseContentForStudy());
        return contentMap;
    }

    private Map<String, Object> convertContentToObjectMap(Object contentObject) {
        try {
            if (contentObject instanceof Map) {
                return (Map<String, Object>) contentObject;
            } else if (contentObject instanceof String) {
                return objectMapper.readValue((String) contentObject, new TypeReference<Map<String, Object>>() {
                });
            }
            return new HashMap<>();
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse content JSON string", e);
        }
    }

    private Map<String, String> createBaseContentForStudy() {
        Map<String, String> content = new HashMap<>();
        content.put("describedBy", "https://schema.morphic.bio/type/project/0.0.1/study");
        content.put("schema_type", "study");
        return content;
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

    public Study linkDatasetToStudy(Study study, Dataset dataset) {
        study.addDataset(dataset);

        return studyRepository.save(study);
    }

    public Set<SubmissionEnvelope> getSubmissionEnvelopes(Study study) {
        return gather(study).submissionEnvelopes;
    }

    private StudyService.StudyBag gather(Study study) {
        Set<SubmissionEnvelope> envelopes = new HashSet<>();
        Set<Study> studies = this.studyRepository.findByUuid(study.getUuid()).collect(toSet());
        studies.forEach(copy -> {
            envelopes.addAll(copy.getSubmissionEnvelopes());
            envelopes.add(copy.getSubmissionEnvelope());
        });

        //ToDo: Find a better way of ensuring that DBRefs to deleted objects aren't returned.
        envelopes.removeIf(env -> env == null || env.getSubmissionState() == null);
        return new StudyService.StudyBag(studies, envelopes);
    }
}
