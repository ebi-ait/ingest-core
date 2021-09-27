package org.humancellatlas.ingest.project.web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Builder
@ToString
public class SearchFilter {
    @Getter String search;
    @Getter String wranglingState;
    @Getter String primaryWrangler;

    @Builder.Default
    @Getter SearchType searchType = SearchType.AllKeywords;
}
