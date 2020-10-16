package org.humancellatlas.ingest.biomaterial;

import org.humancellatlas.ingest.query.MetadataCriteria;
import org.humancellatlas.ingest.query.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

public class BiomaterialRepositoryImpl implements BiomaterialRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<Biomaterial> findByCriteria(List<MetadataCriteria> criteriaList, Boolean andCriteria, Pageable pageable) {
        QueryBuilder queryBuilder = new QueryBuilder();
        Query query = queryBuilder.build(criteriaList, andCriteria);

        long count = mongoTemplate.count(query, Biomaterial.class);
        query.with(pageable);
        List<Biomaterial> result = mongoTemplate.find(query, Biomaterial.class);

        return new PageImpl<>(result, pageable, count);
    }


}