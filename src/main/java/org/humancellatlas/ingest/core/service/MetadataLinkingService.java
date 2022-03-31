package org.humancellatlas.ingest.core.service;

import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import reactor.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Service
public class MetadataLinkingService {

    private static final long RETRY_BACKOFF_MS = 100;
    private static final int RETRY_MAX_ATTEMPTS = 5;

    private ValidationStateChangeService validationStateChangeService;

    private MongoTemplate mongoTemplate;

    @Autowired
    public MetadataLinkingService(ValidationStateChangeService validationStateChangeService, MongoTemplate mongoTemplate) {
        this.validationStateChangeService = validationStateChangeService;
        this.mongoTemplate = mongoTemplate;
    }

    public <S extends MetadataDocument, T extends MetadataDocument> T updateLinks(T targetEntity, List<S> entitiesToLink, String linkProperty, Boolean replace) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (replace) {
            replaceLinks(targetEntity, entitiesToLink, linkProperty);
        } else {
            addLinks(targetEntity, entitiesToLink, linkProperty);
        }
        return targetEntity;
    }

    public <S extends MetadataDocument, T extends MetadataDocument> T addLinks(T targetEntity, List<S> entitiesToLink, String linkProperty) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = getGetterMethod(targetEntity, entitiesToLink.get(0).getClass(), linkProperty);
        Set<S> linkedEntities = (Set<S>) invoke(targetEntity, method);
        entitiesToLink.forEach(doc -> {
            linkedEntities.add(doc);
        });
        mongoTemplate.save(targetEntity);

        setValidationStateToDraftIfGraphValid(targetEntity);

        return targetEntity;
    }

    public <S extends MetadataDocument, T extends MetadataDocument> T replaceLinks(T targetEntity, List<S> entitiesToLink, String linkProperty) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = getGetterMethod(targetEntity, entitiesToLink.get(0).getClass(), linkProperty);
        Set<S> linkedEntities = (Set<S>) invoke(targetEntity, method);
        linkedEntities.clear();
        linkedEntities.addAll(entitiesToLink);
        mongoTemplate.save(targetEntity);

        setValidationStateToDraftIfGraphValid(targetEntity);

        return targetEntity;
    }

    public <S extends MetadataDocument, T extends MetadataDocument> T removeLink(T targetEntity, S entityToUnlink, String linkProperty) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = getGetterMethod(targetEntity, entityToUnlink.getClass(), linkProperty);
        Set<S> linkedEntities = (Set<S>) invoke(targetEntity, method);
        linkedEntities.remove(entityToUnlink);
        mongoTemplate.save(targetEntity);

        setValidationStateToDraftIfGraphValid(targetEntity);

        return targetEntity;
    }

    private <T extends MetadataDocument> Method getGetterMethod(T metadataDocument, Class<?> parameterType, String linkProperty) throws NoSuchMethodException {
        return metadataDocument.getClass().getMethod("get" + StringUtils.capitalize(linkProperty));
    }

    private <T extends MetadataDocument> Object invoke(T metadataDocument, Method method) throws IllegalAccessException, InvocationTargetException {
        return method.invoke(metadataDocument);
    }

    private void setValidationStateToDraftIfGraphValid(MetadataDocument... entities) {
        Arrays.stream(entities).forEach(entity -> {
            SubmissionEnvelope submission = entity.getSubmissionEnvelope();
            if (submission != null && submission.getSubmissionState().equals(SubmissionState.GRAPH_VALID)) {
                setToDraft(entity);
            }
        });
    }

    private void setToDraft(MetadataDocument entity) {
        RetryTemplate retry = RetryTemplate.builder()
                .maxAttempts(RETRY_MAX_ATTEMPTS)
                .fixedBackoff(RETRY_BACKOFF_MS)
                .retryOn(OptimisticLockingFailureException.class)
                .build();
        retry.execute(context -> {
            return validationStateChangeService.changeValidationState(entity.getType(), entity.getId(), ValidationState.DRAFT);
        });
    }
}
