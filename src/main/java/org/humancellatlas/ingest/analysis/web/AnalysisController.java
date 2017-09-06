package org.humancellatlas.ingest.analysis.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.analysis.Analysis;
import org.humancellatlas.ingest.analysis.AnalysisService;
import org.humancellatlas.ingest.envelope.SubmissionEnvelope;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;
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
@ExposesResourceFor(Analysis.class)
@RequiredArgsConstructor
@Getter
public class AnalysisController {
    private final @NonNull AnalysisService analysisService;

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/analyses", method = RequestMethod.POST)
    ResponseEntity<Resource<?>> addAnalysisToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                                      @RequestBody Analysis analysis,
                                                      final PersistentEntityResourceAssembler assembler) {
        Analysis entity = getAnalysisService().addAnalysisToSubmissionEnvelope(submissionEnvelope, analysis);
        PersistentEntityResource resource = assembler.toFullResource(entity);
        return ResponseEntity.accepted().body(resource);
    }
}
