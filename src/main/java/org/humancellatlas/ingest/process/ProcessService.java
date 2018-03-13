package org.humancellatlas.ingest.process;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by rolando on 19/02/2018.
 */
@Service
@NoArgsConstructor
@RequiredArgsConstructor
@Getter
public class ProcessService {

    @NonNull
    private SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @NonNull
    @Autowired
    private ProcessRepository processRepository;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public Process addProcessToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope,
            Process process) {
        process.addToSubmissionEnvelope(submissionEnvelope);
        return getProcessRepository().save(process);
    }

    public Page<Process> retrieveAssaysFrom(SubmissionEnvelope submissionEnvelope,
            Pageable pageable) {
        Page<Process> processes = processRepository
                .findBySubmissionEnvelopesContaining(submissionEnvelope, pageable);
        List<Process> assays = new ArrayList<>();
        processes.forEach(process -> {
            if (process.isAssaying()) assays.add(process);
        });
        return new PageImpl<>(assays, pageable, assays.size());
    }

}
