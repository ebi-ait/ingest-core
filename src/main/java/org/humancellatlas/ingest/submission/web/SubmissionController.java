package org.humancellatlas.ingest.submission.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.analysis.Analysis;
import org.humancellatlas.ingest.analysis.AnalysisRepository;
import org.humancellatlas.ingest.assay.Assay;
import org.humancellatlas.ingest.assay.AssayRepository;
import org.humancellatlas.ingest.core.Event;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.sample.Sample;
import org.humancellatlas.ingest.sample.SampleRepository;
import org.humancellatlas.ingest.submission.SubmissionState;
import org.humancellatlas.ingest.submission.state.SubmissionEnvelopeStateEngine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Spring controller that will handle submission events on a {@link SubmissionEnvelope}
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
@RepositoryRestController
@ExposesResourceFor(SubmissionEnvelope.class)
@RequiredArgsConstructor
@Getter
public class SubmissionController {
    private final @NonNull SubmissionEnvelopeStateEngine stateEngine;

    private final @NonNull AnalysisRepository analysisRepository;
    private final @NonNull AssayRepository assayRepository;
    private final @NonNull FileRepository fileRepository;
    private final @NonNull ProjectRepository projectRepository;
    private final @NonNull ProtocolRepository protocolRepository;
    private final @NonNull SampleRepository sampleRepository;

    private final @NonNull PagedResourcesAssembler pagedResourcesAssembler;

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/analyses", method = RequestMethod.GET)
    ResponseEntity<?> getAnalyses(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                             Pageable pageable,
                                             final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Analysis> analyses = getAnalysisRepository().findBySubmissionEnvelope(submissionEnvelope, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(analyses, resourceAssembler));
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/assays", method = RequestMethod.GET)
    ResponseEntity<?> getAssays(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                Pageable pageable,
                                final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Assay> assays = getAssayRepository().findBySubmissionEnvelope(submissionEnvelope, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(assays, resourceAssembler));
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/files", method = RequestMethod.GET)
    ResponseEntity<?> getFiles(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                               Pageable pageable,
                               final PersistentEntityResourceAssembler resourceAssembler) {
        Page<File> files = getFileRepository().findBySubmissionEnvelope(submissionEnvelope, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(files, resourceAssembler));
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/projects", method = RequestMethod.GET)
    ResponseEntity<?> getProjects(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                  Pageable pageable,
                                  final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Project> projects = getProjectRepository().findBySubmissionEnvelope(submissionEnvelope, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(projects, resourceAssembler));
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/protocols", method = RequestMethod.GET)
    ResponseEntity<?> getProtocols(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                   Pageable pageable,
                                   final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Protocol> protocols = getProtocolRepository().findBySubmissionEnvelope(submissionEnvelope, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(protocols, resourceAssembler));
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/samples", method = RequestMethod.GET)
    ResponseEntity<?> getSamples(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                 Pageable pageable,
                                 final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Sample> samples = getSampleRepository().findBySubmissionEnvelope(submissionEnvelope, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(samples, resourceAssembler));
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.SUBMIT_URL, method = RequestMethod.PUT)
    HttpEntity<?> submitEnvelope(@PathVariable("id") SubmissionEnvelope submissionEnvelope) {
        Event event = getStateEngine().advanceStateOfEnvelope(submissionEnvelope, SubmissionState.SUBMITTED);
        return ResponseEntity.accepted().body(event);
    }
}
