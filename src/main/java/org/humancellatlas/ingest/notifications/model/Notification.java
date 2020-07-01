package org.humancellatlas.ingest.notifications.model;

import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.Identifiable;

@Data
@Builder
@Document
public class Notification implements Identifiable<String> {

  @Id
  private String id;
  @NonNull
  private final String content;
  @NonNull
  private final Map<String, ?> metadata;
  @NonNull
  private final Instant notifyAt;
  @NonNull @Indexed
  private NotificationState state;
  @NonNull @Indexed
  private final Checksum checksum;

  public static NotificationBuilder buildNew() {
    return Notification.builder()
                       .state(NotificationState.PENDING)
                       .notifyAt(Instant.now());
  }
}
