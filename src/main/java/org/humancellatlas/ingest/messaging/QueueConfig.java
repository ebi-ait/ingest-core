package org.humancellatlas.ingest.messaging;

import org.humancellatlas.ingest.messaging.Constants.Exchanges;
import org.humancellatlas.ingest.messaging.Constants.Queues;
import org.humancellatlas.ingest.messaging.Constants.Routing;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class QueueConfig implements RabbitListenerConfigurer {
  @Bean
  Queue queueFileStaged() {
    return new Queue(Constants.Queues.FILE_STAGED_QUEUE, false);
  }

  @Bean
  FanoutExchange fileStagedExchange() {
    return new FanoutExchange(Constants.Exchanges.FILE_STAGED_EXCHANGE);
  }

  @Bean
  Queue queueMetadataValidation() {
    return new Queue(Constants.Queues.METADATA_VALIDATION_QUEUE, false);
  }

  @Bean
  DirectExchange validationExchange() {
    return new DirectExchange(Constants.Exchanges.VALIDATION_EXCHANGE);
  }

  @Bean
  Queue queueGraphValidation() {
    return new Queue(Constants.Queues.GRAPH_VALIDATION_QUEUE);
  }

  @Bean
  TopicExchange stateTrackingExchange() {
    return new TopicExchange(Constants.Exchanges.STATE_TRACKING_EXCHANGE);
  }

  @Bean
  Queue queueNotifications() {
    return new Queue(Queues.NOTIFICATIONS_QUEUE, true);
  }

  @Bean
  TopicExchange notificationExchange() {
    return new TopicExchange(Exchanges.NOTIFICATIONS_EXCHANGE);
  }

  @Bean
  TopicExchange exporterExchange() {
    return new TopicExchange(Constants.Exchanges.EXPORTER_EXCHANGE);
  }

  @Bean
  TopicExchange uploadAreaExchange() {
    return new TopicExchange(Constants.Exchanges.UPLOAD_AREA_EXCHANGE);
  }

  /* bindings */

  @Bean
  Binding bindingFileStaged(Queue queueFileStaged, FanoutExchange fileStagedExchange) {
    return BindingBuilder.bind(queueFileStaged).to(fileStagedExchange);
  }

  @Bean
  Binding bindingValidation(Queue queueMetadataValidation, DirectExchange validationExchange) {
    return BindingBuilder.bind(queueMetadataValidation)
        .to(validationExchange)
        .with(Constants.Queues.METADATA_VALIDATION_QUEUE);
  }

  @Bean
  Binding bindingGraphValidation(Queue queueGraphValidation, DirectExchange validationExchange) {
    return BindingBuilder.bind(queueGraphValidation)
        .to(validationExchange)
        .with(Constants.Queues.GRAPH_VALIDATION_QUEUE);
  }

  @Bean
  Binding bindingNewNotificationQueue(
      Queue queueNotifications, TopicExchange notificationExchange) {
    return BindingBuilder.bind(queueNotifications)
        .to(notificationExchange)
        .with(Routing.NOTIFICATION_NEW);
  }

  /* rabbit config */

  @Bean
  public MessageConverter messageConverter() {
    return jackson2Converter();
  }

  @Bean
  public MappingJackson2MessageConverter jackson2Converter() {
    ObjectMapper mapper = new ObjectMapper();

    mapper.registerModule(new JavaTimeModule());
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    return new MappingJackson2MessageConverter();
  }

  @Bean
  public DefaultMessageHandlerMethodFactory myHandlerMethodFactory() {
    DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
    factory.setMessageConverter(jackson2Converter());
    return factory;
  }

  @Bean
  public RabbitMessagingTemplate rabbitMessagingTemplate(RabbitTemplate rabbitTemplate) {
    RabbitMessagingTemplate rmt = new RabbitMessagingTemplate(rabbitTemplate);
    rmt.setMessageConverter(this.jackson2Converter());
    return rmt;
  }

  @Override
  public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
    registrar.setMessageHandlerMethodFactory(myHandlerMethodFactory());
  }
}
