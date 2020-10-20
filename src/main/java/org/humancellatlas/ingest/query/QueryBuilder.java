package org.humancellatlas.ingest.query;

import org.springframework.data.mongodb.InvalidMongoDbApiUsageException;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class QueryBuilder {
    public Query build(List<MetadataCriteria> criteriaList, Boolean andCriteria){
        Query query = new Query();

        List<Criteria> criterias = new ArrayList<>();

        for (MetadataCriteria metadataCriteria : criteriaList) {
            String field = metadataCriteria.getField();
            Criteria criteria = Criteria.where(field);
            switch (metadataCriteria.getOperator()) {
                case IS:
                    try {
                        criteria = criteria.is(metadataCriteria.getValue());
                    } catch (InvalidMongoDbApiUsageException e) {
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
                    criteria = criteria.regex((String) metadataCriteria.getValue(), "i");
                    break;
                default:
                    throw new IllegalArgumentException(String.format("MetadataCriteria %s is not supported.", metadataCriteria.getOperator()));
            }
            criterias.add(criteria);
        }

        if (andCriteria) {
            query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[criterias.size()])));
        } else {
            query.addCriteria(new Criteria().orOperator(criterias.toArray(new Criteria[criterias.size()])));
        }

        return query;
    }
}