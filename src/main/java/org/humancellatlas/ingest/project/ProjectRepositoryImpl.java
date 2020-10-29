package org.humancellatlas.ingest.project;

import org.humancellatlas.ingest.query.MetadataCriteria;
import org.humancellatlas.ingest.query.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.InvalidMongoDbApiUsageException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProjectRepositoryImpl implements ProjectRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private QueryBuilder queryBuilder;

    @Override
    public Page<Project> findByCriteria(List<MetadataCriteria> criteriaList, Boolean andCriteria, Pageable pageable) {
        Query query = queryBuilder.build(criteriaList, andCriteria);
        long count = mongoTemplate.count(query, Project.class);
        List<Project> result = mongoTemplate.find(query.with(pageable), Project.class);

        return new PageImpl<>(result, pageable, count);
    };
}
