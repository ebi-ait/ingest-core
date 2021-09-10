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
        SearchFilter searchFilter = new SearchFilter("project keyword",null,null,null,null);
        ProjectQueryBuilder.buildProjectsQuery(searchFilter);
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
    public void test_name_me(String searchTypeStr, String input, String expected) {
        SearchType searchType = searchTypeStr.equals("null")?null:SearchType.valueOf(searchTypeStr);
        SearchFilter searchFilter = SearchFilter.builder()
                .search(input)
                .searchType(searchType)
                .build();
        Assertions.assertThat(ProjectQueryBuilder.formatSearchString(searchFilter))
                .isEqualTo(expected);
    }
    // null k1 k2 -> k1 k2

    // * "k1 k2" -> "k1 k2"
    // * "k1" "k2" -> "k1" "k2"
    // * k1 "k2" k3 -> k1 "k2" k3

}
