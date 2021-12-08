package org.humancellatlas.ingest.core.service;

import org.humancellatlas.ingest.core.MetadataDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.data.rest.core.UriToEntityConverter;
import org.springframework.stereotype.Service;

@Service
public class UriToEntityConversionService {

    private UriToEntityConverter converter;

    @Autowired
    public UriToEntityConversionService(PersistentEntities entities, RepositoryInvokerFactory invokerFactory,
                                        Repositories repositories) {
        converter = new UriToEntityConverter(entities, invokerFactory, repositories);
    }

    public <T> T convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType){
        return (T) converter.convert(source, sourceType, targetType);
    }

}
