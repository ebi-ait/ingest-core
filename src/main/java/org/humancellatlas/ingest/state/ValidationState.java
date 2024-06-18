package org.humancellatlas.ingest.state;

import com.fasterxml.jackson.annotation.JsonCreator;

/** Created by rolando on 07/09/2017. */
public enum ValidationState {
  DRAFT,
  VALIDATING,
  VALID,
  INVALID,
  PROCESSING,
  COMPLETE;

  @JsonCreator
  public static ValidationState fromString(String key) {
    return key == null ? null : ValidationState.valueOf(key.toUpperCase());
  }
}
