package org.humancellatlas.ingest.state;

/**
 * @author Simon Jupp
 * @date 04/09/2017
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public enum SubmissionState {
    PENDING,
    DRAFT,
    VALIDATING, // metadata
    VALID, // metadata
    INVALID, // metadata & graph
    GRAPH_VALIDATION_REQUESTED,
    GRAPH_VALIDATING,
    GRAPH_VALIDATED,
    SUBMITTED,
    PROCESSING,
    ARCHIVING,
    ARCHIVED,
    EXPORTING,
    EXPORTED,
    CLEANUP,
    COMPLETE
}
