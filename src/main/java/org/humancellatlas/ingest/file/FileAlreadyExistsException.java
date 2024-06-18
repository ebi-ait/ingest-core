package org.humancellatlas.ingest.file;

import lombok.Getter;
import lombok.Setter;

/** Created by rolando on 04/06/2018. */
public class FileAlreadyExistsException extends RuntimeException {
  @Getter @Setter private String fileName;

  public FileAlreadyExistsException() {}

  public FileAlreadyExistsException(String message) {
    super(message);
  }

  public FileAlreadyExistsException(String message, String fileName) {
    super(message);
    this.fileName = fileName;
  }
}
