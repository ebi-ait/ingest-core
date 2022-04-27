package org.humancellatlas.ingest.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.AbstractEntity;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.Instant;

@Getter
@RequiredArgsConstructor
public class AuditLog {
    protected @Id @JsonIgnore String id;
    @NonNull private String event;
    private @CreatedDate Instant date;
    // todo: @CreatedBy isn't working, need to figure out why
    private @CreatedBy String user;
    @DBRef(lazy = true) @JsonIgnore @NonNull private AbstractEntity entity;
}
