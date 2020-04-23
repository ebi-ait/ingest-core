package org.humancellatlas.ingest.notifications;

import java.util.stream.Stream;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationRepository extends MongoRepository<Notification, String> {

  Stream<Notification> findByStateOrderByNotifyAtDesc(NotificationState state);
}
