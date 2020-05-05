package org.humancellatlas.ingest.notifications.model;

import java.util.Map;
import lombok.Value;

@Value
public class NotificationRequest {
  private final String content;
  private final Map<String, ?> metadata;
  private final Checksum checksum;
}
