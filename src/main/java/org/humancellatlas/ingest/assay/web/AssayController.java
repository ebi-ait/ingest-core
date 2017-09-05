package org.humancellatlas.ingest.assay.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.assay.Assay;
import org.humancellatlas.ingest.assay.AssayService;
import org.humancellatlas.ingest.envelope.SubmissionEnvelope;
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
@ExposesResourceFor(Assay.class)
@RequiredArgsConstructor
@Getter
public class AssayController {
    private final @NonNull AssayService assayService;

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/assays", method = RequestMethod.POST)
    HttpEntity<Assay> addSampleToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                          @RequestBody Assay assay) {
        Assay entity = getAssayService().addAssayToSubmissionEnvelope(submissionEnvelope, assay);
        return ResponseEntity.accepted().body(entity);
    }

}
