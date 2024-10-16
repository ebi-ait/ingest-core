package uk.ac.ebi.subs.ingest.audit;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AuditType {
  STATUS_UPDATED("Status updated");

  protected String value;

  AuditType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return this.value;
  }
}
