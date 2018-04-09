package org.humancellatlas.ingest.core.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
        //NOTE: this effectively does nothing but execute the routing process on a separate thread
        executor.submit(() -> {
            List<Process> assaysInEnvelope = new ArrayList<>(processService.findAssays(envelope));
            int totalAssays = assaysInEnvelope.size();

            for(int assayIndex = 0; assayIndex < totalAssays; assayIndex++) {
                Process assay = assaysInEnvelope.get(assayIndex);
                messageRouter.routeFoundAssayMessage(assay, envelope, assayIndex, totalAssays);
            }
        });
    }

}
