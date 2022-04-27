package org.humancellatlas.ingest.audit;

import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.AbstractEntity;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void addAuditLog(String event, AbstractEntity entity) {
        auditLogRepository.save(new AuditLog(event, entity));
    }

}
