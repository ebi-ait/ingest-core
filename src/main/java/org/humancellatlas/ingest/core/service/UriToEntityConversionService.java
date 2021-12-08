package org.humancellatlas.ingest.core.service;

import org.humancellatlas.ingest.core.MetadataDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.data.rest.core.UriToEntityConverter;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Service
public class UriToEntityConversionService {

    private UriToEntityConverter converter;

    @Autowired
    public UriToEntityConversionService(PersistentEntities entities, RepositoryInvokerFactory invokerFactory,
                                        Repositories repositories) {
        converter = new UriToEntityConverter(entities, invokerFactory, repositories);
    }

    public <T> T convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        return (T) converter.convert(source, sourceType, targetType);
    }

    public <T> T convertLink(Link link, TypeDescriptor targetType) throws URISyntaxException {
        URI uri = new URI(link.getHref());
        return (T) convert(uri, TypeDescriptor.valueOf(URI.class), targetType);
    }

    public <T> List<T> convertLinks(List<Link> links, Class<?> clazz) throws URISyntaxException {
        List<T> list = new ArrayList<>();
        for (Link link : links) list.add((T) convertLink(link, TypeDescriptor.valueOf(clazz)));
        return list;
    }

}
