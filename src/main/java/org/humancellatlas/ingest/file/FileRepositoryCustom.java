package org.humancellatlas.ingest.file;

import org.humancellatlas.ingest.query.MetadataCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FileRepositoryCustom {
    Page<File> findByCriteria(List<MetadataCriteria> criteriaList, Boolean andCriteria, Pageable pageable);
}