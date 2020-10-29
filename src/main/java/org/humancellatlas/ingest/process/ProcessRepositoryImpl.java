package org.humancellatlas.ingest.process;

import org.humancellatlas.ingest.query.MetadataCriteria;
import org.humancellatlas.ingest.query.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

public class ProcessRepositoryImpl implements ProcessRepositoryCustom {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private QueryBuilder queryBuilder;

    @Override
    public Page<Process> findByCriteria(List<MetadataCriteria> criteriaList, Boolean andCriteria, Pageable pageable) {
        Query query = queryBuilder.build(criteriaList, andCriteria);
        List<Process> result = mongoTemplate.find(query.with(pageable), Process.class);
        long count = mongoTemplate.count(query, Process.class);

        return new PageImpl<>(result, pageable, count);
    }
}
