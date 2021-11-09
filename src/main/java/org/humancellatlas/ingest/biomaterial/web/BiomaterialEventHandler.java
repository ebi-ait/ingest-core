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
    public void handleInputProcessLinkingAfterDelete(Biomaterial biomaterial, Set<Process> inputToProcesses){
        validationStateChangeService.changeValidationState(BIOMATERIAL, biomaterial.getId(), ValidationState.DRAFT);
    }

    @HandleAfterLinkSave
    public void handleInputProcessLinkingAfterSave(Biomaterial biomaterial, Set<Process> inputToProcesses) {
        validationStateChangeService.changeValidationState(BIOMATERIAL, biomaterial.getId(), ValidationState.DRAFT);
    }

    @HandleAfterLinkDelete
    public void handleDerivedProcessLinkingAfterDelete(Biomaterial biomaterial, Set<Process> derivedByProcesses){
        validationStateChangeService.changeValidationState(BIOMATERIAL, biomaterial.getId(), ValidationState.DRAFT);
    }

    @HandleAfterLinkSave
    public void handleDerivedProcessLinkingAfterSave(Biomaterial biomaterial, Set<Process> derivedByProcesses) {
        validationStateChangeService.changeValidationState(BIOMATERIAL, biomaterial.getId(), ValidationState.DRAFT);
    }

}
