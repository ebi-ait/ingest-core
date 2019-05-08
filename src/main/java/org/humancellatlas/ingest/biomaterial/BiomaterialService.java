package org.humancellatlas.ingest.biomaterial;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
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
public class BiomaterialService {
  private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
  private final @NonNull BiomaterialRepository biomaterialRepository;
  private final @NonNull MetadataUpdateService metadataUpdateService;
  private final @NonNull MetadataCrudService metadataCrudService;

  private final Logger log = LoggerFactory.getLogger(getClass());

  protected Logger getLog() {
    return log;
  }

  public Biomaterial addBiomaterialToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Biomaterial biomaterial) {
    biomaterial.setIsUpdate(submissionEnvelope.getIsUpdate());

    if(! biomaterial.getIsUpdate()) {
      return metadataCrudService.addToSubmissionEnvelopeAndSave(biomaterial, submissionEnvelope);
    } else {
      return metadataUpdateService.acceptUpdate(biomaterial, submissionEnvelope);
    }
  }
}
