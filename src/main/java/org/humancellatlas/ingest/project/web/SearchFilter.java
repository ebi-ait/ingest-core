package org.humancellatlas.ingest.project.web;

public class SearchFilter {
    String search;
    String wranglingState;
    String wrangler;

    public SearchFilter(String search, String wranglingState, String wrangler) {
        this.search = search;
        this.wranglingState = wranglingState;
        this.wrangler = wrangler;
    }

    public String getSearch() {
        return search;
    }

    public String getWranglingState() {
        return wranglingState;
    }

    public String getWrangler() {
        return wrangler;
    }
}
