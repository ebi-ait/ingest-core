package uk.ac.ebi.subs.ingest.study.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;

/** one entry inside the “target_genes” array */
@Value
public class GeneRef {

  String symbol;
  String href; // may be null when we can’t resolve the gene

  /** let Jackson use the generated all-args constructor */
  @JsonCreator
  public GeneRef(@JsonProperty("symbol") String symbol, @JsonProperty("href") String href) {
    this.symbol = symbol;
    this.href = href;
  }
}
