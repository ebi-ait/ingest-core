package uk.ac.ebi.subs.ingest.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.ac.ebi.subs.ingest.core.EntityType;
import uk.ac.ebi.subs.ingest.core.Uuid;

/** Created by rolando on 11/09/2017. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationMessage {
  private EntityType entityType;
  private Uuid uuid;
  private Object content;
}
