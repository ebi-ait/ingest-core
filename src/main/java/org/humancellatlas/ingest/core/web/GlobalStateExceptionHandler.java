package org.humancellatlas.ingest.core.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        getLog().error("Handling IllegalStateException and returing CONFLICT response", e);
        return new ExceptionInfo(request.getRequestURL().toString(), e.getLocalizedMessage());
    }
}
