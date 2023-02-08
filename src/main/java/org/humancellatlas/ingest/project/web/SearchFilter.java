package org.humancellatlas.ingest.project.web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.humancellatlas.ingest.project.DataAccessTypes;

import java.util.List;

@AllArgsConstructor
@Builder
@ToString
public class SearchFilter {
    @Getter String search;
    @Getter String wranglingState;
    @Getter String primaryWrangler;
    @Getter Integer wranglingPriority;
    @Getter Boolean hasOfficialHcaPublication;
    @Getter String identifyingOrganism;
    @Getter String organOntology;
    @Getter Integer minCellCount;
    @Getter Integer maxCellCount;
    @Getter Integer dcpReleaseNumber;
    @Getter DataAccessTypes dataAccess;
    @Getter String projectLabels;
    @Getter String projectNetwork;
    @Getter Boolean hcaCatalogue;

    @Builder.Default
    @Getter SearchType searchType = SearchType.AllKeywords;
}
