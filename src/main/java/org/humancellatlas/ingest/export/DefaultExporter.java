package org.humancellatlas.ingest.export;

import org.humancellatlas.ingest.messaging.ExportMessage;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.stream.IntStream;

public class DefaultExporter implements Exporter {

    @Autowired
    private ProcessService processService;

    @Autowired
    private MessageRouter messageRouter;

    @Override
    public void exportBundles(SubmissionEnvelope submissionEnvelope) {
        Collection<Process> assayingProcesses = processService.findAssays(submissionEnvelope);
        Collection<Process> analysisProcesses = processService.findAnalyses(submissionEnvelope);
        int totalCount = assayingProcesses.size() + analysisProcesses.size();
        IntStream.range(0, totalCount)
                .mapToObj(count -> new ExportMessage(count))
                .forEach(message -> messageRouter.sendAnalysisForExport(message));
    }

}
