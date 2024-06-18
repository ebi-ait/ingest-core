package org.humancellatlas.ingest.notifications.web;

import org.humancellatlas.ingest.notifications.model.Notification;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;

import lombok.RequiredArgsConstructor;

@RepositoryRestController
@ExposesResourceFor(Notification.class)
@RequiredArgsConstructor
public class NotificationController {}
