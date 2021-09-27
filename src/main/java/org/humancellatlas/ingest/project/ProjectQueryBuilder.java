package org.humancellatlas.ingest.project;

import org.humancellatlas.ingest.project.web.SearchFilter;
import org.humancellatlas.ingest.project.web.SearchType;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProjectQueryBuilder {

    public static Query buildProjectsQuery(SearchFilter searchFilter) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("isUpdate").is(false));
        addCriterionForAttribute(criteriaList, "wranglingState", searchFilter.getWranglingState());
        addCriterionForAttribute(criteriaList, "primaryWrangler", searchFilter.getPrimaryWrangler());

        Criteria queryCriteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()]));
        Query query = new Query().addCriteria(queryCriteria);
        addKeywordSearchCriteria(searchFilter)
                .ifPresent(query::addCriteria);
        return query;
    }

    private static Optional<CriteriaDefinition> addKeywordSearchCriteria(SearchFilter searchFilter) {
        try {
            return buildUuidCriteria(searchFilter);
        } catch (IllegalArgumentException  e) {
            return buildTextCriteria(searchFilter);
        }
    }

    private static Optional<CriteriaDefinition> buildTextCriteria(SearchFilter searchFilter) {
        return Optional.ofNullable(searchFilter.getSearch())
                .map(search -> ProjectQueryBuilder.formatSearchString(searchFilter))
                .map(search ->
                        TextCriteria.forDefaultLanguage().matching(String.valueOf(search)));
    }

    private static Optional<CriteriaDefinition> buildUuidCriteria(SearchFilter searchFilter) {
        return Optional.ofNullable(searchFilter.getSearch())
                .map(UUID::fromString)
                .map(uuid -> Criteria.where("uuid.uuid").is(uuid));
    }

    private static void addCriterionForAttribute(List<Criteria> criteria_list,
                                                 String attributreName,
                                                 String attributeValue) {
        Optional.ofNullable(attributeValue)
                .map(value -> Criteria.where(attributreName).is(value))
                .ifPresent(criteria_list::add);
    }

    private static final Map<SearchType, Function<SearchFilter, String>> keywordFormatterMap = Map.of(
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
                .map(keywordFormatterMap::get)
                .map(formatterFunction->formatterFunction.apply(searchFilter))
                .orElse(searchFilter.getSearch());
    }

    private static String[] splitBySpace(SearchFilter searchFilter) {
        return searchFilter.getSearch().split(" +");
    }

    private static String encloseInQuotes(String search) {
        return "\"" + search + "\"";
    }
}
