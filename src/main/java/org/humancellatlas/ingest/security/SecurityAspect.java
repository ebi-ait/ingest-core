package org.humancellatlas.ingest.security;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.humancellatlas.ingest.security.exception.NotAllowedException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class SecurityAspect {
    private final SpelHelper spelHelper = new SpelHelper();

    /**
     * Advice that runs before any method with the CheckAllowed annotation.
     * Parses the given SpEL in the annotation and throws error if the result returns False.
     *
     * @param joinPoint
     * @throws Throwable
     */
    @Before("@annotation(org.humancellatlas.ingest.security.CheckAllowed) && execution(* *(..))")
    public void checkAllowed(JoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CheckAllowed annotation = method.getAnnotation(CheckAllowed.class);
        Boolean isAllowed = spelHelper.parseExpression(signature.getParameterNames(), joinPoint.getArgs(), annotation.value());
        Class<? extends NotAllowedException> exceptionClass = annotation.exception();
        if (!isAllowed) {
            throw exceptionClass.getDeclaredConstructor().newInstance();
        }
    }

    private Boolean parseExpression(String[] params, Object[] args, String expression) {
        return spelHelper.parseExpression(params, args, expression);
    }
}
