package org.humancellatlas.ingest.core.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
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

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by rolando on 11/06/2018.
 */
@RequiredArgsConstructor
@Service
public class ResourceLinker {
    private final @NonNull EntityLinks entityLinks;
    private final @NonNull RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    private final @NonNull Environment environment;

    private final HttpHeaders URI_LIST_HEADERS = uriListHeaders();

    private final Logger log = LoggerFactory.getLogger(getClass());

    public void addToRefList(Identifiable sourceEntity, Identifiable targetEntity, String relationship) {
        LinkBuilder processLinkBuilder = entityLinks.linkForSingleResource(sourceEntity);
        URI relationshipUri = localResourceUri(processLinkBuilder.slash(relationship).toUri());
        URI targetUri = localResourceUri(entityLinks.linkForSingleResource(targetEntity).toUri());

        HttpEntity httpEntity = new HttpEntity<>(targetUri.toString(), URI_LIST_HEADERS);

        try {
            this.restTemplate.exchange(relationshipUri.toString(), HttpMethod.PATCH, httpEntity, String.class);
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

    private URI localResourceUri(URI resourceUri) {
        int apiPort = Integer.valueOf(environment.getProperty("server.port"));
        try {
            return new URIBuilder(resourceUri).setHost("localhost").setPort(apiPort).build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(String.format("Failed to build a valid localhost URI for resource at %s", resourceUri), e);
        }
    }

}
