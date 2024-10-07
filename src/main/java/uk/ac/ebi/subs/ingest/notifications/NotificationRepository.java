package uk.ac.ebi.subs.ingest.notifications;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import uk.ac.ebi.subs.ingest.notifications.model.Checksum;
import uk.ac.ebi.subs.ingest.notifications.model.Notification;
import uk.ac.ebi.subs.ingest.notifications.model.NotificationState;

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
