package org.humancellatlas.ingest.notifications;

import java.util.Map;
import lombok.Data;

@Data
public class NotificationRequest {
  private final String content;
  private final Map metadata;
}
