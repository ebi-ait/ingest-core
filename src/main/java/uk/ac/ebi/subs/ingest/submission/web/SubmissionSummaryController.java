package uk.ac.ebi.subs.ingest.submission.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.ac.ebi.subs.ingest.biomaterial.BiomaterialRepository;
import uk.ac.ebi.subs.ingest.core.Uuid;
import uk.ac.ebi.subs.ingest.file.FileRepository;
import uk.ac.ebi.subs.ingest.file.ValidationErrorType;
import uk.ac.ebi.subs.ingest.process.ProcessRepository;
import uk.ac.ebi.subs.ingest.protocol.ProtocolRepository;
import uk.ac.ebi.subs.ingest.state.ValidationState;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

@RestController
public class SubmissionSummaryController {

  @Autowired BiomaterialRepository biomaterialRepository;
  @Autowired FileRepository fileRepository;
  @Autowired ProcessRepository processRepository;
  @Autowired ProtocolRepository protocolRepository;

  @RequestMapping(path = "/submissionEnvelopes/{sub_id}/summary", method = RequestMethod.GET)
  @ResponseBody
  public SubmissionSummary submissionSummary(
      @PathVariable("sub_id") SubmissionEnvelope submissionEnvelope) {
    SubmissionSummary summary = new SubmissionSummary();
    summary.setUuid(submissionEnvelope.getUuid());
    summary.setTotalBiomaterials(
        biomaterialRepository.countBySubmissionEnvelope(submissionEnvelope));
    summary.setTotalFiles(fileRepository.countBySubmissionEnvelope(submissionEnvelope));
    summary.setTotalProcesses(processRepository.countBySubmissionEnvelope(submissionEnvelope));
    summary.setTotalProtocols(protocolRepository.countBySubmissionEnvelope(submissionEnvelope));

    long invalidBiomaterials =
        biomaterialRepository.countBySubmissionEnvelopeAndValidationState(
            submissionEnvelope, ValidationState.INVALID);
    // Setting a special graphInvalid[type] property until dcp-546 is done
    // This allows us to filter by graph invalid entities until we consolidate the
    // graphValidationState into
    // validationState
    long graphInvalidBiomaterials =
        biomaterialRepository.countBySubmissionEnvelopeAndCountWithGraphValidationErrors(
            submissionEnvelope.getId());

    long invalidFiles =
        fileRepository.countBySubmissionEnvelopeAndValidationState(
            submissionEnvelope, ValidationState.INVALID);
    long graphInvalidFiles =
        fileRepository.countBySubmissionEnvelopeAndCountWithGraphValidationErrors(
            submissionEnvelope.getId());
    long fileMetadataErrors =
        fileRepository.countBySubmissionEnvelopeIdAndErrorType(
            submissionEnvelope.getId(), ValidationErrorType.METADATA_ERROR.name());
    long missingFiles =
        fileRepository.countBySubmissionEnvelopeIdAndErrorType(
            submissionEnvelope.getId(), ValidationErrorType.FILE_NOT_UPLOADED.name());
    long fileErrors =
        fileRepository.countBySubmissionEnvelopeIdAndErrorType(
            submissionEnvelope.getId(), ValidationErrorType.FILE_ERROR.name());

    long invalidProcesses =
        processRepository.countBySubmissionEnvelopeAndValidationState(
            submissionEnvelope, ValidationState.INVALID);
    long graphInvalidProcesses =
        processRepository.countBySubmissionEnvelopeAndCountWithGraphValidationErrors(
            submissionEnvelope.getId());
    long invalidProtocols =
        protocolRepository.countBySubmissionEnvelopeAndValidationState(
            submissionEnvelope, ValidationState.INVALID);
    long graphInvalidProtocols =
        protocolRepository.countBySubmissionEnvelopeAndCountWithGraphValidationErrors(
            submissionEnvelope.getId());

    long totalInvalid =
        invalidBiomaterials
            + graphInvalidBiomaterials
            + (fileMetadataErrors + missingFiles + fileErrors + graphInvalidFiles)
            + invalidProcesses
            + graphInvalidProcesses
            + invalidProtocols
            + graphInvalidProtocols;

    summary.setInvalidBiomaterials(invalidBiomaterials);
    summary.setGraphInvalidBiomaterials(graphInvalidBiomaterials);

    summary.setInvalidFiles(invalidFiles);
    summary.setGraphInvalidFiles(graphInvalidFiles);
    summary.setFileMetadataErrors(fileMetadataErrors);
    summary.setMissingFiles(missingFiles);
    summary.setFileErrors(fileErrors);

    summary.setInvalidProcesses(invalidProcesses);
    summary.setGraphInvalidProcesses(graphInvalidProcesses);

    summary.setInvalidProtocols(invalidProtocols);
    summary.setGraphInvalidProtocols(graphInvalidProtocols);

    summary.setTotalInvalid(totalInvalid);

    return summary;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public class SubmissionSummary {

    private Uuid uuid;
    private Long totalBiomaterials, invalidBiomaterials, graphInvalidBiomaterials;
    private Long totalFiles,
        invalidFiles,
        graphInvalidFiles,
        fileMetadataErrors,
        missingFiles,
        fileErrors;
    private Long totalProcesses, invalidProcesses, graphInvalidProcesses;
    private Long totalProtocols, invalidProtocols, graphInvalidProtocols;
    private Long totalInvalid;
  }
}
