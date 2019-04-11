package org.humancellatlas.ingest.state;

/**
 * @author Simon Jupp

 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public enum SubmissionState {
    PENDING,
    DRAFT,
    VALIDATING,
    VALID,
    INVALID,
    SUBMITTED,
    PROCESSING,
    CLEANUP,
    COMPLETE
}
