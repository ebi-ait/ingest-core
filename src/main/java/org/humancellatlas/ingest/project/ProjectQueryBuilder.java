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
        addIsCriterionForAttribute(criteriaList, "wranglingState", searchFilter.getWranglingState());
        addIsCriterionForAttribute(criteriaList, "primaryWrangler", searchFilter.getPrimaryWrangler());
        addIsCriterionForAttribute(criteriaList, "wranglingPriority", searchFilter.getWranglingPriority());
        addLTECriterionForAttribute(criteriaList, "cellCount", searchFilter.getMaxCellCount());
        addGTECriterionForAttribute(criteriaList, "cellCount", searchFilter.getMinCellCount());
        addInCriterionForAttribute(criteriaList, "identifyingOrganisms", searchFilter.getIdentifyingOrganism());
        addInCriterionForAttribute(criteriaList, "dcpReleaseNumber", searchFilter.getDcpReleaseNumber());
        addInCriterionForAttribute(criteriaList, "wranglingLabels", searchFilter.getLabels());

        if(searchFilter.getDataAccess() != null){
            addIsCriterionForAttribute(criteriaList, "dataAccess.type", searchFilter.getDataAccess().getLabel());
        }

        Optional.ofNullable(searchFilter.getHasOfficialHcaPublication())
                .map(value ->
                        Criteria.where("content.publications")
                                .elemMatch(Criteria.where("official_hca_publication").is(value))
                ).ifPresent(criteriaList::add);

        Optional.ofNullable(searchFilter.getOrganOntology())
                .map(value ->
                        Criteria.where("organ.ontologies").elemMatch(Criteria.where("ontology").is(value))
                ).ifPresent(criteriaList::add);

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

    private static void addIsCriterionForAttribute(List<Criteria> criteria_list,
                                                   String attributeName,
                                                   Object attributeValue) {
        Optional.ofNullable(attributeValue)
                .map(value -> Criteria.where(attributeName).is(value))
                .ifPresent(criteria_list::add);
    }

    private static void addLTECriterionForAttribute(List<Criteria> criteria_list,
                                                       String attributeName,
                                                       Integer attributeValue) {
        Optional.ofNullable(attributeValue)
                .map(value -> Criteria.where(attributeName).lte(value))
                .ifPresent(criteria_list::add);
    }

    private static void addGTECriterionForAttribute(List<Criteria> criteria_list,
                                                    String attributeName,
                                                    Integer attributeValue) {
        Optional.ofNullable(attributeValue)
                .map(value -> Criteria.where(attributeName).gte(value))
                .ifPresent(criteria_list::add);
    }

    private static void addInCriterionForAttribute(List<Criteria> criteria_list,
                                                    String attributeName,
                                                    Object attributeValue) {
        Optional.ofNullable(attributeValue)
                .map(value -> Criteria.where(attributeName).in(value))
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
