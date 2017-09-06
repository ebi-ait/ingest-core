package org.humancellatlas.ingest.envelope;

import lombok.Getter;
import org.springframework.util.Assert;

/**
 * @author Simon Jupp
 * @date 04/09/2017 Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Getter
public class SubmissionEnvelopeMessage {
    private final String id;
    private final String uuid;

    public SubmissionEnvelopeMessage(SubmissionEnvelope submissionEnvelope) {
        Assert.notNull(submissionEnvelope.getUuid(),
                       "Cannot generate a submission message for an envelope with a null UUID");

        this.id = submissionEnvelope.getId();
        this.uuid = submissionEnvelope.getUuid().toString();
    }
}
