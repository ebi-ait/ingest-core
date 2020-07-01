package org.humancellatlas.ingest.notifications;

import java.util.Collection;
import java.util.Collections;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.humancellatlas.ingest.notifications.NotificationConfiguration.NotificationProperties;
import org.humancellatlas.ingest.notifications.NotificationConfiguration.NotificationProperties.AmqpProperties;
import org.humancellatlas.ingest.notifications.NotificationConfiguration.NotificationProperties.SmtpProperties;
import org.humancellatlas.ingest.notifications.processors.NotificationProcessor;
import org.humancellatlas.ingest.notifications.processors.impl.email.EmailNotificationProcessor;
import org.humancellatlas.ingest.notifications.processors.impl.email.SMTPConfig;
import org.humancellatlas.ingest.notifications.sources.NotificationSource;
import org.humancellatlas.ingest.notifications.sources.impl.rabbit.AmqpConfig;
import org.humancellatlas.ingest.notifications.sources.impl.rabbit.RabbitNotificationSource;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({NotificationProperties.class, SmtpProperties.class, AmqpProperties.class})
public class NotificationConfiguration {

    @Bean
    public SMTPConfig smtpConfig(SmtpProperties smtpEnvVars) {
        return SMTPConfig.builder()
                         .host(smtpEnvVars.getHost())
                         .port(Integer.parseInt(smtpEnvVars.getPort()))
                         .username(smtpEnvVars.getUsername())
                         .password(smtpEnvVars.getPassword())
                         .build();
    }

    @Bean
    public AmqpConfig amqpConfig(AmqpProperties amqpEnvVars) {
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

    @Bean
    public NotificationSource notificationSource(RabbitMessagingTemplate rabbitMessagingTemplate,
                                                 AmqpConfig amqpConfig) {
        return new RabbitNotificationSource(rabbitMessagingTemplate, amqpConfig);
    }

    @ConfigurationProperties(prefix = "notifications")
    class NotificationProperties {

        @ConfigurationProperties(prefix = "notifications.smtp")
        @NoArgsConstructor
        @Getter
        @Setter
        class SmtpProperties {

            private String host = "localhost";
            private String port = "587";
            private String username = "provide username";
            private String password = "provide password";

        }

        @ConfigurationProperties(prefix = "notifications.amqp")
        @NoArgsConstructor
        @Getter
        @Setter
        class AmqpProperties {

            private String sendExchange = "provide notifications send exchange";
            private String sendRoutingKey = "provide notifications routing key";
        }
    }
}