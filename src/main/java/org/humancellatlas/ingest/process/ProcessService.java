package org.humancellatlas.ingest.process;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by rolando on 19/02/2018.
 */
@Service
@RequiredArgsConstructor
@Getter
public class ProcessService {
  private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
  private final @NonNull ProcessRepository processRepository;

  private final Logger log = LoggerFactory.getLogger(getClass());

  protected Logger getLog() {
    return log;
  }

  public Process addProcessToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Process process) {
    process.addToSubmissionEnvelope(submissionEnvelope);
    return getProcessRepository().save(process);
  }

}
