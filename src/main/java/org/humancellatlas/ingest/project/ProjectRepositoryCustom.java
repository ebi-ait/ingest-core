package org.humancellatlas.ingest.project;

import org.humancellatlas.ingest.query.MetadataCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProjectRepositoryCustom {
    Page<Project> findByCriteria(List<MetadataCriteria> criteriaList, Pageable pageable);
}
