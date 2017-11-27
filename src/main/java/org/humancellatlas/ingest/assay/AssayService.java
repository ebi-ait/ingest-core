package org.humancellatlas.ingest.assay;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.MetadataReference;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 05/09/17
 */
@Service
@RequiredArgsConstructor
@Getter
public class AssayService {
    private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
    private final @NonNull AssayRepository assayRepository;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }
    public Assay addAssayToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Assay assay) {
        assay.addToSubmissionEnvelope(submissionEnvelope);
        return getAssayRepository().save(assay);
    }

    public SubmissionEnvelope resolveAssayReferencesForSubmission(SubmissionEnvelope submissionEnvelope, MetadataReference reference) {
        List<Assay> assays = new ArrayList<>();

        for (String uuid : reference.getUuids()) {
            Uuid uuidObj = new Uuid(uuid);
            Assay assay = getAssayRepository().findByUuid(uuidObj);

            if (assay != null) {
                assay.addToSubmissionEnvelope(submissionEnvelope);
                assays.add(assay);
                getLog().info(String.format("Adding assay to submission envelope '%s'", assay.getId()));
            }
            else {
                getLog().warn(String.format(
                        "No Assay present with UUID '%s' - in future this will cause a critical error",
                        uuid));
            }
        }

        getAssayRepository().save(assays);

        return submissionEnvelope;
    }
}
