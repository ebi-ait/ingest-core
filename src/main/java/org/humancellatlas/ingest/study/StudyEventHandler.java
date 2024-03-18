package org.humancellatlas.ingest.study;


import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.notifications.NotificationService;
import org.humancellatlas.ingest.user.IdentityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudyEventHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public void registeredStudy(Study study) {
        log.info("A new study [" + study.getUuid() + "] was registered.");
    }

    public void updatedStudy(Study study) {
        log.info("Updated study: {}", study);
    }

    public void deletedStudy(String id) {
        log.info("Deleted study with ID: {}", id);
    }

}
