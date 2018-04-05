package org.humancellatlas.ingest.export;

import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultExporter implements Exporter {

    @Autowired
    private MessageRouter messageRouter;

    @Override
    public void exportBundles(SubmissionEnvelope submissionEnvelope) {
        messageRouter.sendAssayForExport(new Process());
    }

}
