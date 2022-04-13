package org.humancellatlas.ingest.security;

import org.humancellatlas.ingest.security.exception.NotAllowedException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckAllowed {
    String value();
    Class<? extends NotAllowedException> exception() default NotAllowedException.class;
}
