package org.humancellatlas.ingest.submission;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.config.ConfigurationService;
import org.humancellatlas.ingest.state.ValidationState;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import sun.applet.resources.MsgAppletViewer;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

/**
 * Created by rolando on 05/09/2018.
 */
@Service
@AllArgsConstructor
public class SubmissionStateMachineService {
    private final @NonNull RestOperations restOperations = new RestTemplate( );
    private final @NonNull ConfigurationService configurationService;

    private static HttpEntity DEFAULT_HTTP_ENTITY = defaultHttpEntity();

    public Map<String, ValidationState> documentStatesForEnvelope(SubmissionEnvelope submissionEnvelope) {
        UUID envelopeUuid = submissionEnvelope.getUuid().getUuid();
        ParameterizedTypeReference<Map<String, ValidationState>> documentStatesType = new ParameterizedTypeReference<Map<String, ValidationState>>() {};
        URI documentStatesUri = UriComponentsBuilder.newInstance()
                                                    .scheme(configurationService.getStateTrackerScheme())
                                                    .host(configurationService.getStateTrackerHost())
                                                    .port(configurationService.getStateTrackerPort())
                                                    .pathSegment(configurationService.getDocumentStatesPath(), envelopeUuid.toString())
                                                    .build()
                                                    .toUri();

        return restOperations.exchange(documentStatesUri,
                                       HttpMethod.GET,
                                       DEFAULT_HTTP_ENTITY,
                                       documentStatesType)
                             .getBody();
    }

    private static HttpEntity defaultHttpEntity() {
        return new HttpEntity<>(null, uriListHeaders());
    }

    private static HttpHeaders uriListHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/json");
        return headers;
    }
}
