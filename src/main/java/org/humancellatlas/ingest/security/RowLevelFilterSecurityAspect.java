package org.humancellatlas.ingest.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.humancellatlas.ingest.file.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
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

    private final SpelHelper spelHelper = new SpelHelper();

// TODO: implement "Before" protection for data modification functions
    @Around("execution(* org.humancellatlas.ingest.file.FileRepository.find*(..)) " +
            "&& @within(org.humancellatlas.ingest.security.RowLevelFilterSecurity)")
    public Object applyRowLevelSecurity(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result;
        // Proceed with the original method execution
        Object queryResult = joinPoint.proceed();
        // Get the currently authenticated user

        if (queryResult instanceof Page) {
            Page<File> page = (Page<File>) queryResult;
            Pageable pageable = page.getPageable();
            List<File> documentList = page.getContent();

            result = filterDocumentList(joinPoint, pageable, documentList);
        } else if (queryResult instanceof List){
            List<?> documentList = (List<?>) queryResult;
            if (documentList.size()==0) {
                result = documentList;
            } else {
                Pageable pageable = PageRequest.of(0, documentList.size());
                result = filterDocumentList(joinPoint, pageable, (List<File>) documentList);
            }
        } else {
            throw new IllegalArgumentException("only supports filtering results of type Page. Type given: " + queryResult.getClass());
        }


        return result;
    }

    private Object filterDocumentList(ProceedingJoinPoint joinPoint, Pageable pageable, List<File> documentList) {
        Object result;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RowLevelFilterSecurity rowLevelFilterSecurity = method.getClass().getAnnotation(RowLevelFilterSecurity.class);
        List<File> retainedDocuments = documentList
                .stream()
                .filter(document -> {
                    List<String> variableNames = new ArrayList<>();
                    variableNames.add("filterObject");
                    variableNames.add("authentication");
                    Arrays.stream(method.getParameters()).forEach(p -> variableNames.add(p.getName()));
                    List<Object> variableValues = new ArrayList<>();
                    variableValues.add(document);
                    variableValues.add(authentication);
                    variableValues.addAll(List.of(joinPoint.getArgs()));
                    return spelHelper.parseExpression(variableNames, variableValues, rowLevelFilterSecurity.value());
                })
                .collect(Collectors.toList());
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), retainedDocuments.size());
        result = new PageImpl<>(retainedDocuments.subList(start, end), pageable, retainedDocuments.size());
        return result;
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
