package org.humancellatlas.ingest.manifest.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.manifest.EmptyManifest;
import org.humancellatlas.ingest.manifest.SubmissionManifest;
import org.humancellatlas.ingest.manifest.SubmissionManifestRepository;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by rolando on 30/05/2018.
 */
@RepositoryRestController
@ExposesResourceFor(Project.class)
@RequiredArgsConstructor
@Getter
public class SubmissionManifestController {
    private final @NonNull SubmissionManifestRepository submissionManifestRepository;

    @RequestMapping(path = "submissionEnvelopes/{sub_id}/addEmptyManifest", method = RequestMethod.POST)
    ResponseEntity<Resource<?>> addEmptyManifestToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                                           PersistentEntityResourceAssembler assembler) {
        SubmissionManifest submissionManifest = submissionManifestRepository.save(new EmptyManifest(submissionEnvelope));
        PersistentEntityResource resource = assembler.toFullResource(submissionManifest);
        return ResponseEntity.accepted().body(resource);
    }
}
