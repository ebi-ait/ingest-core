package org.humancellatlas.ingest.protocol;

import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.query.MetadataCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProtocolRepositoryCustom {
    Page<Protocol> findByCriteria(List<MetadataCriteria> criteriaList, Boolean andCriteria, Pageable pageable);
}
