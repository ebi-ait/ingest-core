package org.humancellatlas.ingest.biomaterial;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.query.MetadataCriteria;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by rolando on 19/02/2018.
 */
@Service
@RequiredArgsConstructor
@Getter
public class BiomaterialService {
    private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
    private final @NonNull BiomaterialRepository biomaterialRepository;
    private final @NonNull ProcessRepository processRepository;
    private final @NonNull MetadataUpdateService metadataUpdateService;
    private final @NonNull MetadataCrudService metadataCrudService;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public Biomaterial addBiomaterialToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Biomaterial biomaterial) {
        if (!biomaterial.getIsUpdate()) {
            return metadataCrudService.addToSubmissionEnvelopeAndSave(biomaterial, submissionEnvelope);
        } else {
            return metadataUpdateService.acceptUpdate(biomaterial, submissionEnvelope);
        }
    }

    public Page<Biomaterial> findByCriteria(List<MetadataCriteria> criteriaList, Boolean andCriteria, Pageable pageable) {
        return this.biomaterialRepository.findByCriteria(criteriaList, andCriteria, pageable);
    }
}
