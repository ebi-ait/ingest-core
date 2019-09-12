package org.humancellatlas.ingest.messaging.model;


import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author Simon Jupp
 * @date 04/09/2017 Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@RequiredArgsConstructor
@Getter
public class SubmissionEnvelopeMessage implements AbstractEntityMessage {
    private final @NonNull MessageProtocol messageProtocol;
    private final @NonNull String documentType;
    private final @NonNull String documentId;
    private final @NonNull String documentUuid;
    private final @NonNull String callbackLink;
}
