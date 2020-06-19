package org.humancellatlas.ingest.archiving.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.humancellatlas.ingest.archiving.Error;
import org.humancellatlas.ingest.archiving.submission.ArchiveSubmission;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.Identifiable;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Document
public class ArchiveEntity implements Identifiable<String> {
    @Id
    @JsonIgnore
    private String id;

    @CreatedDate
    private Instant created;

    @Setter
    private ArchiveEntityType type;

    @Setter
    private String dspUuid;

    @Setter
    private URI dspUrl;

    @Setter
    private String accession;

    @Setter
    private Object conversion;

    @Setter
    private List<String> metadataUuids;
    
    private @Setter
    @DBRef(lazy = true)
    ArchiveSubmission archiveSubmission;

    private @Setter
    List<Error> errors = new ArrayList<>();
}
