package org.humancellatlas.ingest.core.web;

import org.humancellatlas.ingest.state.InvalidMetadataDocumentStateException;
import org.humancellatlas.ingest.state.InvalidSubmissionStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 27/09/17
 */
@ControllerAdvice
public class GlobalStateExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(IllegalStateException.class)
    public @ResponseBody
    ExceptionInfo handleIllegalStateException(HttpServletRequest request, Exception e) {
        getLog().warn(String.format("Attempted an illegal state transition at '%s';" +
                        "this will generate a CONFLICT RESPONSE",
                request.getRequestURL().toString()));
        getLog().debug("Handling IllegalStateException and returning CONFLICT response", e);
        return new ExceptionInfo(request.getRequestURL().toString(), e.getLocalizedMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public @ResponseBody
    ExceptionInfo handleOptimisticLock(HttpServletRequest request, Exception e) {
        getLog().warn(String.format("Attempt a failed save, likely due to multiple requests, at '%s'; " +
                        "this will generate a CONFLICT RESPONSE",
                request.getRequestURL().toString()));
        getLog().debug("Handling OptimisticLockingFailureException and returning CONFLICT response", e);
        return new ExceptionInfo(request.getRequestURL().toString(), e.getLocalizedMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(InvalidMetadataDocumentStateException.class)
    public @ResponseBody
    ExceptionInfo handleInvalidMetadataDocumentState(HttpServletRequest request, Exception e) {
        getLog().warn(String.format("Attempt a failed metadata document state transition at '%s'; " +
                        "this will generate a CONFLICT RESPONSE",
                request.getRequestURL().toString()));
        getLog().debug("Handling InvalidMetadataDocumentStateException and returning CONFLICT response", e);
        return new ExceptionInfo(request.getRequestURL().toString(), e.getLocalizedMessage());
    }


    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(InvalidSubmissionStateException.class)
    public @ResponseBody ExceptionInfo handleInvalidSubmissionState(HttpServletRequest request, Exception e) {
        getLog().warn(String.format("Attempt a failed submission envelope state transition at '%s'; " +
                        "this will generate a CONFLICT RESPONSE",
                request.getRequestURL().toString()));
        getLog().debug("Handling InvalidSubmissionStateException and returning CONFLICT response", e);
        return new ExceptionInfo(request.getRequestURL().toString(), e.getLocalizedMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public @ResponseBody
    ExceptionInfo handleIllegalArgument(HttpServletRequest request, Exception e) {
        getLog().warn(String.format("Caught an illegal argument at '%s'; " +
                        "this will generate a BAD_REQUEST RESPONSE",
                request.getRequestURL().toString()));
        getLog().debug("Handling IllegalArgumentException and returning BAD_REQUEST response", e);
        return new ExceptionInfo(request.getRequestURL().toString(), e.getLocalizedMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public @ResponseBody ExceptionInfo handleResourceNotFound(HttpServletRequest request, Exception e) {
        getLog().warn(String.format("Caught a resource not found exception argument at '%s'; " +
                "this will generate a NOT_FOUND RESPONSE",
            request.getRequestURL().toString()));
        getLog().debug("Handling ResourceNotFoundException and returning NOT_FOUND response", e);
        return new ExceptionInfo(request.getRequestURL().toString(), e.getLocalizedMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public @ResponseBody ExceptionInfo handleRuntimeException(HttpServletRequest request, Exception e) {
        getLog().error(String.format("Runtime exception encountered on %s request to resource %s ", request.getMethod(),
                request.getRequestURL().toString()), e);
        getLog().debug("Handling RuntimeException and returning INTERNAL_SERVER_ERROR response", e);
        return new ExceptionInfo(request.getRequestURL().toString(), "Internal server error");
    }
}
