package org.humancellatlas.ingest.core.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

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
    public @ResponseBody ExceptionInfo handleIllegalStateException(HttpServletRequest request, Exception e) {
        getLog().warn("Attempted an illegal state transition at '%s';" +
                              "this will generate a CONFLICT RESPONSE", request.getRequestURL().toString());
        getLog().debug("Handling IllegalStateException and returning CONFLICT response", e);
        return new ExceptionInfo(request.getRequestURL().toString(), e.getLocalizedMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public @ResponseBody ExceptionInfo handleOptimisticLock(HttpServletRequest request, Exception e) {
        getLog().warn("Attempt a failed save, likely due to multiple requests, at '%s'; " +
                              "this will generate a CONFLICT RESPONSE", request.getRequestURL().toString());
        getLog().debug("Handling OptimisticLockingFailureException and returning CONFLICT response", e);
        return new ExceptionInfo(request.getRequestURL().toString(), e.getLocalizedMessage());
    }
}
