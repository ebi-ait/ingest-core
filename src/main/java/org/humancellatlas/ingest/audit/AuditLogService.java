package org.humancellatlas.ingest.audit;

import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.AbstractEntity;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void addAuditLog(AuditLog auditLog) {
        auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getAuditLogOf(AbstractEntity entity) {
        return auditLogRepository.findByEntityEqualsOrderByDateDesc(entity);
    }
}
