package org.humancellatlas.ingest.submission.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.envelope.SubmissionEnvelope;
import org.humancellatlas.ingest.messaging.Constants;
import org.humancellatlas.ingest.submission.SubmissionReceipt;
import org.humancellatlas.ingest.submission.SubmissionService;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Spring controller that will handle submission events on a {@link org.humancellatlas.ingest.envelope.SubmissionEnvelope}
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
@Controller
@ExposesResourceFor(SubmissionEnvelope.class)
@RequestMapping("/submissionEnvelopes/{id}")
@RequiredArgsConstructor
public class SubmissionController {
    private final @NonNull SubmissionService submissionService;

    @Autowired
    private AmqpTemplate messagingTemplate;

    @RequestMapping(path = "/submit", method = RequestMethod.PUT)
    HttpEntity<?> submitEnvelope(@PathVariable("id") SubmissionEnvelope submissionEnvelope) {
        SubmissionReceipt receipt = submissionService.submitEnvelope(submissionEnvelope);

        // post event to queue
        messagingTemplate.convertAndSend(Constants.Exchanges.SUBMISSION_FANOUT,"", submissionEnvelope);

        return ResponseEntity.accepted().body(receipt);
    }
}
