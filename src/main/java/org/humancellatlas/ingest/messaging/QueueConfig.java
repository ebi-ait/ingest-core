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
 * @date 04/09/2017
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Configuration
public class QueueConfig {
    @Bean
    MessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean Queue queueFileUpdate() { return new Queue(Constants.Queues.FILE_UPDATE, false); }
    @Bean FanoutExchange fileExchange() { return new FanoutExchange(Constants.Exchanges.FILE_FANOUT); }
    @Bean Queue queueSubmissionUpdate() { return new Queue(Constants.Queues.SUBMISSION_UPDATE, false); }
    @Bean FanoutExchange submissionExchange() { return new FanoutExchange(Constants.Exchanges.SUBMISSION_FANOUT); }
    @Bean Binding bindingFile(Queue queueFileUpdate, FanoutExchange fileExchange) { return BindingBuilder.bind(queueFileUpdate).to(fileExchange); }
    @Bean Binding bindingSubmission(Queue queueSubmissionUpdate, FanoutExchange submissionExchange) { return BindingBuilder.bind(queueSubmissionUpdate).to(submissionExchange); }
}
