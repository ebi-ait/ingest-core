package uk.ac.ebi.subs.ingest.state;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import uk.ac.ebi.subs.ingest.biomaterial.Biomaterial;
import uk.ac.ebi.subs.ingest.messaging.MessageRouter;
import uk.ac.ebi.subs.ingest.project.Project;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

public class MetadataDocumentEventHandlerTest {

  private MessageRouter messageRouter = mock(MessageRouter.class);
  MetadataDocumentEventHandler handler = new MetadataDocumentEventHandler(messageRouter);

  @Test
  public void testHandleCreateDocumentsWithoutSubmissionEnvelope() {
    Biomaterial biomaterial = new Biomaterial(null);
    handler.handleMetadataDocumentCreate(biomaterial);
    Mockito.verify(messageRouter, times(1)).routeValidationMessageFor(biomaterial);
    // Mockito.verify(messageRouter, times(1)).routeStateTrackingUpdateMessageFor(biomaterial);
  }

  @Test
  public void testHandleCreateDocumentsWithSubmissionEnvelope() {
    Project project = new Project(null);
    SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
    project.getSubmissionEnvelopes().add(submissionEnvelope);
    handler.handleMetadataDocumentCreate(project);
    Mockito.verify(messageRouter, times(1)).routeValidationMessageFor(project);
    // Mockito.verify(messageRouter, times(1)).routeStateTrackingUpdateMessageFor(project);
  }
}
