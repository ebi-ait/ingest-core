package org.humancellatlas.ingest.export.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
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
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@Document
public class ExportEntity implements Identifiable<String> {
    @Id
    @JsonIgnore
    private String id;

    @NonNull
    @Indexed
    @DBRef(lazy = true)
    private ExportJob exportJob;

    @NonNull
    @Indexed
    private ExportState status;

    @CreatedDate
    private Instant createdDate;

    @NonNull
    private Object context;

    @NonNull
    private List<ExportError> errors;

}
