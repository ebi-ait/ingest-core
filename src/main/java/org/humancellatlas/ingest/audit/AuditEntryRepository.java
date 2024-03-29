package org.humancellatlas.ingest.audit;

import org.humancellatlas.ingest.core.AbstractEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@RestResource(exported=false)
public interface AuditEntryRepository extends MongoRepository<AuditEntry, String> {

    List<AuditEntry> findByEntityEqualsOrderByDateDesc(AbstractEntity entity);
}

