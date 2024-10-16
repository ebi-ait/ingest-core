package uk.ac.ebi.subs.ingest.schemas.schemascraper.impl;

/** Created by rolando on 19/04/2018. */
public class SchemaScrapeException extends RuntimeException {
  public SchemaScrapeException(String message) {
    super(message);
  }

  public SchemaScrapeException(String message, Throwable cause) {
    super(message, cause);
  }
}
