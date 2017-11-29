package org.humancellatlas.ingest.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

    @Bean FanoutExchange fileExchange() { return new FanoutExchange(Constants.Exchanges.FILE_FANOUT); }

    @Bean Queue queueFileStaged() { return new Queue(Constants.Queues.FILE_STAGED, false); }

    @Bean FanoutExchange fileStagedExchange() { return new FanoutExchange(Constants.Exchanges.FILE_STAGED_FANOUT); }

    @Bean Queue queueEnvelopeCreated() { return new Queue(Constants.Queues.ENVELOPE_CREATED, false); }

    @Bean FanoutExchange envelopeCreatedExchange() { return new FanoutExchange(Constants.Exchanges.ENVELOPE_CREATED_FANOUT); }

    @Bean Queue queueEnvelopeSubmitted() { return new Queue(Constants.Queues.ENVELOPE_SUBMITTED, false); }

    @Bean FanoutExchange envelopeSubmittedExchange() { return new FanoutExchange(Constants.Exchanges.ENVELOPE_SUBMITTED_FANOUT); }

    @Bean Queue queueValidationRequired() { return new Queue(Constants.Queues.VALIDATION_REQUIRED, false); }

    @Bean DirectExchange validationExchange() { return new DirectExchange(Constants.Exchanges.VALIDATION); }

    @Bean Queue queueAccessionRequired() { return new Queue(Constants.Queues.ACCESSION_REQUIRED, false); }

    @Bean DirectExchange accessionExchange() { return new DirectExchange(Constants.Exchanges.ACCESSION); }

    @Bean Queue queueArchival() { return new Queue(Constants.Queues.SUBMISSION_ARCHIVAL, false); }

    @Bean DirectExchange archivalExchange() { return new DirectExchange(Constants.Exchanges.SUBMISSION_ARCHIVAL_DIRECT); }
    /* bindings */

    @Bean Binding bindingFileStaged(Queue queueFileStaged, FanoutExchange fileStagedExchange) {
        return BindingBuilder.bind(queueFileStaged).to(fileStagedExchange);
    }

    @Bean Binding bindingFile(Queue queueFileUpdate, FanoutExchange fileExchange) {
        return BindingBuilder.bind(queueFileUpdate).to(fileExchange);
    }

    @Bean Binding bindingCreation(Queue queueEnvelopeCreated,
                                  FanoutExchange envelopeCreatedExchange) {
        return BindingBuilder.bind(queueEnvelopeCreated).to(envelopeCreatedExchange);
    }

    @Bean Binding bindingSubmission(Queue queueEnvelopeSubmitted,
                                    FanoutExchange envelopeSubmittedExchange) {
        return BindingBuilder.bind(queueEnvelopeSubmitted).to(envelopeSubmittedExchange);
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
