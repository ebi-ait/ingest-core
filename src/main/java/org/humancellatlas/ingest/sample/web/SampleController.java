package org.humancellatlas.ingest.sample.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.envelope.SubmissionEnvelope;
import org.humancellatlas.ingest.sample.Sample;
import org.humancellatlas.ingest.sample.SampleService;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 05/09/17
 */
@RepositoryRestController
@ExposesResourceFor(Sample.class)
@RequiredArgsConstructor
@Getter
public class SampleController {
    private final @NonNull SampleService sampleService;

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/samples", method = RequestMethod.POST)
    HttpEntity<Sample> addSampleToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                           @RequestBody Sample sample) {
        Sample entity = getSampleService().addSampleToSubmissionEnvelope(submissionEnvelope, sample);
        return ResponseEntity.accepted().body(entity);
    }
}
