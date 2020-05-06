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
import org.springframework.util.DigestUtils;

@RequiredArgsConstructor
public class ProjectNotifications {
  private final NotificationService notificationService;

  public Notification editedProjectMetadata(Project project) {
      String notificationContent = String.format("Project %s was updated:\n\nNew content:\n\n%s",
                                                 project.getUuid().getUuid().toString(),
                                                 objectToString(project.getContent()));
      var notificationMetadata = new HashMap<String, Object>();
      var emailMetadata = new HashMap<String, String>();
      emailMetadata.put("to", "");
      emailMetadata.put("from", "");
      emailMetadata.put("subject", "HCA DCP project update");
      emailMetadata.put("body", notificationContent);
      notificationMetadata.put("email", emailMetadata);

      Checksum checksum = editedProjectChecksum(project);
      NotificationRequest notificationRequest = new NotificationRequest(notificationContent,
                                                                        notificationMetadata,
                                                                        checksum);

      try {
        return this.notificationService.createNotification(notificationRequest);
      } catch (DuplicateNotification e) {
        return this.notificationService.retrieveForChecksum(checksum);
      }
  }


  private Checksum editedProjectChecksum(Project project) {
    String checksumInput = String.format("%s:%s", "projectEdited", project.getUuid().getUuid());
    return new Checksum("projectEdited",
                        DigestUtils.md5DigestAsHex(checksumInput.getBytes()));
  }

  private static String objectToString(Object object) {
    try {
      return new ObjectMapper().writeValueAsString(object);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
