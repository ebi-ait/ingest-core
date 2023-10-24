package org.humancellatlas.ingest.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.humancellatlas.ingest.file.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @see <a href="https://docs.spring.io/spring-framework/docs/2.0.x/reference/aop.html">Spring 2 AOP Docs</a>
 */
@Aspect
@Component
public class RowLevelFilterSecurityAspect {

    // TODO: implement "Before" protection for data modification functions
    @Pointcut("execution(* org.humancellatlas.ingest.file.*Repository.find*(..))")
    public void repositoryFindFunctions(){}
    @Pointcut("execution(* org.springframework.data.repository.PagingAndSortingRepository.find*(..)) ")
    public void repositoryInheritedFindFunctions(){}



    @Around  ("repositoryInheritedFindFunctions() || repositoryFindFunctions()")
    public Object applyRowLevelSecurity(ProceedingJoinPoint joinPoint) throws Throwable {
        Object queryResult = joinPoint.proceed();
        try {
            return new RowlevelSecurityAdviceHelper(joinPoint).filterResult(queryResult);
        } catch (Exception e) {
            throw new RuntimeException(String.format("problem during advice for %s", joinPoint.getSignature().getName()), e);
        }
    }

    class RowlevelSecurityAdviceHelper {
        private final MethodSignature signature;
        private final ProceedingJoinPoint joinPoint;
        private final Method method;
        private final RowLevelFilterSecurity rowLevelFilterSecurity;
        private final SpelHelper spelHelper = new SpelHelper();
        private final Authentication authentication;

        public RowlevelSecurityAdviceHelper(ProceedingJoinPoint joinPoint) {
            this.joinPoint = joinPoint;
            this.signature = (MethodSignature) this.joinPoint.getSignature();
            this.method = signature.getMethod();

            this.rowLevelFilterSecurity =
                Optional.ofNullable(method.getClass().getAnnotation(RowLevelFilterSecurity.class))
                    .orElseGet(()->
                            readAnnotationFromSuperInterface(joinPoint)
                    );
            this.authentication = SecurityContextHolder.getContext().getAuthentication();
        }

        private RowLevelFilterSecurity readAnnotationFromSuperInterface(ProceedingJoinPoint joinPoint) {
            return (RowLevelFilterSecurity) Arrays.stream(joinPoint.getThis().getClass().getInterfaces())
                    .filter(o -> o.getName().contains("humancellatlas"))
                    .flatMap(o -> Arrays.stream(o.getAnnotations()))
                    .filter(a -> a.annotationType().getName().equals(RowLevelFilterSecurity.class.getName()))
                    .findFirst()
                    .get();
        }

        private Object filterDocumentList(Pageable pageable, List<File> documentList) {
            Method method = this.method;
            List<File> retainedDocuments = documentList
                    .stream()
                    .filter(document -> evaluateDocumentExpression(document, method))
                    .collect(Collectors.toList());
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), retainedDocuments.size());
            return new PageImpl<>(retainedDocuments.subList(start, end), pageable, retainedDocuments.size());
        }

        private Boolean evaluateDocumentExpression(File document, Method method) {
            List<String> variableNames = buildVariableNames(method);
            List<Object> variableValues = buildVariableValues(document, authentication);
            String spelExpression = rowLevelFilterSecurity.expression();
            return spelHelper.parseExpression(variableNames, variableValues, spelExpression);
        }

        private List<Object> buildVariableValues(File document, Authentication authentication) {
            List<Object> variableValues = new ArrayList<>();
            variableValues.add(document);
            variableValues.add(authentication);
            variableValues.addAll(List.of(joinPoint.getArgs()));
            return variableValues;
        }

        private List<String> buildVariableNames(Method method) {
            List<String> variableNames = new ArrayList<>();
            variableNames.add("filterObject");
            variableNames.add("authentication");
            Arrays.stream(method.getParameters()).forEach(p -> variableNames.add(p.getName()));
            return variableNames;
        }

        public Object filterResult(Object queryResult) {
            Object result;
            if (queryResult instanceof Page) {
                result = filterPage((Page<File>) queryResult);
            } else if (queryResult instanceof List) {
                result = filterList((List<?>) queryResult);
            } else if (queryResult instanceof Optional) {
                result = filterOptional((Optional<?>) queryResult);

            } else {
                throw new IllegalArgumentException("only supports filtering results of type Page, List, Optional. Type given: " + queryResult.getClass());
            }
            return result;
        }

        private Object filterOptional(Optional<?> queryResult) {
            // if on ignore list - return object
            if(isResultInIgnoreList(queryResult)) {
                return queryResult;
            }
            List<String> variableNames = buildVariableNames(method);
            List<Object> variableValues = buildVariableValues((File)queryResult.get(), authentication);
            if (spelHelper.parseExpression(variableNames, variableValues, rowLevelFilterSecurity.expression())){
                return queryResult;
            }
            throw new AccessDeniedException("access denied");
        }

        private boolean isResultInIgnoreList(Optional<?> queryResult) {
            return Arrays.stream(rowLevelFilterSecurity.ignoreClasses())
                    .anyMatch(queryResult.get().getClass()::isInstance);
        }

        private Object filterList(List<?> queryResult) {
            Object result;
            List<?> documentList = queryResult;
            if (documentList.size() == 0) {
                result = documentList;
            } else {
                Pageable pageable = PageRequest.of(0, documentList.size());
                result = filterDocumentList(pageable, (List<File>) documentList);
            }
            return result;
        }

        private Object filterPage(Page<File> queryResult) {
            Page<File> page = queryResult;
            Pageable pageable = page.getPageable();
            List<File> documentList = page.getContent();
            return filterDocumentList(pageable, documentList);
        }
    }
}
