package org.humancellatlas.ingest.project;

import org.assertj.core.api.Assertions;
import org.humancellatlas.ingest.project.web.SearchFilter;
import org.humancellatlas.ingest.project.web.SearchType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

public class ProjectFilterQueryBuilderTest {
    @Test
    void null_search_type_with_non_null_text() {
        SearchFilter searchFilter = SearchFilter.builder()
                .search("project keyword")
                .wranglingState(null)
                .primaryWrangler(null)
                .wranglingPriority(null)
                .hasOfficialHcaPublication(null)
                .identifyingOrganism(null)
                .organOntology(null)
                .minCellCount(null)
                .maxCellCount(null)
                .projectLabels(null)
                .dcpReleaseNumber(null)
                .dataAccess(null)
                .searchType(null)
                .build();


        ProjectQueryBuilder.buildProjectsQuery(searchFilter);
        // no exception thrown when searchType is null
    }

    @ParameterizedTest
    @CsvSource({
            "AllKeywords,k1 k2,\"k1\" \"k2\"",
            "AnyKeyword,k1 k2,k1 k2",
            "ExactMatch,k1 k2,\"k1 k2\"",

            "null,k1 k2,k1 k2",

            "AllKeywords,\"k1 k2\",\"k1 k2\"",
            "AnyKeyword,k1 \"k2\" k3,k1 \"k2\" k3",
            "ExactMatch,\"k1\" k2,\"k1\" k2",
    })
    public void quoting_in_mongo_syntax_by_search_type(String searchTypeStr, String input, String expected) {
        SearchType searchType = searchTypeStr.equals("null") ? null : SearchType.valueOf(searchTypeStr);
        SearchFilter searchFilter = SearchFilter.builder()
                .search(input)
                .searchType(searchType)
                .build();
        Assertions.assertThat(ProjectQueryBuilder.formatSearchString(searchFilter))
                .isEqualTo(expected);
    }

}
