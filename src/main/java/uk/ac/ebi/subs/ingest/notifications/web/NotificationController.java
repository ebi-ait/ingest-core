package uk.ac.ebi.subs.ingest.notifications.web;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;

import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.notifications.model.Notification;

@RepositoryRestController
@ExposesResourceFor(Notification.class)
@RequiredArgsConstructor
public class NotificationController {}
