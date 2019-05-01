package org.humancellatlas.ingest.patch;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.humancellatlas.ingest.core.AbstractEntity;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@AllArgsConstructor
@Document
public class Patch<T extends MetadataDocument> extends AbstractEntity {
    private final JsonNode patch;
    private @DBRef final SubmissionEnvelope submissionEnvelope;
    private @DBRef final T originalDocument;
    private @DBRef final T updateDocument;
}
