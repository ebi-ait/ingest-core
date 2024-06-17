package org.humancellatlas.ingest.messaging.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Simon Jupp
 * @date 04/09/2017 Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@AllArgsConstructor
@Getter
public class SubmissionEnvelopeMessage {
  private final String documentType;
  private final String documentId;
  private final String documentUuid;
  private final String callbackLink;
}
