package uk.ac.ebi.subs.ingest.notifications.processors.impl.email;

import java.util.Optional;
import java.util.Properties;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.ebi.subs.ingest.notifications.exception.ProcessingException;
import uk.ac.ebi.subs.ingest.notifications.model.Notification;
import uk.ac.ebi.subs.ingest.notifications.processors.NotificationProcessor;

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
    try {
      SimpleMailMessage message = messageFrom(notification);
      mailSender.send(message);
    } catch (RuntimeException e) {
      throw new ProcessingException(e);
    }
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
        .orElseThrow(
            () -> {
              throw new ProcessingException(
                  String.format("Email metadata empty for notification %s", notification.getId()));
            });
  }

  private static MailSender createMailClient(SMTPConfig smtpConfig) {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

    mailSender.setHost(smtpConfig.getHost());
    mailSender.setPort(smtpConfig.getPort());
    mailSender.setUsername(smtpConfig.getUsername());
    mailSender.setPassword(smtpConfig.getPassword());

    Properties props = new Properties();
    props.setProperty("mail.transport.protocol", "smtp");
    props.setProperty("mail.host", "outgoing.ebi.ac.uk");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.port", "587");
    props.put("mail.smtp.starttls.enable", "true"); // TLS

    mailSender.setJavaMailProperties(props);

    return mailSender;
  }
}
