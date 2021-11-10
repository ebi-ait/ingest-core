package org.humancellatlas.ingest.file.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.core.service.ValidationStateChangeService;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.state.ValidationState;
import org.springframework.data.rest.core.annotation.HandleAfterLinkDelete;
import org.springframework.data.rest.core.annotation.HandleAfterLinkSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

import java.util.Set;

import static org.humancellatlas.ingest.core.EntityType.BIOMATERIAL;
import static org.humancellatlas.ingest.core.EntityType.FILE;

@RepositoryEventHandler()
@RequiredArgsConstructor
public class FileEventHandler {

    private final @NonNull ValidationStateChangeService validationStateChangeService;

    @HandleAfterLinkDelete
    public void handleInputProcessLinkingAfterDelete(File file, Set<Process> processes) {
        validationStateChangeService.changeValidationState(FILE, file.getId(), ValidationState.DRAFT);
    }

    @HandleAfterLinkSave
    public void handleInputProcessLinkingAfterSave(File file, Set<Process> processes) {
        validationStateChangeService.changeValidationState(FILE, file.getId(), ValidationState.DRAFT);
    }
}
