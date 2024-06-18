package org.humancellatlas.ingest.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.humancellatlas.ingest.security.exception.NotAllowedException;

/**
 * Annotation for allowing a method to proceed based on the evaluation of a given SpEL input. See:
 * https://docs.spring.io/spring-framework/docs/3.1.0.M1/spring-framework-reference/html/expressions.html
 * for SpEL docs
 *
 * <p>Allows a value (SpEL input) and an optional custom exception
 *
 * <p>E.g.: @CheckAllowed("#foo.bar") public void myMethod(Object foo) { ... }
 *
 * <p>or: @CheckAllowed(value = "#foo.bar", exception = MyCustomException.class) public void
 * myMethod(Object foo) { ... }
 *
 * <p>See SecurityAspect for the aspect and advice that use this.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckAllowed {
  String value();

  Class<? extends NotAllowedException> exception() default NotAllowedException.class;
}
