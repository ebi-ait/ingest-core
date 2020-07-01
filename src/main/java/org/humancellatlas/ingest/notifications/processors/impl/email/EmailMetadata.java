package org.humancellatlas.ingest.notifications.processors.impl.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailMetadata {
  private String from;
  private String to;
  private String subject;
  private String body;
}
