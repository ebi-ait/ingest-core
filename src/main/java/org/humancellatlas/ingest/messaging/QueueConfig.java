package org.humancellatlas.ingest.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.humancellatlas.ingest.messaging.Constants.Exchanges;
import org.humancellatlas.ingest.messaging.Constants.Queues;
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

/**
 * @author Simon Jupp
 * @date 04/09/2017 Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Configuration
public class QueueConfig implements RabbitListenerConfigurer {
    @Bean Queue queueFileUpdate() { return new Queue(Constants.Queues.FILE_UPDATE, false); }

    @Bean Queue queueBundleUpdate() { return new Queue(Constants.Routing.UPDATE_SUBMITTED, true); }

    @Bean FanoutExchange fileExchange() { return new FanoutExchange(Constants.Exchanges.FILE_FANOUT); }

    @Bean Queue queueFileStaged() { return new Queue(Constants.Queues.FILE_STAGED, false); }

    @Bean FanoutExchange fileStagedExchange() { return new FanoutExchange(Constants.Exchanges.FILE_STAGED_EXCHANGE); }

    @Bean Queue queueValidationRequired() { return new Queue(Constants.Queues.VALIDATION_REQUIRED, false); }

    @Bean DirectExchange validationExchange() { return new DirectExchange(Constants.Exchanges.VALIDATION); }

    @Bean Queue queueAccessionRequired() { return new Queue(Constants.Queues.ACCESSION_REQUIRED, false); }

    @Bean DirectExchange accessionExchange() { return new DirectExchange(Constants.Exchanges.ACCESSION); }

    @Bean Queue queueArchival() { return new Queue(Constants.Queues.SUBMISSION_ARCHIVAL, false); }

    @Bean DirectExchange archivalExchange() { return new DirectExchange(Constants.Exchanges.SUBMISSION_ARCHIVAL_DIRECT); }

    @Bean Queue queueStateTracking() { return new Queue(Constants.Queues.STATE_TRACKING, false); }

    @Bean TopicExchange stateTrackingExchange() { return new TopicExchange(Constants.Exchanges.STATE_TRACKING); }

    @Bean Queue queueNotifications() { return new Queue(Queues.NOTIFICATIONS_QUEUE, true); }

    @Bean FanoutExchange notificationExchange() { return new FanoutExchange(Exchanges.NOTIFICATIONS_EXCHANGE); }

    @Bean TopicExchange assayExchange() { return new TopicExchange(Constants.Exchanges.ASSAY_EXCHANGE); }

    @Bean TopicExchange uploadAreaExchange() { return new TopicExchange(Constants.Exchanges.UPLOAD_AREA_EXCHANGE); }


    /* bindings */

    @Bean Binding bindingFileStaged(Queue queueFileStaged, FanoutExchange fileStagedExchange) {
        return BindingBuilder.bind(queueFileStaged).to(fileStagedExchange);
    }

    @Bean Binding bindingFile(Queue queueFileUpdate, FanoutExchange fileExchange) {
        return BindingBuilder.bind(queueFileUpdate).to(fileExchange);
    }

    @Bean Binding bindingValidation(Queue queueValidationRequired, DirectExchange validationExchange) {
        return BindingBuilder.bind(queueValidationRequired).to(validationExchange).with(Constants.Queues.VALIDATION_REQUIRED);
    }

    @Bean Binding bindingAccession(Queue queueAccessionRequired, DirectExchange accessionExchange) {
        return BindingBuilder.bind(queueAccessionRequired).to(accessionExchange).with(Constants.Queues.ACCESSION_REQUIRED);
    }

    @Bean Binding bindingArchival(Queue queueArchival, DirectExchange archivalExchange) {
        return BindingBuilder.bind(queueArchival).to(archivalExchange).with(Constants.Queues.SUBMISSION_ARCHIVAL);
    }

    @Bean Binding bindingStateTracking(Queue queueStateTracking, TopicExchange stateTrackingExchange) {
        return BindingBuilder.bind(queueStateTracking).to(stateTrackingExchange).with(Constants.Queues.STATE_TRACKING);
    }

    @Bean Binding bindingUpdateQueue(Queue queueBundleUpdate, TopicExchange assayExchange) {
        return BindingBuilder.bind(queueBundleUpdate).to(assayExchange).with(Constants.Routing.UPDATE_SUBMITTED);
    }

    @Bean Binding bindingNotificationQueue(Queue queueNotifications, FanoutExchange notificationExchange) {
        return BindingBuilder.bind(queueNotifications).to(notificationExchange);
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
