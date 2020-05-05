package org.humancellatlas.ingest.notifications.model;

import lombok.Data;

@Data
public class Checksum {
  private final String type;
  private final String value;

  public String toString() {
    return String.format("{ Checksum - type: %s, value: %s }", type, value);
  }
}
