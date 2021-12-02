package org.humancellatlas.ingest.state;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.service.ValidationStateChangeService;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.protocol.Protocol;
import org.springframework.data.rest.core.annotation.HandleAfterLinkDelete;
import org.springframework.data.rest.core.annotation.HandleAfterLinkSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

import java.util.Set;

import static org.humancellatlas.ingest.core.EntityType.*;

@RepositoryEventHandler()
@RequiredArgsConstructor
public class MetadataLinkEventHandler {

    private final @NonNull ValidationStateChangeService validationStateChangeService;

    @HandleAfterLinkDelete
    public void handleProtocolLinkingAfterDelete(Process process, Object unlinked) {
        // TOD0 unlinked is a proxy object and we couldn't find a way to know its type
        // ideally we only want to restrict this to linking done on metadata entity types
        // which can invalidate the graph
        changeValidationStatetoDraft(process, PROCESS);
    }

    @HandleAfterLinkSave
    public void handleProtocolLinkingAfterSave(Process process, Object linked) {
        if (isSetOfType(linked, Protocol.class)) {
            changeValidationStatetoDraft(process, PROCESS);
        }
    }

    @HandleAfterLinkDelete
    public void handleProcessLinkingAfterDelete(File file, Object linked) {
        changeValidationStatetoDraft(file, FILE);
    }

    @HandleAfterLinkSave
    public void handleProcessLinkingAfterSave(File file, Object linked) {
        if (isSetOfType(linked, Process.class)) {
            changeValidationStatetoDraft(file, FILE);
        }
    }

    @HandleAfterLinkDelete
    public void handleProcessLinkingAfterDelete(Biomaterial biomaterial, Object linked) {
        changeValidationStatetoDraft(biomaterial, BIOMATERIAL);
    }

    @HandleAfterLinkSave
    public void handleProcessLinkingAfterSave(Biomaterial biomaterial, Object linked) {
        if (isSetOfType(linked, Process.class)) {
            changeValidationStatetoDraft(biomaterial, BIOMATERIAL);
        }
    }

    private void changeValidationStatetoDraft(MetadataDocument metadataDocument, EntityType entityType) {
        validationStateChangeService.changeValidationState(entityType, metadataDocument.getId(), ValidationState.DRAFT);
    }

    private boolean isSetOfType(Object obj, Class<?> clazz) {
        if (obj instanceof Set) {
            Set<?> set = (Set<?>) obj;
            return (set.iterator().hasNext() && (clazz.isInstance(set.iterator().next())));
        }
        return false;
    }
}