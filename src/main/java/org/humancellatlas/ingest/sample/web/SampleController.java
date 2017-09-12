package org.humancellatlas.ingest.sample.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.sample.Sample;
import org.humancellatlas.ingest.sample.SampleService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.state.SubmissionEnvelopeStateEngine;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
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
    private final @NonNull SubmissionEnvelopeStateEngine submissionEnvelopeStateEngine;

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/samples",
                    method = RequestMethod.POST,
                    produces = MediaTypes.HAL_JSON_VALUE)
    ResponseEntity<Resource<?>> addSampleToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                                    @RequestBody Sample sample,
                                                    final PersistentEntityResourceAssembler assembler) {
        Sample entity = getSampleService().addSampleToSubmissionEnvelope(submissionEnvelope, sample);
        PersistentEntityResource resource = assembler.toFullResource(entity);
        return ResponseEntity.accepted().body(resource);
    }
}
