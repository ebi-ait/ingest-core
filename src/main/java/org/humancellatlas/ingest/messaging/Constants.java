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
        public static final String VALIDATION_REQUIRED = "ingest.metadata.validation.queue";
        public static final String ACCESSION_REQUIRED = "ingest.metadata.accession.queue";
        public static final String SUBMISSION_ARCHIVAL = "ingest.archival.queue";
    }

    public class Exchanges {
        public static final String VALIDATION = "ingest.validation.exchange";
        public static final String FILE_FANOUT = "ingest.file.update.exchange";
        public static final String FILE_STAGED_FANOUT = "ingest.file.staged.exchange";
        public static final String ENVELOPE_CREATED_FANOUT = "ingest.envelope.created.exchange";
        public static final String ENVELOPE_SUBMITTED_FANOUT = "ingest.envelope.submitted.exchange";
        public static final String ACCESSION = "ingest.accession.exchange";
        public static final String SUBMISSION_ARCHIVAL_DIRECT = "ingest.archival.exchange";
    }
}
