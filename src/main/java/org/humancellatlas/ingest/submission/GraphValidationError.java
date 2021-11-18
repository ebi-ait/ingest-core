package org.humancellatlas.ingest.submission;
import lombok.Data;

@Data
public class GraphValidationError {
    private String test;
    private String message;
}
