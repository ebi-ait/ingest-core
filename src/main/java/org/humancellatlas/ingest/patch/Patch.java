package org.humancellatlas.ingest.patch;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.humancellatlas.ingest.core.AbstractEntity;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@AllArgsConstructor
@Document
@EqualsAndHashCode(callSuper=true)
public class Patch<T extends MetadataDocument> extends AbstractEntity {
    private Map<String, Object> jsonPatch;
    private @DBRef SubmissionEnvelope submissionEnvelope;
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, property="@class")
    private @DBRef T originalDocument;
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, property="@class")
    private @DBRef T updateDocument;

}
