package org.humancellatlas.ingest.project;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.exception.MultipleOpenSubmissionsException;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Getter
public class ProjectChangeListener extends AbstractMongoEventListener<Project> {
    private final ProjectEventHandler projectEventHandler;

    @Override
    public void onBeforeSave(BeforeSaveEvent<Project> event) {
        Project project = event.getSource();
        if (project.getOpenSubmissionEnvelopes().size() > 1)
            throw new MultipleOpenSubmissionsException("A project can't have multiple open submissions.");
    }

    @Override
    public void onAfterSave(AfterSaveEvent<Project> event) {
        Project project = event.getSource();
        projectEventHandler.editedProjectMetadata(project);
    }
}
