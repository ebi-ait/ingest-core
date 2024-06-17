package org.humancellatlas.ingest.core;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JsonPatch {
  private JsonNode patch;

  public JsonPatch() {}
}
