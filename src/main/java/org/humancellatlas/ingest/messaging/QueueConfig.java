package org.humancellatlas.ingest.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Simon Jupp
 * @date 04/09/2017 Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Configuration
public class QueueConfig {
    @Bean MessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean Queue queueFileUpdate() { return new Queue(Constants.Queues.FILE_UPDATE, false); }

    @Bean FanoutExchange fileExchange() { return new FanoutExchange(Constants.Exchanges.FILE_FANOUT); }

    @Bean Queue queueFileStaged() { return new Queue(Constants.Queues.FILE_STAGED, false); }

    @Bean FanoutExchange fileStagedExchange() { return new FanoutExchange(Constants.Exchanges.FILE_STAGED_FANOUT); }

    @Bean Queue queueEnvelopeSubmitted() { return new Queue(Constants.Queues.ENVELOPE_SUBMITTED, false); }

    @Bean FanoutExchange envelopeExchange() { return new FanoutExchange(Constants.Exchanges.ENVELOPE_FANOUT); }

    @Bean Queue queueValidationRequired() { return new Queue(Constants.Queues.VALIDATION_REQUIRED, false); }

    @Bean FanoutExchange validationExchange() { return new FanoutExchange(Constants.Exchanges.VALIDATION_FANOUT); }

    @Bean Queue queueAccessionRequired() { return new Queue(Constants.Queues.ACCESSION_REQUIRED, false); }

    @Bean FanoutExchange accessionExchange() { return new FanoutExchange(Constants.Exchanges.ACCESSION_FANOUT); }

    /* bindings */

    @Bean Binding bindingFileStaged(Queue queueFileStaged, FanoutExchange fileStagedExchange) {
        return BindingBuilder.bind(queueFileStaged).to(fileStagedExchange);
    }

    @Bean Binding bindingFile(Queue queueFileUpdate, FanoutExchange fileExchange) {
        return BindingBuilder.bind(queueFileUpdate).to(fileExchange);
    }

    @Bean Binding bindingSubmission(Queue queueEnvelopeSubmitted,
                                    FanoutExchange envelopeExchange) {
        return BindingBuilder.bind(queueEnvelopeSubmitted)
                .to(envelopeExchange);
    }

    @Bean Binding bindingValidation(Queue queueValidationRequired, FanoutExchange validationExchange) {
        return BindingBuilder.bind(queueValidationRequired).to(validationExchange);
    }

    @Bean Binding bindingAccession(Queue queueAccessionRequired, FanoutExchange accessionExchange) {
        return BindingBuilder.bind(queueAccessionRequired).to(accessionExchange);
    }

}
