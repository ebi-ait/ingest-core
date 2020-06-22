package org.humancellatlas.ingest.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.notifications.NotificationService;
import org.humancellatlas.ingest.notifications.exception.DuplicateNotification;
import org.humancellatlas.ingest.notifications.model.Checksum;
import org.humancellatlas.ingest.notifications.model.Notification;
import org.humancellatlas.ingest.notifications.model.NotificationRequest;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.user.IdentityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

@Component
@RequiredArgsConstructor
public class ProjectEventHandler {
  private final NotificationService notificationService;
  private final Environment environment;
  private final IdentityService identityService;

  private final Logger log = LoggerFactory.getLogger(getClass());

  public Notification editedProjectMetadata(Project project) {
    String notificationContent = String.format("Project %s was updated:\n\nNew content:\n\n%s",
                                               project.getUuid().getUuid().toString(),
                                               objectToPrettyString(project.getContent()));
    Checksum checksum = editedProjectChecksum(project);

    return notifyWranglersByEmail(notificationContent, checksum);
  }

  public Notification deletedProject(Project project) {
    String notificationContent = String.format("Project %s was deleted:\n\n%s",
                                               project.getUuid().getUuid().toString(),
                                               objectToPrettyString(project.getContent()));

    Checksum checksum = deletedProjectChecksum(project);

    return notifyWranglersByEmail(notificationContent, checksum);
  }

  public Optional<Notification> validatedProject(Project project) {
    if (project.getValidationState().equals(ValidationState.VALID)) {
      String notificationContent = String.format("Project %s has been validated:\n\n%s",
                                                 project.getUuid().getUuid().toString(),
                                                 objectToPrettyString(project.getContent()));

      Checksum checksum = validProjectChecksum(project);

      return Optional.of(notifyWranglersByEmail(notificationContent, checksum));
    } else {
      return Optional.empty();
    }
  }

  private Notification notifyWranglersByEmail(String notificationContent, Checksum notificationChecksum) {
    var notificationMetadata = new HashMap<String, Object>();
    var emailMetadata = new HashMap<String, String>();
    emailMetadata.put("to", this.emailNotificationsFromAddress());
    emailMetadata.put("from", identityService.wranglerEmail());
    emailMetadata.put("subject", "HCA DCP project update");
    emailMetadata.put("body", notificationContent);
    notificationMetadata.put("email", emailMetadata);

    NotificationRequest notificationRequest = new NotificationRequest(notificationContent,
                                                                      notificationMetadata,
                                                                      notificationChecksum);
    try {
      return this.notificationService.createNotification(notificationRequest);
    } catch (DuplicateNotification e) {
      return this.notificationService.retrieveForChecksum(notificationChecksum)
                                     .orElseThrow(() -> {
                                       log.error("Duplicate notification for non-existent checksum");
                                       throw new RuntimeException(e);
                                     });
    }
  }

  private Checksum editedProjectChecksum(Project project) {
    String checksumInput = String.format("%s:%s", "project-edited", project.getUuid().getUuid());
    return new Checksum("project-edited",
                        DigestUtils.md5DigestAsHex(checksumInput.getBytes()));
  }

  private Checksum deletedProjectChecksum(Project project) {
    String checksumInput = String.format("%s:%s", "project-deleted", project.getUuid().getUuid());
    return new Checksum("project-deleted",
                        DigestUtils.md5DigestAsHex(checksumInput.getBytes()));
  }

  private Checksum validProjectChecksum(Project project) {
    String checksumInput = String.format("%s:%s:%s",
                                         "project-validated",
                                         project.getUuid().getUuid(),
                                         objectToString(project.getContent()));
    return new Checksum("project-validated",
                        DigestUtils.md5DigestAsHex(checksumInput.getBytes()));
  }

  private static String objectToString(Object object) {
    try {
      return new ObjectMapper().writeValueAsString(object);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static String objectToPrettyString(Object object) {
    try {
      return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String emailNotificationsFromAddress () {
    return environment.getProperty("PROJECT_NOTIFICATIONS_FROM_ADDRESS",
                                   "hca-notifications-test@ebi.ac.uk");
  }
}
