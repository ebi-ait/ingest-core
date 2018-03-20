package org.humancellatlas.ingest.core.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by rolando on 20/03/2018.
 */
@Service
public class AssayService {
    @Autowired @NonNull private ProcessService processService;
    @Autowired @NonNull private MessageRouter messageRouter;

    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    /**
     * identifies the assays in an envelope and sends a messages to the assay exchange
     * @param envelope
     */
    public void identifyAssaysFor(SubmissionEnvelope envelope) {
        executor.submit(() -> {
            Collection<Process> assaysInEnvelope = processService.findAssays(envelope);
            assaysInEnvelope.forEach(assay -> messageRouter.routeFoundAssayMessage(assay, envelope));
        });
    }

}
