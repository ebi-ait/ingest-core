package org.humancellatlas.ingest.file.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.service.ValidationStateChangeService;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.state.ValidationState;
import org.springframework.data.rest.core.annotation.HandleAfterLinkDelete;
import org.springframework.data.rest.core.annotation.HandleAfterLinkSave;
import org.springframework.stereotype.Component;

import java.util.Set;

import static org.humancellatlas.ingest.core.EntityType.FILE;

@Component
@RequiredArgsConstructor
public class FileEventHandler {

    private final @NonNull ValidationStateChangeService validationStateChangeService;

    @HandleAfterLinkDelete
    public void handleProcessLinkingAfterDelete(File file, Set<Process> processes) {
        validationStateChangeService.changeValidationState(FILE, file.getId(), ValidationState.DRAFT);
    }

    @HandleAfterLinkSave
    public void handleProcessLinkingAfterSave(File file, Set<Process> processes) {
        validationStateChangeService.changeValidationState(FILE, file.getId(), ValidationState.DRAFT);
    }
}
