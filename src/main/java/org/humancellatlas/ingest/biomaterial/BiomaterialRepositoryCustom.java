package org.humancellatlas.ingest.biomaterial;

import org.humancellatlas.ingest.query.MetadataCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BiomaterialRepositoryCustom {
    Page<Biomaterial> findByCriteria(List<MetadataCriteria> criteriaList, Boolean andCriteria, Pageable pageable);
}