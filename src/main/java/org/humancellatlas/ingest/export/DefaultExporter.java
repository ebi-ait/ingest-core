package org.humancellatlas.ingest.export;

import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class DefaultExporter implements Exporter {

    @Autowired
    private ProcessService processService;

    @Autowired
    private MessageRouter messageRouter;

    @Override
    public void exportBundles(SubmissionEnvelope envelope) {
        Collection<Process> assayingProcesses = processService.findAssays(envelope);
        Collection<Process> analysisProcesses = processService.findAnalyses(envelope);

        IndexCounter counter = new IndexCounter();
        int totalCount = assayingProcesses.size() + analysisProcesses.size();
        assayingProcesses.stream()
                .map(process -> new ExportData(counter.next(), totalCount, process,  envelope))
                .forEach(messageRouter::sendAssayForExport);
        analysisProcesses.stream()
                .map(process -> new ExportData(counter.next(), totalCount, process, envelope))
                .forEach(messageRouter::sendAnalysisForExport);
    }

    private static class IndexCounter {

        int base = 0;

        int next() {
            return base++;
        }

    }

}
