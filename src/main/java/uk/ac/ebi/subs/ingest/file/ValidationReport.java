package uk.ac.ebi.subs.ingest.file;

import java.util.List;

import lombok.Data;
import uk.ac.ebi.subs.ingest.state.ValidationState;

@Data
public class ValidationReport {
  private ValidationState validationState;
  private List<Object> validationErrors;

  protected ValidationReport() {}
}
