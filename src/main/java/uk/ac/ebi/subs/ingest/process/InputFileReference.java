package uk.ac.ebi.subs.ingest.process;

import java.util.UUID;

import lombok.Data;

@Data
public class InputFileReference {
  private UUID inputFileUuid;
}
