package org.humancellatlas.ingest.exporter;

import org.humancellatlas.ingest.submission.SubmissionEnvelope;

public interface Exporter {

    void exportManifests(SubmissionEnvelope submissionEnvelope);

    void exportBundles(SubmissionEnvelope submissionEnvelope);

}
