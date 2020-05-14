package org.humancellatlas.ingest.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.notifications.NotificationService;
import org.humancellatlas.ingest.notifications.exception.DuplicateNotification;
import org.humancellatlas.ingest.notifications.model.Checksum;
import org.humancellatlas.ingest.notifications.model.Notification;
import org.humancellatlas.ingest.notifications.model.NotificationRequest;
import org.humancellatlas.ingest.user.IdentityService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

@Component
@RequiredArgsConstructor
public class ProjectNotifications {
  private final NotificationService notificationService;
  private final Environment environment;
  private final IdentityService identityService;

  public Notification editedProjectMetadata(Project project) {
    String notificationContent = String.format("Project %s was updated:\n\nNew content:\n\n%s",
                                               project.getUuid().getUuid().toString(),
                                               objectToString(project.getContent()));
    Checksum checksum = editedProjectChecksum(project);

    return notifyWranglersByEmail(notificationContent, checksum);
  }

  public Notification deletedProject(Project project) {
    String notificationContent = String.format("Project %s was deleted:\n\n%s",
                                               project.getUuid().getUuid().toString(),
                                               objectToString(project.getContent()));

    Checksum checksum = deletedProjectChecksum(project);

    return notifyWranglersByEmail(notificationContent, checksum);
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
      return this.notificationService.retrieveForChecksum(notificationChecksum);
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

  private static String objectToString(Object object) {
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
