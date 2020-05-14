package org.humancellatlas.ingest.state;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.ValidationEvent;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectNotifications;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidationStateChangeListener implements ApplicationListener<ValidationEvent> {
    private final @NonNull MessageRouter messageRouter;
    private final @NonNull ProjectNotifications projectNotifications;

    @Override
    public void onApplicationEvent(ValidationEvent event) {
        MetadataDocument document = (MetadataDocument) event.getSource();
        messageRouter.routeStateTrackingUpdateMessageFor(document);

        if(document.getType().equals(EntityType.PROJECT)) {
            projectNotifications.validatedProject((Project) document);
        }
    }
}
