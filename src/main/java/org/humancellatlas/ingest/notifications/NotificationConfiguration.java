package org.humancellatlas.ingest.notifications;

import java.util.Collection;
import java.util.Collections;
import lombok.Getter;
import org.humancellatlas.ingest.notifications.processors.NotificationProcessor;
import org.humancellatlas.ingest.notifications.processors.impl.email.EmailNotificationProcessor;
import org.humancellatlas.ingest.notifications.processors.impl.email.SMTPConfig;
import org.humancellatlas.ingest.notifications.sources.impl.rabbit.AmqpConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;


@Component
@Getter
class SmtpEnvVars {
  private final String smtpHost;
  private final int smtpPort;
  private final String username;
  private final String password;


  @Autowired
  public SmtpEnvVars(Environment environment) {
    this.smtpHost = environment.getProperty("NOTIFICATIONS_SMTP_HOST");
    this.smtpPort = Integer.parseInt(environment.getProperty("NOTIFICATIONS_SMTP_PORT"));
    this.username = environment.getProperty("NOTIFICATIONS_SMTP_USERNAME");
    this.password = environment.getProperty("NOTIFICATIONS_SMTP_PASSWORD");
  }
}

@Component("amqpEnvVars")
@Getter
class AmqpEnvVars {
  private final String sendExchange;
  private final String sendRoutingKey;

  @Autowired
  public AmqpEnvVars(Environment environment) {
    this.sendExchange = environment.getProperty("NOTIFICATIONS_AMQP_SEND_EXCHANGE");
    this.sendRoutingKey = environment.getProperty("NOTIFICATIONS_AMQP_SEND_ROUTING_KEY");
  }
}

@Configuration
public class NotificationConfiguration {

  @Bean
  public SMTPConfig smtpConfig(SmtpEnvVars smtpEnvVars) {
    return SMTPConfig.builder()
                     .host(smtpEnvVars.getSmtpHost())
                     .port(smtpEnvVars.getSmtpPort())
                     .username(smtpEnvVars.getUsername())
                     .password(smtpEnvVars.getPassword())
                     .build();
  }

  @Bean
  public AmqpConfig amqpConfig(AmqpEnvVars amqpEnvVars) {
    return AmqpConfig.builder()
                     .sendExchange(amqpEnvVars.getSendExchange())
                     .sendRoutingKey(amqpEnvVars.getSendRoutingKey())
                     .build();
  }

  @Bean
  public Collection<NotificationProcessor> notificationProcessors(SMTPConfig smtpConfig) {
    EmailNotificationProcessor emailNotificationProcessor = new EmailNotificationProcessor(smtpConfig);
    return Collections.singletonList(emailNotificationProcessor);
  }
}
