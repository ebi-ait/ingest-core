package uk.ac.ebi.subs.ingest.project.exception;

public class NonEmptyProject extends Exception {

  public NonEmptyProject() {
    super("Operation cannot be carried out on non-empty Project.");
  }
}
