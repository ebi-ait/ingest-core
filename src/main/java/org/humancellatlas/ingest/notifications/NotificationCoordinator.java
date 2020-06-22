package org.humancellatlas.ingest.notifications;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.notifications.exception.ProcessingException;
import org.humancellatlas.ingest.notifications.model.Notification;
import org.humancellatlas.ingest.notifications.model.NotificationState;
import org.humancellatlas.ingest.notifications.processors.NotificationProcessor;
import org.humancellatlas.ingest.notifications.sources.NotificationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class NotificationCoordinator {

    private final @NonNull Collection<NotificationProcessor> notificationProcessors;
    private final @NonNull NotificationSource notificationSource;
    private final @NonNull NotificationService notificationService;

    private final Logger log = LoggerFactory.getLogger(getClass());

    public void queue() {
        this.notificationService.getUnhandledNotifications()
                                .forEach(notification -> {
                                    this.notificationService
                                        .changeState(notification, NotificationState.QUEUED);
                                    this.notificationSource
                                        .supply(Collections.singletonList(notification));
                                });
    }

    public void process() {
        this.notificationSource
            .stream()
            .forEach(notification -> {
                Notification processingNotification = this.notificationService
                    .changeState(notification, NotificationState.PROCESSING);

                this.processNotification(processingNotification)
                    .filter(report -> !report.isSuccessful())
                    .findAny()
                    .ifPresentOrElse(failedReport -> this.notificationService.changeState(processingNotification, NotificationState.FAILED),
                                     () -> this.notificationService.changeState(processingNotification, NotificationState.PROCESSED));
            });
    }

    public void cleanup() {
        this.notificationService.getHandledNotifications()
                                .forEach(notificationService::deleteNotification);
    }

    private Stream<NotificationProcessReport> processNotification(Notification notification) {
        Stream<NotificationProcessor> processers = this.notificationProcessors.stream();

        return processers.filter(notificationProcessor -> notificationProcessor.isEligible(notification))
                         .map(notificationProcessor -> {
                             try {
                                 notificationProcessor.handle(notification);
                                 return NotificationProcessReport.successReport(notification);
                             } catch (ProcessingException e) {
                                 log.warn(String.format("Notification processor failed for %s on notification with ID %s", notificationProcessor.getClass(),
                                                         notification.getId()),
                                          e);
                                 return NotificationProcessReport.failureReport(notification);
                             }
                         });
    }

    @Scheduled(fixedDelay = 20000)
    private void scheduledQueue() {
        this.queue();
    }

    @Scheduled(fixedDelay = 60000)
    private void scheduledProcess() {
        this.process();
    }

    @Scheduled(fixedDelay = 300000)
    private void scheduledCleanup() {
        this.cleanup();
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class NotificationProcessReport {

        private final Notification notification;
        private final NotificationState result;

        public static NotificationProcessReport successReport(Notification notification) {
            return new NotificationProcessReport(notification, NotificationState.PROCESSED);
        }

        public static NotificationProcessReport failureReport(Notification notification) {
            return new NotificationProcessReport(notification, NotificationState.FAILED);
        }

        public boolean isSuccessful() {
            return this.result.equals(NotificationState.PROCESSED);
        }
    }
}