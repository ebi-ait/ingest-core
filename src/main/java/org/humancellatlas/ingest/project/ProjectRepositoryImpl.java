package org.humancellatlas.ingest.project;

import org.humancellatlas.ingest.query.MetadataCriteria;
import org.humancellatlas.ingest.query.Operator;
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

    @Override
    public Page<Project> findByContent(List<MetadataCriteria> metadataQuery, Pageable pageable) {
        Query query = new Query();
        
        List<Criteria> criterias = new ArrayList<>();

        for(MetadataCriteria metadataCriteria : metadataQuery){
            String contentField = "content." + metadataCriteria.getContentField();
            Criteria criteria = Criteria.where(contentField);
            switch (metadataCriteria.getOperator()) {
                case IS:
                    try {
                        criteria = criteria.is(metadataCriteria.getValue());
                    }
                    catch (InvalidMongoDbApiUsageException e) {
                      throw new IllegalArgumentException(e.getMessage(), e);
                    }
                    break;
                case NE:
                    criteria = criteria.ne(metadataCriteria.getValue());
                    break;
                case GT:
                    criteria = criteria.gt(metadataCriteria.getValue());
                    break;
                case GTE:
                    criteria = criteria.gte(metadataCriteria.getValue());
                    break;
                case LT:
                    criteria = criteria.lt(metadataCriteria.getValue());
                    break;
                case LTE:
                    criteria = criteria.lte(metadataCriteria.getValue());
                    break;
                case IN:
                    criteria = criteria.in((Collection<?>) metadataCriteria.getValue());
                    break;
                case NIN:
                    criteria = criteria.nin((Collection<?>) metadataCriteria.getValue());
                    break;
                case REGEX:
                    criteria = criteria.regex((String) metadataCriteria.getValue());
                    break;
                default:
                    throw new IllegalArgumentException(String.format("MetadataCriteria %s is not supported.", metadataCriteria.getOperator()));
            }
            criterias.add(criteria);
        }

        query.addCriteria(
            new Criteria().orOperator(criterias.toArray(
                new Criteria[criterias.size()]
            ))
        );

        query.with(pageable);

        List<Project> result = mongoTemplate.find(query, Project.class);
        long count = mongoTemplate.count(query, Project.class);
        Page<Project> projectsPage = new PageImpl<>(result, pageable, count);
        return projectsPage;
    };
}
