package org.humancellatlas.ingest.security;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.humancellatlas.ingest.security.exception.NotAllowedException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class SecurityAspect {
    @Before("@annotation(org.humancellatlas.ingest.security.CheckAllowed) && execution(* *(..))")
    public void checkAllowed(JoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CheckAllowed annotation = method.getAnnotation(CheckAllowed.class);
        Boolean isAllowed = parseExpression(signature.getParameterNames(), joinPoint.getArgs(), annotation.value());
        Class<? extends NotAllowedException> exceptionClass = annotation.exception();
        if (!isAllowed) {
            throw exceptionClass.getDeclaredConstructor().newInstance();
        }
    }

    private Boolean parseExpression(String[] params, Object[] args, String expression) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();

        for (int i = 0; i < params.length; i++) {
            context.setVariable(params[i], args[i]);
        }

        return parser.parseExpression(expression).getValue(context, Boolean.class);
    }
}
