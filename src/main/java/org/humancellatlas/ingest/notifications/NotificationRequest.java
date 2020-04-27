package org.humancellatlas.ingest.notifications;

import java.util.Map;
import lombok.Data;
import org.humancellatlas.ingest.notifications.model.Checksum;

@Data
public class NotificationRequest {
  private final String content;
  private final Map metadata;
  private final Checksum checksum;
}
