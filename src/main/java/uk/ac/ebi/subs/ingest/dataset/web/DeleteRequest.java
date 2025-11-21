package uk.ac.ebi.subs.ingest.dataset.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DeleteRequest {

    @JsonProperty("paths")
    private List<String> paths;

    @JsonProperty("all_contents")
    private boolean allContents;

    @JsonProperty("recursive")
    private boolean recursive;
}