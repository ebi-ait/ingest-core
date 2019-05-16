package org.humancellatlas.ingest.export;

import org.humancellatlas.ingest.submission.SubmissionEnvelope;

public interface Exporter {

    void exportBundles(SubmissionEnvelope submissionEnvelope);

    void updateBundles(SubmissionEnvelope submissionEnvelope);
}
