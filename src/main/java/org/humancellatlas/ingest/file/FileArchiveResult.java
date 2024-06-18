package org.humancellatlas.ingest.file;

import java.time.Instant;

import lombok.Data;

@Data
public class FileArchiveResult {
  private Instant lastArchived;
  private Boolean compressed;
  private String md5;
  private String enaUploadPath;
  private String error;

  protected FileArchiveResult() {}
}
