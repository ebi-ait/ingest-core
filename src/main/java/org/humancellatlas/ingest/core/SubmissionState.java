package org.humancellatlas.ingest.core;

/**
 * @author Simon Jupp
 * @date 04/09/2017
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
