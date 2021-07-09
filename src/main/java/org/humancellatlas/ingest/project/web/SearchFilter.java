package org.humancellatlas.ingest.project.web;

import lombok.Getter;

public class SearchFilter {
    @Getter String search;
    @Getter String wranglingState;
    @Getter String wrangler;

    public SearchFilter(String search, String wranglingState, String wrangler) {
        this.search = search;
        this.wranglingState = wranglingState;
        this.wrangler = wrangler;
    }
}
