package org.humancellatlas.ingest.export.job;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Builder;
import org.humancellatlas.ingest.export.ExportError;
import org.humancellatlas.ingest.export.ExportState;
import org.humancellatlas.ingest.export.destination.ExportDestination;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.Identifiable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@Document
@CompoundIndexes({
    @CompoundIndex(name = "exportDestinationName", def = "{ 'destination.name': 1 }"),
    @CompoundIndex(name = "exportDestinationVersion", def = "{ 'destination.version': 1 }")
})
public class ExportJob implements Identifiable<String> {
    @Id
    @JsonIgnore
    private String id;

    @CreatedDate
    private Instant createdDate;

    @Indexed
    @DBRef(lazy = true)
    private final SubmissionEnvelope submission;

    private final ExportDestination destination;

    @Indexed
    private ExportState status;

    @LastModifiedDate
    private Instant updatedDate;

    private Object context;

    private List<ExportError> errors;

    public static ExportJobBuilder buildNew() {
        return ExportJob.builder()
            .status(ExportState.Exporting)
            .errors(new ArrayList<ExportError>());
    }
}
