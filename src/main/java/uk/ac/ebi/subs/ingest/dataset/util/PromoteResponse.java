package uk.ac.ebi.subs.ingest.dataset.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PromoteResponse {
  private String datasetId;
  private String ssmCommandId;
}
