package org.humancellatlas.ingest.audit;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.AbstractEntity;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.Instant;

@RequiredArgsConstructor
public class AuditLog {
    @NonNull private String event;
    private @CreatedBy String user;
    private @CreatedDate Instant date;
    @DBRef(lazy = true)
    @NonNull private AbstractEntity entity;
}
