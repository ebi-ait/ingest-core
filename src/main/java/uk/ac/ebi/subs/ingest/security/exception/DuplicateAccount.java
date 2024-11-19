package uk.ac.ebi.subs.ingest.security.exception;

public class DuplicateAccount extends RuntimeException {

  public DuplicateAccount() {
    super("Operation failed due to Account duplication.");
  }
}
