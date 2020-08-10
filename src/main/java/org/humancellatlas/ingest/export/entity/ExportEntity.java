package org.humancellatlas.ingest.export.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.humancellatlas.ingest.export.ExportError;
import org.humancellatlas.ingest.export.ExportState;
import org.humancellatlas.ingest.export.job.ExportJob;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.Identifiable;

import java.time.Instant;
import java.util.List;

@Data
@Document
public class ExportEntity implements Identifiable<String> {
    @Id
    @JsonIgnore
    private final String id;

    @Indexed
    @DBRef(lazy = true)
    private final ExportJob exportJob;

    @Indexed
    private final ExportState status;

    @CreatedDate
    private final Instant createdDate;

    private final Object context;

    private final List<ExportError> errors;

}
