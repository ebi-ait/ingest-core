package org.humancellatlas.ingest.notifications.model;

import lombok.Data;

@Data
public class Checksum {
  private final String type;
  private final String content;
}
