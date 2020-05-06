package org.humancellatlas.ingest.security.exception;

public class DuplicateAccount extends RuntimeException {

    public DuplicateAccount() {
        super("Operation failed due to Account duplication.");
    }

}
