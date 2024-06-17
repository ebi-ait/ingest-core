package org.humancellatlas.ingest.core.exception;

/** Created by rolando on 10/03/2018. */
public class StateTransitionNotAllowed extends RuntimeException {
  public StateTransitionNotAllowed() {}

  public StateTransitionNotAllowed(String message) {
    super(message);
  }
}
