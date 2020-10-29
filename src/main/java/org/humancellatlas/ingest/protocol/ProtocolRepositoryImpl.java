package org.humancellatlas.ingest.protocol;

import org.humancellatlas.ingest.query.MetadataCriteria;
import org.humancellatlas.ingest.query.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

public class ProtocolRepositoryImpl implements ProtocolRepositoryCustom {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private QueryBuilder queryBuilder;

    @Override
    public Page<Protocol> findByCriteria(List<MetadataCriteria> criteriaList, Boolean andCriteria, Pageable pageable) {
        Query query = queryBuilder.build(criteriaList, andCriteria);
        long count = mongoTemplate.count(query, Protocol.class);
        List<Protocol> result = mongoTemplate.find(query.with(pageable), Protocol.class);

        return new PageImpl<>(result, pageable, count);
    };
}
