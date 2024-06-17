package org.humancellatlas.ingest.core.service.strategy;

import java.util.Collection;
import java.util.stream.Stream;

import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;

public interface MetadataCrudStrategy<T extends MetadataDocument> {
  T saveMetadataDocument(T document);

  T findMetadataDocument(String id);

  T findOriginalByUuid(String uuid);

  Stream<T> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);

  Collection<T> findAllBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);

  void removeLinksToDocument(T document);

  void deleteDocument(T document);
}
