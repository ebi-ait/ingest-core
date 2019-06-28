package org.humancellatlas.ingest.project;

import org.humancellatlas.ingest.query.MetadataCriteria;
import org.humancellatlas.ingest.query.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
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
            if(metadataCriteria.getOperator().equals(Operator.IS)){
                criterias.add(Criteria.where(contentField).is(metadataCriteria.getValue()));
            } else if (metadataCriteria.getOperator().equals(Operator.REGEX)){
            	criterias.add(Criteria.where(contentField).regex((String) metadataCriteria.getValue()));
            } else if (metadataCriteria.getOperator().equals(Operator.NE)){
            	criterias.add(Criteria.where(contentField).ne(metadataCriteria.getValue()));
            } else {
                throw new RuntimeException("MetadataCriteria not allowed!");
            }
        }
        
        query.addCriteria(
            new Criteria().orOperator(
            		 (Criteria[]) criterias.toArray()
            )
        );
        
        query.with(pageable);

        List<Project> result = mongoTemplate.find(query, Project.class);
        long count = mongoTemplate.count(query, Project.class);
        Page<Project> projectsPage = new PageImpl<>(result, pageable, count);
        return projectsPage;
    };
}
