package uk.ac.ebi.subs.ingest.audit;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.core.AbstractEntity;

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
