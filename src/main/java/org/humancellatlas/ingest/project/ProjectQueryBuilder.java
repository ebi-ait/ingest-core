package org.humancellatlas.ingest.project;

import org.humancellatlas.ingest.project.web.SearchFilter;
import org.humancellatlas.ingest.project.web.SearchType;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                .map(search -> ProjectQueryBuilder.formatSearchString(searchFilter))
                .ifPresent(search -> {
                    query.addCriteria(TextCriteria.forDefaultLanguage().matching(String.valueOf(search)));
                });

        return query;
    }

    private static final Map<SearchType, Function<SearchFilter, String>> x = Map.of(
            SearchType.ExactMatch, (SearchFilter searchFilter)->encloseInQuotes(searchFilter.getSearch()),
            SearchType.AllKeywords, (SearchFilter searchFilter)->Stream.of(splitBySpace(searchFilter))
                    .map(ProjectQueryBuilder::encloseInQuotes)
                    .collect(Collectors.joining(" "))
    );

    protected static String formatSearchString(SearchFilter searchFilter) {
        if(searchFilter.getSearch()!=null && searchFilter.getSearch().contains("\"")) {
            return searchFilter.getSearch();
        }
        return Optional.ofNullable(searchFilter)
                .map(SearchFilter::getSearchType)
                .map(x::get)
                .map(x->x.apply(searchFilter))
                .orElse(searchFilter.getSearch());
    }

    private static String[] splitBySpace(SearchFilter searchFilter) {
        return searchFilter.getSearch().split(" +");
    }

    private static String encloseInQuotes(String search) {
        return "\"" + search + "\"";
    }
}
