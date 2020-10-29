package org.humancellatlas.ingest.process;

import org.humancellatlas.ingest.query.MetadataCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProcessRepositoryCustom {
    Page<Process> findByCriteria(List<MetadataCriteria> criteriaList, Boolean andCriteria, Pageable pageable);
}
