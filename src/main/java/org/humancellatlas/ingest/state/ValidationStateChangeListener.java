package org.humancellatlas.ingest.state;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.ValidationEvent;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidationStateChangeListener implements ApplicationListener<ValidationEvent> {
    private final @NonNull MessageRouter messageRouter;

    @Override
    public void onApplicationEvent(ValidationEvent event) {
        messageRouter.routeStateTrackingUpdateMessageFor((MetadataDocument) event.getSource());
    }
}
