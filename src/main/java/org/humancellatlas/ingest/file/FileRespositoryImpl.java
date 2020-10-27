package org.humancellatlas.ingest.file;

import org.humancellatlas.ingest.query.MetadataCriteria;
import org.humancellatlas.ingest.query.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

class FileRepositoryImpl implements FileRepositoryCustom {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private QueryBuilder queryBuilder;

    @Override
    public Page<File> findByCriteria(List<MetadataCriteria> criteriaList, Boolean andCriteria, Pageable pageable) {
        Query query = queryBuilder.build(criteriaList, andCriteria);

        long count = mongoTemplate.count(query, File.class);
        query.with(pageable);
        List<File> result = mongoTemplate.find(query, File.class);

        return new PageImpl<>(result, pageable, count);
    };
}