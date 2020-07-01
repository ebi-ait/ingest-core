package org.humancellatlas.ingest.state;

import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.notifications.NotificationService;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MetadataStateChangeListenerTest {
    private MessageRouter messageRouter = mock(MessageRouter.class);

    MetadataStateChangeListener metadataDocumentMongoEventListener = new MetadataStateChangeListener(messageRouter);

    @Test
    public void testOnBeforeConvert() {
        Project project = new Project(null);
        metadataDocumentMongoEventListener.onBeforeConvert(new BeforeConvertEvent(project, "project"));
        assertThat(project.getUuid()).isNotNull();
        assertThat(project.getDcpVersion()).isEqualTo(project.getSubmissionDate());
    }

    @Test
    public void testOnAfterSave() {
        Project project = new Project(null);
        AfterSaveEvent<MetadataDocument> afterSaveEvent = mock(AfterSaveEvent.class);
        doReturn(project).when(afterSaveEvent).getSource();
        metadataDocumentMongoEventListener.onAfterSave(afterSaveEvent);
        Mockito.verify(messageRouter, times(1)).routeValidationMessageFor(project);
    }
}
