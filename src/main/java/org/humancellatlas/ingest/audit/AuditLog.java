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
public class AuditLog {
    protected @Id @JsonIgnore String id;
    @NonNull private AuditType auditType;
    private Object before;
    private Object after;
    private @CreatedDate Instant date;
    // todo: @CreatedBy isn't working, need to figure out why
    private @CreatedBy String user;
    @DBRef(lazy = true) @JsonIgnore @NonNull private AbstractEntity entity;

    public AuditLog(AuditType auditType, Object before, Object after, AbstractEntity entity) {
        this.auditType = auditType;
        this.before = before;
        this.after = after;
        this.entity = entity;
    }
}
