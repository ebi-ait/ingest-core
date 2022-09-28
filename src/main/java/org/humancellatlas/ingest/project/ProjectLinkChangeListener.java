package org.humancellatlas.ingest.project;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@Component
@RepositoryEventHandler
@RequiredArgsConstructor
public class ProjectLinkChangeListener {

    @Autowired
    ProjectService projectService;
    private final @NonNull Logger log = LoggerFactory.getLogger(getClass());

    /**
     * The `linked` parameter, due to a bug in Spring, is passed to the handler as a {@link java.lang.reflect.Proxy}
     * object. This is a proxy to a {@link Collection}. Since we need to respond to associations of
     * {@link SubmissionEnvelope}s and not other properties of {@link Project}, we need to filter the contents
     * of the `linked` Collection.
     *
     * @link <a href="https://stackoverflow.com/questions/70288400/spring-repositoryeventhandler-for-link-modification-missing-link-target-referenc">Stack Overflow ticket</a>
     */
    @HandleBeforeLinkSave
    public void beforeLinkSave(Project project, Object linked) {
        Stream.of(linked)
                .map(Collection.class::cast)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .filter(SubmissionEnvelope.class::isInstance)
                .findAny()
                .ifPresent(o -> {
                    log.info("setting project {} to IN_PROGRESS", project.getUuid().getUuid().toString());
                    projectService.updateWranglingState(project, WranglingState.IN_PROGRESS);
                });
    }
}
