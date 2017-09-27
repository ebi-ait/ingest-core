package org.humancellatlas.ingest.core.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 27/09/17
 */
@AllArgsConstructor
@Getter
public class ExceptionInfo {
    private final @NonNull String url;
    private final @NonNull String exceptionMessage;
}
