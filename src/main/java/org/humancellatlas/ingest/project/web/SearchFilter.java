package org.humancellatlas.ingest.project.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class SearchFilter {
    @Getter String search;
    @Getter String wranglingState;
    @Getter String wrangler;
}
