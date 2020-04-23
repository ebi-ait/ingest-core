package org.humancellatlas.ingest.notifications;

import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.hateoas.Identifiable;

@Data
@Builder
public class Notification implements Identifiable<String> {

  @Id
  private String id;
  @NonNull
  private final String content;
  @NonNull
  private final Map metadata;
  @NonNull
  private final Instant notifyAt;
  @NonNull
  private final NotificationState state;
}
