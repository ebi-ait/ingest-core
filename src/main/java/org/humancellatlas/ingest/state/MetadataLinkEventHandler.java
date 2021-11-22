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
import static org.humancellatlas.ingest.core.EntityType.BIOMATERIAL;

@RepositoryEventHandler()
@RequiredArgsConstructor
public class MetadataLinkEventHandler {

    private final @NonNull ValidationStateChangeService validationStateChangeService;

    @HandleAfterLinkDelete
    public void handleProtocolLinkingAfterDelete(Process process, Set<Protocol> protocols) {
        changeValidationStatetoDraft(process, PROCESS);
    }

    @HandleAfterLinkSave
    public void handleProtocolLinkingAfterSave(Process process, Set<Protocol> protocols) {
        changeValidationStatetoDraft(process, PROCESS);
    }

    @HandleAfterLinkDelete
    public void handleProcessLinkingAfterDelete(File file, Set<Process> processes) {
        changeValidationStatetoDraft(file, FILE);
    }

    @HandleAfterLinkSave
    public void handleProcessLinkingAfterSave(File file, Set<Process> processes) {
        changeValidationStatetoDraft(file, FILE);
    }

    @HandleAfterLinkDelete
    public void handleProcessLinkingAfterDelete(Biomaterial biomaterial, Set<Process> processes) {
        changeValidationStatetoDraft(biomaterial, BIOMATERIAL);
    }

    @HandleAfterLinkSave
    public void handleProcessLinkingAfterSave(Biomaterial biomaterial, Set<Process> processes) {
        changeValidationStatetoDraft(biomaterial, BIOMATERIAL);
    }

    private void changeValidationStatetoDraft(MetadataDocument metadataDocument, EntityType entityType) {
        validationStateChangeService.changeValidationState(entityType, metadataDocument.getId(), ValidationState.DRAFT);

    }
}
