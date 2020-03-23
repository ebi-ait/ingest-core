package org.humancellatlas.ingest.state;

import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;


public class MetadataDocumentEventHandlerTest {

    private MessageRouter messageRouter = mock(MessageRouter.class);
    MetadataDocumentEventHandler handler = new MetadataDocumentEventHandler(messageRouter);

    @Test
    public void testHandleCreateDocumentsWithoutSubmissionEnvelope() {
        Project project = new Project(null);
        handler.handleMetadataDocumentCreate(project);
        Mockito.verify(messageRouter, times(1)).routeValidationMessageFor(project);
        Mockito.verify(messageRouter, never()).routeStateTrackingUpdateMessageFor(project);
    }

    @Test
    public void testHandleCreateDocumentsWithSubmissionEnvelope() {
        Project project = new Project(null);
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope("sub-1");
        project.setSubmissionEnvelope(submissionEnvelope);
        handler.handleMetadataDocumentCreate(project);
        Mockito.verify(messageRouter, times(1)).routeValidationMessageFor(project);
        Mockito.verify(messageRouter, times(1)).routeStateTrackingUpdateMessageFor(project);
    }


}
