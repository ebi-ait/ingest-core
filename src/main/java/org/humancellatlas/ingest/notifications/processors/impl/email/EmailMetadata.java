package org.humancellatlas.ingest.notifications.processors.impl.email;

import lombok.Data;

@Data
public class EmailMetadata {
  private final String from;
  private final String to;
  private final String subject;
  private final String body;
}
