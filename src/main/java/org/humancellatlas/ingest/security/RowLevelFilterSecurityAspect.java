package org.humancellatlas.ingest.security;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.humancellatlas.ingest.file.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Aspect
@Component
public class RowLevelFilterSecurityAspect<T> {

    @Autowired
    private AccountRepository userRepository; // Your user repository

    @Autowired
    private MongoTemplate mongoTemplate; // Spring Data MongoDB's MongoTemplate

    @Around("execution(* org.humancellatlas.ingest.file.FileRepository.findAll(..))")
    public Object applyRowLevelSecurity(ProceedingJoinPoint joinPoint) throws Throwable {
        // Proceed with the original method execution
        Object queryResult = joinPoint.proceed();
        // Get the currently authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        // TODO this filtering criteria should be a SPEL expression
        Set<String> allowedProjectsForUser = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(s -> s.startsWith("ROLE_access_"))
                .map(s -> s.replace("ROLE_access_", ""))
                .collect(Collectors.toUnmodifiableSet());
        if (queryResult instanceof Page) {
            Page<File> page = (Page<File>) queryResult;
            List<File> retainedDocuments = page.getContent()
                    .stream()
                    .filter(document -> allowedProjectsForUser.contains(document.getProject().getUuid().toString()))
                    .collect(Collectors.toList());
            Pageable pageable = page.getPageable();
            int start = (int) pageable.getOffset();

            int end = (int) ((start + pageable.getPageSize()) > retainedDocuments.size() ? retainedDocuments.size()
                    : (start + pageable.getPageSize()));
            Page<File> filteredPage
                    = new PageImpl<File>(retainedDocuments.subList(start, end), pageable, retainedDocuments.size());
            return filteredPage;
        } else {
            throw new IllegalArgumentException("only supports filtering results of type Page. Type given: " + queryResult.getClass());
        }


    }
//        User user = userRepository.findByUsername(authentication.getName());

    // Define the query modification logic based on user attributes or roles
//        Query modifiedQuery = new Query();
    // Implement your custom logic here to modify the query

    // Proceed with the original method execution using the modified query
//    Object[] args = joinPoint.getArgs();
//        args[0] = modifiedQuery; // Assuming the query is the first parameter
//        return joinPoint.proceed(args);
//        Object filterTarget;
//        Expression filterExpression;
//        EvaluationContext ctx;
//        if (ExpressionUtils.evaluateAsBoolean(filterExpression, ctx)) {
//            retainList.add(filterObject);
//        }
}
//}
