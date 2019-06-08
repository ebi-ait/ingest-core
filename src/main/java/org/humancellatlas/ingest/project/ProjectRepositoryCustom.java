package org.humancellatlas.ingest.project;

import org.humancellatlas.ingest.query.Criteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProjectRepositoryCustom {
    Page<Project> queryProject(List<Criteria> criteria, Pageable pageable);
}
