package org.humancellatlas.ingest.project;

import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.query.Criteria;
import org.humancellatlas.ingest.query.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

public class ProjectRepositoryImpl implements ProjectRepositoryCustom {

    private  final MongoTemplate mongoTemplate;

    @Autowired
    public ProjectRepositoryImpl(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<Project> queryProject(List<Criteria> metadataQuery, Pageable pageable) {
        Query query = new Query();

        for(Criteria criteria: metadataQuery){
            if(criteria.getOperator().equals(Operator.IS)){
                query.addCriteria(org.springframework.data.mongodb.core.query.Criteria.where(criteria.getField()).is(criteria.getValue()));
            } else if (criteria.getOperator().equals(Operator.REGEX)){
                query.addCriteria(org.springframework.data.mongodb.core.query.Criteria.where(criteria.getField()).regex((String) criteria.getValue()));
            } else if (criteria.getOperator().equals(Operator.NE)){
                query.addCriteria(org.springframework.data.mongodb.core.query.Criteria.where(criteria.getField()).ne(criteria.getValue()));
            } else {
                throw new RuntimeException("Criteria not allowed!");
            }
        }

        query.with(pageable);

        List<Project> result = mongoTemplate.find(query, Project.class);
        long count = mongoTemplate.count(query, BundleManifest.class);
        Page<Project> projectsPage = new PageImpl<>(result, pageable, count);
        return projectsPage;
    };
}
