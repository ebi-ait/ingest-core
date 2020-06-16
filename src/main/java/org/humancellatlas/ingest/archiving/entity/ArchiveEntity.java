package org.humancellatlas.ingest.archiving.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.humancellatlas.ingest.archiving.Error;
import org.humancellatlas.ingest.archiving.submission.ArchiveSubmission;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.Identifiable;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Document
public class ArchiveEntity implements Identifiable<String> {
    @DBRef(lazy = true)
    ArchiveSubmission archiveSubmission;
    @Id
    @JsonIgnore
    private String id;
    @CreatedDate
    private Instant created;
    private ArchiveEntityType type;
    @Indexed(unique = true)
    private String alias;
    @Indexed(unique = true)
    private String dspUuid;
    private URI dspUrl;
    private String accession;
    private Object conversion;
    private List<String> metadataUuids;
    private List<Error> errors = new ArrayList<>();

    ArchiveEntity() {

    }
}
