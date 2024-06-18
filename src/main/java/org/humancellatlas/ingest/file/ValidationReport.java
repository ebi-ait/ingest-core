package org.humancellatlas.ingest.file;

import java.util.List;

import org.humancellatlas.ingest.state.ValidationState;

import lombok.Data;

@Data
public class ValidationReport {
  private ValidationState validationState;
  private List<Object> validationErrors;

  protected ValidationReport() {}
}
