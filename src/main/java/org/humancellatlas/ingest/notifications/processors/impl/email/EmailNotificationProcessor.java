package org.humancellatlas.ingest.notifications.processors.impl.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.humancellatlas.ingest.notifications.model.Notification;
import org.humancellatlas.ingest.notifications.processors.NotificationProcessor;
import org.humancellatlas.ingest.notifications.processors.ProcessingException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

public class EmailNotificationProcessor implements NotificationProcessor {
  private final SMTPConfig smtpConfig;
  private final MailSender mailSender;

  public EmailNotificationProcessor(SMTPConfig smtpConfig) {
    this.smtpConfig = smtpConfig;
    this.mailSender = createMailClient(this.smtpConfig);
  }

  @Override
  public boolean isEligible(Notification notification) {
    return Optional.ofNullable(notification.getMetadata())
                   .map(metadata -> metadata.containsKey("email"))
                   .orElse(false);
  }

  @Override
  public void handle(Notification notification) {
    SimpleMailMessage message = messageFrom(notification);
    mailSender.send(message);
  }

  private static SimpleMailMessage messageFrom(Notification notification) {
    EmailMetadata emailMetadata = parseEmailMetadata(notification);

    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(emailMetadata.getTo());
    message.setFrom(emailMetadata.getFrom());
    message.setText(emailMetadata.getBody());
    message.setSubject(emailMetadata.getSubject());

    return message;
  }

  private static EmailMetadata parseEmailMetadata(Notification notification) {
    return Optional.ofNullable(notification.getMetadata())
                   .map(metadata -> metadata.get("email"))
                   .map(emailMetadata -> new ObjectMapper().convertValue(emailMetadata, EmailMetadata.class))
                   .orElseThrow(() -> {
                     throw new ProcessingException(String.format("Email metadata empty for notification %s", notification.getId()));
                   });
  }

  private static MailSender createMailClient(SMTPConfig smtpConfig) {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

    mailSender.setHost(smtpConfig.getHost());
    mailSender.setPort(smtpConfig.getPort());
    mailSender.setUsername(smtpConfig.getUsername());
    mailSender.setPassword(smtpConfig.getPassword());

    return mailSender;
  }
}
