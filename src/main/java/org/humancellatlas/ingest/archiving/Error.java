package org.humancellatlas.ingest.archiving;

import lombok.Data;

@Data
public class Error {
    private String errorCode;
    private String message;
    private Object details;
}
