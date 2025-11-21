package uk.ac.ebi.subs.ingest.dataset.web;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class DeleteRequest {

  @JsonProperty("paths")
  private List<String> paths;

  @JsonProperty("all_contents")
  private boolean allContents;

  @JsonProperty("recursive")
  private boolean recursive;
}
