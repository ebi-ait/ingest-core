package org.humancellatlas.ingest.messaging;

import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.Uuid;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Created by rolando on 11/09/2017. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationMessage {
  private EntityType entityType;
  private Uuid uuid;
  private Object content;
}
