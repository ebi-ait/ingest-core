package uk.ac.ebi.subs.ingest.audit;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

import uk.ac.ebi.subs.ingest.core.AbstractEntity;

@Repository
@RestResource(exported = false)
public interface AuditEntryRepository extends MongoRepository<AuditEntry, String> {

  List<AuditEntry> findByEntityEqualsOrderByDateDesc(AbstractEntity entity);
}
