package org.humancellatlas.ingest.export;

import org.humancellatlas.ingest.messaging.ExportMessage;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DefaultExporter implements Exporter {

    @Autowired
    private ProcessService processService;

    @Autowired
    private MessageRouter messageRouter;

    @Override
    public void exportBundles(SubmissionEnvelope submissionEnvelope) {
        Collection<Process> assayingProcesses = processService.findAssays(submissionEnvelope);
        Collection<Process> analysisProcesses = processService.findAnalyses(submissionEnvelope);
        List<Process> allProcesses = Stream
                .concat(assayingProcesses.stream(), analysisProcesses.stream())
                .collect(Collectors.toList());
        int totalCount = allProcesses.size();
        IntStream.range(0, totalCount)
                .mapToObj(count -> new ExportMessage(count, totalCount, allProcesses.get(count)))
                .forEach(message -> {
                    if (assayingProcesses.contains(message.getProcess())) {
                        messageRouter.sendAssayForExport(message);
                    } else {
                        messageRouter.sendAnalysisForExport(message);
                    }
                });
    }

}
