package org.humancellatlas.ingest.project;

import static org.springframework.util.DigestUtils.md5DigestAsHex;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProjectEventHandler {

  private final NotificationService notificationService;
  private final Environment environment;
  private final IdentityService identityService;

  private final Logger log = LoggerFactory.getLogger(getClass());

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

  public Notification registeredProject(Project project) {
    String message = "A new project [" + project.getUuid() + "] was registered.";
    String header = "project-registered";
    Checksum checksum =
        new Checksum(header, md5DigestAsHex((header + ":" + project.getUuid()).getBytes()));
    return notifyWranglersByEmail(message, checksum);
  }

  public Notification editedProjectMetadata(Project project) {
    String notificationContent =
        String.format(
            "Project %s was updated:\n\nNew content:\n\n%s",
            project.getUuid().getUuid().toString(), objectToPrettyString(project.getContent()));
    Checksum checksum = editedProjectChecksum(project);

    return notifyWranglersByEmail(notificationContent, checksum);
  }

  public Notification deletedProject(Project project) {
    String notificationContent =
        String.format(
            "Project %s was deleted:\n\n%s",
            project.getUuid().getUuid().toString(), objectToPrettyString(project.getContent()));

    Checksum checksum = deletedProjectChecksum(project);

    return notifyWranglersByEmail(notificationContent, checksum);
  }

  public Optional<Notification> validatedProject(Project project) {
    if (project.getValidationState().equals(ValidationState.VALID)) {
      String notificationContent =
          String.format(
              "Project %s has been validated:\n\n%s",
              project.getUuid().getUuid().toString(), objectToPrettyString(project.getContent()));

      Checksum checksum = validProjectChecksum(project);

      return Optional.of(notifyWranglersByEmail(notificationContent, checksum));
    } else {
      return Optional.empty();
    }
  }

  private Notification notifyWranglersByEmail(
      String notificationContent, Checksum notificationChecksum) {
    var notificationMetadata = new HashMap<String, Object>();
    var emailMetadata = new HashMap<String, String>();
    emailMetadata.put("to", this.emailNotificationsFromAddress());
    emailMetadata.put("from", identityService.wranglerEmail());
    emailMetadata.put("subject", "HCA DCP project update");
    emailMetadata.put("body", notificationContent);
    notificationMetadata.put("email", emailMetadata);

    NotificationRequest notificationRequest =
        new NotificationRequest(notificationContent, notificationMetadata, notificationChecksum);
    try {
      return this.notificationService.createNotification(notificationRequest);
    } catch (DuplicateNotification e) {
      return this.notificationService
          .retrieveForChecksum(notificationChecksum)
          .orElseThrow(
              () -> {
                log.error("Duplicate notification for non-existent checksum");
                throw new RuntimeException(e);
              });
    }
  }

  private Checksum editedProjectChecksum(Project project) {
    String checksumInput = String.format("%s:%s", "project-edited", project.getUuid().getUuid());
    return new Checksum("project-edited", md5DigestAsHex(checksumInput.getBytes()));
  }

  private Checksum deletedProjectChecksum(Project project) {
    String checksumInput = String.format("%s:%s", "project-deleted", project.getUuid().getUuid());
    return new Checksum("project-deleted", md5DigestAsHex(checksumInput.getBytes()));
  }

  private Checksum validProjectChecksum(Project project) {
    String checksumInput =
        String.format(
            "%s:%s:%s",
            "project-validated", project.getUuid().getUuid(), objectToString(project.getContent()));
    return new Checksum("project-validated", md5DigestAsHex(checksumInput.getBytes()));
  }

  private String emailNotificationsFromAddress() {
    return environment.getProperty(
        "PROJECT_NOTIFICATIONS_FROM_ADDRESS", "hca-notifications-test@ebi.ac.uk");
  }
}
