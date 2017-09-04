package org.humancellatlas.ingest.envelope;

import lombok.Getter;

/**
 * @author Simon Jupp
 * @date 04/09/2017
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Getter
public class SubmissionEnvelopeMessage {

    private String id;
    private String uuid;

    public SubmissionEnvelopeMessage(SubmissionEnvelope submissionEnvelope) {
        this.id = submissionEnvelope.getId();
        this.uuid = submissionEnvelope.getUuid().toString();
    }

}
