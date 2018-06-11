package org.humancellatlas.ingest.core.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Created by rolando on 11/06/2018.
 */
@AllArgsConstructor
@Service
public class ResourceLinker {
    private final @NonNull EntityLinks entityLinks;
    private final @NonNull RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

    private final HttpHeaders URI_LIST_HEADERS = uriListHeaders();

    private final Logger log = LoggerFactory.getLogger(getClass());

    public void addToRefList(Identifiable sourceEntity, Identifiable targetEntity, String relationship) {
        LinkBuilder processLinkBuilder = entityLinks.linkForSingleResource(sourceEntity);
        String relationshipUri = processLinkBuilder.slash(relationship).toString();
        String targetUri = entityLinks.linkForSingleResource(targetEntity).toString();

        HttpEntity<String> httpEntity = new HttpEntity<>(targetUri, URI_LIST_HEADERS);

        try {
            this.restTemplate.exchange(relationshipUri, HttpMethod.PATCH, httpEntity, String.class);
        } catch (HttpClientErrorException e) {
            log.trace("Failed to patch link %s to %s", targetUri, relationshipUri);
            throw e;
        }
    }

    private HttpHeaders uriListHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "text/uri-list");
        return headers;
    }
}
