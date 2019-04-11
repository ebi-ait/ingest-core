package org.humancellatlas.ingest.core.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett

 */
@AllArgsConstructor
@Getter
public class ExceptionInfo {
    private final @NonNull String url;
    private final @NonNull String exceptionMessage;
}
