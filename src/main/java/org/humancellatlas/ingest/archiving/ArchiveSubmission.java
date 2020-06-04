package org.humancellatlas.ingest.archiving;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.Identifiable;

import java.time.Instant;

@Getter
@Document
@RequiredArgsConstructor
public class ArchiveSubmission implements Identifiable<String> {

    @Id
    @JsonIgnore
    private String id;

    @CreatedDate
    private Instant created;

    @Setter
    private String dspUuid;

    @Setter
    private String dspUrl;

    @Setter
    private String submissionUuid;
}
