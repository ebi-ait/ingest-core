package org.humancellatlas.ingest.query;

import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.core.EntityType;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;

import java.util.List;

/**
 * Created by prabhat on 02/11/2020.
 */
@Service
public class MetadataQueryService {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private QueryBuilder queryBuilder;

    public Page<?> findByCriteria(EntityType metadataType, List<MetadataCriteria> criteriaList, Boolean andCriteria, Pageable pageable) {
        Query query = queryBuilder.build(criteriaList, andCriteria);
        List<?> result = mongoTemplate.find(query.with(pageable), getEntityClass(metadataType));
        long count = mongoTemplate.count(query, getEntityClass(metadataType));

        return new PageImpl<>(result, pageable, count);
    };

    Class<?> getEntityClass(EntityType metadataType) {
        switch (metadataType) {
            case BIOMATERIAL:
                return Biomaterial.class;
            case PROTOCOL:
                return Protocol.class;
            case PROJECT:
                return Project.class;
            case PROCESS:
                return Process.class;
            case FILE:
                return File.class;
            default:
                throw new ResourceNotFoundException();
        }
    }
}
