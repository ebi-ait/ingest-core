package org.humancellatlas.ingest.messaging;

public class Constants {
    public class Queues {
        public static final String FILE_STAGED_QUEUE = "ingest.file.create.staged";
        public static final String FILE_VALIDATION_QUEUE = "ingest.file.validation.queue";
        public static final String METADATA_VALIDATION_QUEUE = "ingest.metadata.validation.queue";
        public static final String NOTIFICATIONS_QUEUE = "ingest.notifications.queue";
        public static final String GRAPH_VALIDATION_QUEUE = "ingest.validation.graph.queue";
    }

    public class Exchanges {
        public static final String VALIDATION_EXCHANGE = "ingest.validation.exchange";
        public static final String FILE_STAGED_EXCHANGE = "ingest.file.staged.exchange";
        public static final String STATE_TRACKING_EXCHANGE = "ingest.state-tracking.exchange";
        public static final String EXPORTER_EXCHANGE = "ingest.exporter.exchange";
        public static final String UPLOAD_AREA_EXCHANGE = "ingest.upload.area.exchange";
        public static final String NOTIFICATIONS_EXCHANGE = "ingest.notifications.exchange";
        public static final String SPREADSHEET_EXCHANGE = "ingest.spreadsheet.exchange";
    }

    public class Routing {
        public static final String ENVELOPE_STATE_UPDATE = "ingest.state-tracking.envelope.state.update";
        public static final String ENVELOPE_CREATE = "ingest.state-tracking.envelope.create";

        public static final String MANIFEST_SUBMITTED = "ingest.exporter.manifest.submitted";
        public static final String EXPERIMENT_SUBMITTED = "ingest.exporter.experiment.submitted";

        public static final String SUBMISSION_SUBMITTED = "ingest.exporter.submission.submitted";

        public static final String UPLOAD_AREA_CREATE = "ingest.upload.area.create";
        public static final String UPLOAD_AREA_CLEANUP = "ingest.upload.area.cleanup";

        public static final String NOTIFICATION_NEW = "ingest.notifications.new";
        public static final String SPREADSHEET_GENERATION = "ingest.spreadsheet.generate";
    }

}
