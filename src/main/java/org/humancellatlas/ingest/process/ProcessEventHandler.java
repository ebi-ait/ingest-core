package org.humancellatlas.ingest.process;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.service.ValidationStateChangeService;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.state.ValidationState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Set;

import static org.humancellatlas.ingest.core.EntityType.PROCESS;

@Component
@RequiredArgsConstructor
public class ProcessEventHandler {

    private final @NonNull ValidationStateChangeService validationStateChangeService;

    @HandleAfterLinkDelete
    public void handleProtocolLinkingAfterDelete(Process process, Set<Protocol> protocols){
        validationStateChangeService.changeValidationState(PROCESS, process.getId(), ValidationState.DRAFT);
    }

    @HandleAfterLinkSave
    public void handleProtocolLinkingAfterSave(Process process, Set<Protocol> protocols) {
        validationStateChangeService.changeValidationState(PROCESS, process.getId(), ValidationState.DRAFT);
    }

}
