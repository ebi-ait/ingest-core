package org.humancellatlas.ingest.project;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

public enum DataAccessTypes {
  @JsonProperty("OPEN")
  OPEN("All fully open"),
  @JsonProperty("MANAGED")
  MANAGED("All managed access"),
  @JsonProperty("MIXTURE")
  MIXTURE("A mixture of open and managed"),
  @JsonProperty("COMPLICATED")
  COMPLICATED("It's complicated");

  @Getter final String label;

  DataAccessTypes(String label) {
    this.label = label;
  }

  public static DataAccessTypes fromLabel(String label) {
    return Arrays.stream(DataAccessTypes.values())
        .filter(s -> s.getLabel().equals(label))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(label));
  }
}
