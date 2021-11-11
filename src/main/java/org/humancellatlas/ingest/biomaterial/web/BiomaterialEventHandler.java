package org.humancellatlas.ingest.biomaterial.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.core.service.ValidationStateChangeService;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.state.ValidationState;
import org.springframework.data.rest.core.annotation.HandleAfterLinkDelete;
import org.springframework.data.rest.core.annotation.HandleAfterLinkSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

import java.util.Set;

import static org.humancellatlas.ingest.core.EntityType.BIOMATERIAL;

@RepositoryEventHandler()
@RequiredArgsConstructor
public class BiomaterialEventHandler {

    private final @NonNull ValidationStateChangeService validationStateChangeService;

    @HandleAfterLinkDelete
    public void handleProcessLinkingAfterDelete(Biomaterial biomaterial, Set<Process> processes) {
        validationStateChangeService.changeValidationState(BIOMATERIAL, biomaterial.getId(), ValidationState.DRAFT);
    }

    @HandleAfterLinkSave
    public void handleProcessLinkingAfterSave(Biomaterial biomaterial, Set<Process> processes) {
        validationStateChangeService.changeValidationState(BIOMATERIAL, biomaterial.getId(), ValidationState.DRAFT);
    }

}
