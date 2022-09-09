package org.humancellatlas.ingest.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NonNull;
import org.humancellatlas.ingest.core.AbstractEntity;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;


import java.time.Instant;

@Getter
public class AuditEntry {
    protected @Id @JsonIgnore String id;
    @NonNull private final AuditType auditType;
    private final Object before;
    private final Object after;
    private @CreatedDate Instant date;
    // todo: @CreatedBy isn't working, need to figure out why
    private @CreatedBy String user;
    @DBRef(lazy = true) @JsonIgnore final @NonNull private AbstractEntity entity;

    public AuditEntry(AuditType auditType, Object before, Object after, AbstractEntity entity) {
        this.auditType = auditType;
        this.before = before;
        this.after = after;
        this.entity = entity;
    }
}
