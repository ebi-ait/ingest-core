package org.humancellatlas.ingest.submission;
import lombok.Getter;

public class GraphValidationError {
    @Getter()
    private String test;
    @Getter()
    private String message;
}
