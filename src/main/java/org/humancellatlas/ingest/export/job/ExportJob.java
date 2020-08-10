package org.humancellatlas.ingest.export.job;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.humancellatlas.ingest.export.ExportError;
import org.humancellatlas.ingest.export.ExportState;
import org.humancellatlas.ingest.export.destination.ExportDestination;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.Identifiable;

import java.time.Instant;
import java.util.List;

@Data
@Document
public class ExportJob implements Identifiable<String> {
    @Id
    @JsonIgnore
    private final String id;

    @CreatedDate
    private final Instant createdDate;

    @Indexed
    @DBRef(lazy = true)
    private final SubmissionEnvelope submission;

    @Indexed
    private final ExportDestination destination;

    @Indexed
    private ExportState status;

    @LastModifiedDate
    private Instant updatedDate;

    private Object context;

    private List<ExportError> errors;

}
