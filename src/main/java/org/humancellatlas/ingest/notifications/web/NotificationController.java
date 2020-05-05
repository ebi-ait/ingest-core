package org.humancellatlas.ingest.notifications.web;

import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.notifications.NotificationService;
import org.humancellatlas.ingest.notifications.model.Notification;
import org.humancellatlas.ingest.notifications.model.NotificationRequest;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RepositoryRestController
@ExposesResourceFor(Notification.class)
@RequiredArgsConstructor
public class NotificationController {
  private final NotificationService notificationService;

  @RequestMapping(path = "notifications", method = RequestMethod.POST)
  ResponseEntity<?> createNotification(@RequestBody NotificationRequest notificationRequest,
                                       PersistentEntityResourceAssembler assembler) {
    Notification createdNotification = this.notificationService.createNotification(notificationRequest);
    return ResponseEntity.ok(assembler.toFullResource(createdNotification));
  }
}
