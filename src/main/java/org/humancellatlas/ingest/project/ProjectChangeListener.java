package org.humancellatlas.ingest.project;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.exception.MultipleOpenSubmissions;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
@Getter
public class ProjectChangeListener extends AbstractMongoEventListener<Project> {
    @Autowired
    @NonNull
    private final MessageRouter messageRouter;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Override
    public void onBeforeSave(BeforeSaveEvent<Project> event) {
        Project project = event.getSource();

        List<SubmissionEnvelope> openSubmissions = project.getSubmissionEnvelopes().stream()
                .filter(env -> env.isOpen())
                .collect(Collectors.toList());

        if (openSubmissions.size() > 1)
            throw new MultipleOpenSubmissions("A project can't have multiple open submissions.");
    }
}
