package org.humancellatlas.ingest.sample.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.Event;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.sample.Sample;
import org.humancellatlas.ingest.sample.SampleService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
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

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/samples/{id}", method = RequestMethod.PUT)
    ResponseEntity<Resource<?>> linkAnalysisToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                                       @PathVariable("id") Sample sample,
                                                       final PersistentEntityResourceAssembler assembler) {
        Sample entity = getSampleService().addSampleToSubmissionEnvelope(submissionEnvelope, sample);
        PersistentEntityResource resource = assembler.toFullResource(entity);
        return ResponseEntity.accepted().body(resource);
    }

    @RequestMapping(path = "/samples/{id}" + Links.VALIDATING_URL, method = RequestMethod.PUT)
    HttpEntity<?> validatingSample(@PathVariable("id") Sample sample, final PersistentEntityResourceAssembler assembler) {
        sample.setValidationState(ValidationState.VALIDATING);
        sample = getSampleService().getSampleRepository().save(sample);
        return ResponseEntity.accepted().body(assembler.toFullResource(sample));
    }

    @RequestMapping(path = "/samples/{id}" + Links.VALID_URL, method = RequestMethod.PUT)
    HttpEntity<?> validateSample(@PathVariable("id") Sample sample, final PersistentEntityResourceAssembler assembler) {
        sample.setValidationState(ValidationState.VALID);
        sample = getSampleService().getSampleRepository().save(sample);
        return ResponseEntity.accepted().body(assembler.toFullResource(sample));
    }

    @RequestMapping(path = "/samples/{id}" + Links.INVALID_URL, method = RequestMethod.PUT)
    HttpEntity<?> invalidateSample(@PathVariable("id") Sample sample, final PersistentEntityResourceAssembler assembler) {
        sample.setValidationState(ValidationState.INVALID);
        sample = getSampleService().getSampleRepository().save(sample);
        return ResponseEntity.accepted().body(assembler.toFullResource(sample));
    }

    @RequestMapping(path = "/samples/{id}" + Links.PROCESSING_URL, method = RequestMethod.PUT)
    HttpEntity<?> processingSample(@PathVariable("id") Sample sample, final PersistentEntityResourceAssembler assembler) {
        sample.setValidationState(ValidationState.PROCESSING);
        sample = getSampleService().getSampleRepository().save(sample);
        return ResponseEntity.accepted().body(assembler.toFullResource(sample));
    }

    @RequestMapping(path = "/samples/{id}" + Links.COMPLETE_URL, method = RequestMethod.PUT)
    HttpEntity<?> completeSample(@PathVariable("id") Sample sample, final PersistentEntityResourceAssembler assembler) {
        sample.setValidationState(ValidationState.COMPLETE);
        sample = getSampleService().getSampleRepository().save(sample);
        return ResponseEntity.accepted().body(assembler.toFullResource(sample));
    }
}
