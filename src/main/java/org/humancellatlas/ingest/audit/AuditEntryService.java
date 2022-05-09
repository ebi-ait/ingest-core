package org.humancellatlas.ingest.audit;

import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.AbstractEntity;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AuditEntryService {

    private final AuditEntryRepository auditEntryRepository;

    public void addAuditEntry(AuditEntry auditEntry) {
        auditEntryRepository.save(auditEntry);
    }

    public List<AuditEntry> getAuditEntriesForAbstractEntity(AbstractEntity entity) {
        return auditEntryRepository.findByEntityEqualsOrderByDateDesc(entity);
    }
}
