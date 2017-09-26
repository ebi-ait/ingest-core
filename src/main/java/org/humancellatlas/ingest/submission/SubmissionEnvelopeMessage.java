package org.humancellatlas.ingest.submission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.humancellatlas.ingest.core.AbstractEntityMessage;

/**
 * @author Simon Jupp
 * @date 04/09/2017 Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@AllArgsConstructor
@Getter
public class SubmissionEnvelopeMessage implements AbstractEntityMessage {
    private final String documentType;
    private final String documentId;
    private final String documentUuid;
    private final String callbackLink;
}
