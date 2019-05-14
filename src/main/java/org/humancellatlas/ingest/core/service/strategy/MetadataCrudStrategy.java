package org.humancellatlas.ingest.core.service.strategy;

import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;

import java.util.Collection;
import java.util.stream.Stream;

public interface MetadataCrudStrategy <T extends MetadataDocument> {
    T saveMetadataDocument(T document);
    T findMetadataDocument(String id);
    T findOriginalByUuid(String uuid);
    Collection<T> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);
}