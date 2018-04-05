package org.humancellatlas.ingest.export;

import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

public class DefaultExporter implements Exporter {

    @Autowired
    private ProcessService processService;

    @Autowired
    private MessageRouter messageRouter;

    @Override
    public void exportBundles(SubmissionEnvelope submissionEnvelope) {
        Collection<Process> assayingProcesses = processService.findAssays(submissionEnvelope);
        assayingProcesses.forEach(messageRouter::sendAssayForExport);

        Collection<Process> analysisProcesses = processService.findAnalyses(submissionEnvelope);
        analysisProcesses.forEach(messageRouter::sendAnalysisForExport);
    }

}
