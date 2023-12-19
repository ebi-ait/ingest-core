package org.humancellatlas.ingest.core.service.strategy.impl;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.humancellatlas.ingest.study.Study;
import org.humancellatlas.ingest.study.StudyRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Stream;

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
        return studyRepository.findById(id)
                .orElseThrow(() -> {
                    throw new ResourceNotFoundException();
                });
    }

    @Override
    public Study findOriginalByUuid(String uuid) {
        return null;
    }

    @Override
    public Stream<Study> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        return null;
    }

    @Override
    public Collection<Study> findAllBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        return null;
    }

    @Override
    public void removeLinksToDocument(Study document) {

    }

    @Override
    public void deleteDocument(Study document) {
        //TODO: Check what links need to be removed when deleting a Study document
        // removeLinksToDocument(document);
        studyRepository.delete(document);
    }
}