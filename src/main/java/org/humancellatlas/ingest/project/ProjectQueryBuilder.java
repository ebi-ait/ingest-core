package org.humancellatlas.ingest.project;

import org.humancellatlas.ingest.project.web.SearchFilter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

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

        Optional.ofNullable(searchFilter.getSearch())
                .ifPresent(search -> {
                    criteria_list.add(new Criteria().orOperator(
                            Criteria.where("content.project_core.project_title").regex(search, "i"),
                            Criteria.where("content.project_core.project_description").regex(search, "i"),
                            Criteria.where("content.project_core.project_short_name").regex(search, "i")
                    ));
                });

        Criteria queryCriteria = new Criteria().andOperator(criteria_list.toArray(new Criteria[criteria_list.size()]));
        return new Query().addCriteria(queryCriteria);
    }
}
