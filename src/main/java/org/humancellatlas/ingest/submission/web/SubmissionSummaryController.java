package org.humancellatlas.ingest.submission.web;

import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.file.ValidationErrorType;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@RestController
@RequestMapping("/submissionEnvelopes")
public class SubmissionSummaryController implements ResourceProcessor<RepositoryLinksResource> {

    @Autowired
    BiomaterialRepository biomaterialRepository;
    @Autowired
    FileRepository fileRepository;
    @Autowired
    ProcessRepository processRepository;
    @Autowired
    ProtocolRepository protocolRepository;

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(ControllerLinkBuilder.linkTo(SubmissionSummaryController.class).withRel("submissionEnvelopes"));
        return resource;
    }

    @RequestMapping(path = "/{sub_id}/summary", method = RequestMethod.GET)
    @ResponseBody
    public SubmissionSummary submissionSummary(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope) {

        SubmissionSummary summary = new SubmissionSummary();
        summary.setUuid(submissionEnvelope.getUuid());
        summary.setTotalBiomaterials(biomaterialRepository.countBySubmissionEnvelope(submissionEnvelope));
        summary.setTotalFiles(fileRepository.countBySubmissionEnvelope(submissionEnvelope));
        summary.setTotalProcesses(processRepository.countBySubmissionEnvelope(submissionEnvelope));
        summary.setTotalProtocols(protocolRepository.countBySubmissionEnvelope(submissionEnvelope));

        long invalidBiomaterials = biomaterialRepository.countBySubmissionEnvelopeAndValidationState(submissionEnvelope, ValidationState.INVALID);
        long invalidFiles = fileRepository.countBySubmissionEnvelopeAndValidationState(submissionEnvelope, ValidationState.INVALID);
        long missingFiles = fileRepository.countBySubmissionEnvelopeIdAndErrorType(submissionEnvelope.getId(), ValidationErrorType.FILE_NOT_UPLOADED.name());
        long invalidProcesses = processRepository.countBySubmissionEnvelopeAndValidationState(submissionEnvelope, ValidationState.INVALID);
        long invalidProtocols = protocolRepository.countBySubmissionEnvelopeAndValidationState(submissionEnvelope, ValidationState.INVALID);

        summary.setInvalidBiomaterials(invalidBiomaterials);
        summary.setInvalidFiles(invalidFiles);
        summary.setMissingFiles(missingFiles);
        summary.setInvalidProcesses(invalidProcesses);
        summary.setInvalidProtocols(invalidProtocols);

        return summary;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public class SubmissionSummary {

        private Uuid uuid;
        private Long totalBiomaterials, invalidBiomaterials;
        private Long totalFiles, invalidFiles, missingFiles;
        private Long totalProcesses, invalidProcesses;
        private Long totalProtocols, invalidProtocols;

    }
    
}
