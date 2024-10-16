package uk.ac.ebi.subs.ingest.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.core.service.MetadataCrudService;
import uk.ac.ebi.subs.ingest.core.service.MetadataUpdateService;
import uk.ac.ebi.subs.ingest.process.ProcessRepository;
import uk.ac.ebi.subs.ingest.project.ProjectRepository;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelopeRepository;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 05/09/17
 */
@Service
@RequiredArgsConstructor
@Getter
public class ProtocolService {

  private final @NonNull MetadataCrudService metadataCrudService;
  private final @NonNull MetadataUpdateService metadataUpdateService;

  private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
  private final @NonNull ProjectRepository projectRepository;
  private final @NonNull ProtocolRepository protocolRepository;
  private final @NonNull ProcessRepository processRepository;

  private final Logger log = LoggerFactory.getLogger(getClass());

  protected Logger getLog() {
    return log;
  }

  public Protocol addProtocolToSubmissionEnvelope(
      SubmissionEnvelope submissionEnvelope, Protocol protocol) {
    if (!protocol.getIsUpdate()) {
      projectRepository
          .findBySubmissionEnvelopesContains(submissionEnvelope)
          .findFirst()
          .ifPresent(protocol::setProject);
      return metadataCrudService.addToSubmissionEnvelopeAndSave(protocol, submissionEnvelope);
    } else {
      return metadataUpdateService.acceptUpdate(protocol, submissionEnvelope);
    }
  }

  public Page<Protocol> retrieve(SubmissionEnvelope submission, Pageable pageable) {
    Page<Protocol> protocols = protocolRepository.findBySubmissionEnvelope(submission, pageable);
    protocols.forEach(
        protocol -> {
          processRepository
              .findFirstByProtocolsContains(protocol)
              .ifPresent(it -> protocol.markAsLinked());
        });
    return protocols;
  }
}
