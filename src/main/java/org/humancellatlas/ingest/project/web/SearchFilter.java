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
    @Getter Integer priority;
    @Getter Boolean hcaPublication;
    @Getter String species;
    @Getter String organ;

    @Builder.Default
    @Getter SearchType searchType = SearchType.AllKeywords;
    @Builder.Default
    @Getter Integer minCellCount = 0;
    @Builder.Default
    @Getter Integer maxCellCount = Integer.MAX_VALUE;
}
