package uk.ac.ebi.subs.ingest.archiving;

import lombok.Data;

@Data
public class Error {
  private String errorCode;
  private String message;
  private Object details;
}
