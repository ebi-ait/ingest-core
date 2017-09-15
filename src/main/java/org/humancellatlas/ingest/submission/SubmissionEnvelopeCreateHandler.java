package org.humancellatlas.ingest.submission;

import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 15/09/17
 */
@RepositoryEventHandler
public class SubmissionEnvelopeCreateHandler {

    @HandleAfterCreate
    public void handleSubmissionEnvelopeCreation(SubmissionEnvelope submissionEnvelope) {

    }
}
