package org.humancellatlas.ingest.notifications;

import java.util.Optional;
import java.util.stream.Stream;

import org.humancellatlas.ingest.notifications.model.Checksum;
import org.humancellatlas.ingest.notifications.model.Notification;
import org.humancellatlas.ingest.notifications.model.NotificationState;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RestResource;

public interface NotificationRepository extends MongoRepository<Notification, String> {

  @RestResource(exported = false)
  <S extends Notification> S save(S notification);

  @RestResource(exported = false)
  void delete(Notification notification);

  @RestResource(exported = false)
  Stream<Notification> findByStateOrderByNotifyAtDesc(NotificationState state);

  @RestResource(rel = "findByChecksumValue")
  <S extends Notification> Optional<S> findByChecksum_Value(String checksumValue);

  @RestResource(exported = false)
  <S extends Notification> Optional<S> findByChecksum(Checksum checksumValue);
}
