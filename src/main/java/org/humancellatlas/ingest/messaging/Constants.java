package org.humancellatlas.ingest.messaging;

/**
 * @author Simon Jupp
 * @date 04/09/2017
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class Constants {
    public class Queues {
        public static final String FILE_STAGED = "ingest.file.create.staged";
        public static final String FILE_UPDATE = "ingest.file.update.queue";
        public static final String ENVELOPE_CREATED = "ingest.envelope.created.queue";
        public static final String ENVELOPE_SUBMITTED = "ingest.envelope.submitted.queue";
        public static final String ENVELOPE_CLEANUP = "ingest.envelope.cleanup.queue";
        public static final String VALIDATION_REQUIRED = "ingest.metadata.validation.queue";
        public static final String ACCESSION_REQUIRED = "ingest.metadata.accession.queue";
        public static final String SUBMISSION_ARCHIVAL = "ingest.archival.queue";
        public static final String STATE_TRACKING = "ingest.state-tracking.queue";
    }

    public class Exchanges {
        public static final String VALIDATION = "ingest.validation.exchange";
        public static final String FILE_FANOUT = "ingest.file.update.exchange";
        public static final String FILE_STAGED_FANOUT = "ingest.file.staged.exchange";
        public static final String ENVELOPE_CREATED_FANOUT = "ingest.envelope.created.exchange";
        public static final String ENVELOPE_SUBMITTED_FANOUT = "ingest.envelope.submitted.exchange";
        public static final String ENVELOPE_CLEANUP_FANOUT = "ingest.envelope.cleanup.exchange";
        public static final String ACCESSION = "ingest.accession.exchange";
        public static final String SUBMISSION_ARCHIVAL_DIRECT = "ingest.archival.exchange";
        public static final String STATE_TRACKING = "ingest.state-tracking.exchange";
        public static final String ASSAY_EXCHANGE = "ingest.bundle.exchange";

    }

    public class Routing {
        public static final String ENVELOPE_STATE_UPDATE = "ingest.state-tracking.envelope.state.update";
        public static final String ENVELOPE_CREATE = "ingest.state-tracking.envelope.create";
        public static final String METADATA_UPDATE = "ingest.state-tracking.document.update";
        public static final String ASSAY_SUBMITTED = "ingest.bundle.assay.submitted";
        public static final String ANALYSIS_SUBMITTED = "ingest.bundle.analysis.submitted";
    }

}
