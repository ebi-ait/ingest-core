package org.humancellatlas.ingest.archiving;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.Identifiable;

import java.time.Instant;

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
    private String dspUrl;

    @Setter
    private String accession;

    @Setter
    private Object conversion;

    @Setter
    private String[] metadataUuids;

    private @Setter
    @DBRef(lazy = true)
    ArchiveSubmission archiveSubmission;
}
