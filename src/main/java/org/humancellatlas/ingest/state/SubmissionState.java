package org.humancellatlas.ingest.state;

/**
 * @author Simon Jupp
 * @date 04/09/2017 Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public enum SubmissionState {
  PENDING,
  DRAFT,
  METADATA_VALIDATING,
  METADATA_VALID,
  METADATA_INVALID,
  GRAPH_VALIDATION_REQUESTED,
  GRAPH_VALIDATING,
  GRAPH_VALID,
  GRAPH_INVALID,
  SUBMITTED,
  PROCESSING,
  ARCHIVING,
  ARCHIVED,
  EXPORTING,
  EXPORTED,
  CLEANUP,
  COMPLETE
}
