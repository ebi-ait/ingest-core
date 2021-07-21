package org.humancellatlas.ingest.project;

import org.humancellatlas.ingest.project.web.SearchFilter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectQueryBuilder {

    public static Query buildProjectsQuery(SearchFilter searchFilter) {
        List<Criteria> criteria_list = new ArrayList<>();
        criteria_list.add(Criteria.where("isUpdate").is(false));

        Optional.ofNullable(searchFilter.getWranglingState())
                .ifPresent(wranglingState -> {
                    criteria_list.add(Criteria.where("wranglingState").is(wranglingState));
                });

        Optional.ofNullable(searchFilter.getWrangler())
                .ifPresent(wrangler -> {
                    criteria_list.add(Criteria.where("primaryWrangler").is(wrangler));
                });

        Criteria queryCriteria = new Criteria().andOperator(criteria_list.toArray(new Criteria[criteria_list.size()]));
        Query query = new Query().addCriteria(queryCriteria);
        Optional.ofNullable(searchFilter.getSearch())
                .ifPresent(search -> {
                    query.addCriteria(TextCriteria.forDefaultLanguage().matching(String.valueOf(search)));
                });

        return query;
    }
}
