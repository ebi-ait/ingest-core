package org.humancellatlas.ingest.project;

import org.humancellatlas.ingest.query.MetadataCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProjectRepositoryCustom {
    Page<Project> findByContent(List<MetadataCriteria> criteria, Optional<Boolean> isUpdate, Pageable pageable);
}
