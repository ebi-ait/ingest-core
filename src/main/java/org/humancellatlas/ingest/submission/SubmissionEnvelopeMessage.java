package org.humancellatlas.ingest.submission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.Assert;

/**
 * @author Simon Jupp
 * @date 04/09/2017 Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@AllArgsConstructor
@Getter
public class SubmissionEnvelopeMessage {
    private final String documentType;
    private final String id;
    private final String uuid;
    private final String callbackLink;
}
