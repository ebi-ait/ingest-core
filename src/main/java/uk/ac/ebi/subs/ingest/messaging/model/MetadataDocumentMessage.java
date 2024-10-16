package uk.ac.ebi.subs.ingest.messaging.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.ac.ebi.subs.ingest.state.ValidationState;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 12/09/17
 */
@Getter
@AllArgsConstructor
public class MetadataDocumentMessage {
  private final String documentType;
  private final String documentId;
  private final String documentUuid;
  private final ValidationState validationState;
  private final String callbackLink;
  private final String envelopeId;
}
