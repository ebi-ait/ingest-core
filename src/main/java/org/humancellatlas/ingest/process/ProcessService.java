package org.humancellatlas.ingest.process;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    @NonNull
    @Autowired
    private FileRepository fileRepository;

    @NonNull
    @Autowired
    private BiomaterialRepository biomaterialRepository;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public Page<Biomaterial> findInputBiomaterialsForProcess(Process process, Pageable pageable) {
        return biomaterialRepository.findByInputToProcessesContaining(process, pageable);
    }

    public Page<File> findInputFilesForProcess(Process process, Pageable pageable) {
        return fileRepository.findByInputToProcessesContaining(process, pageable);
    }

    public Page<Biomaterial> findOutputBiomaterialsForProcess(Process process, Pageable pageable) {
        return biomaterialRepository.findByDerivedByProcessesContaining(process, pageable);
    }

    public Page<File> findOutputFilesForProcess(Process process, Pageable pageable) {
        return fileRepository.findByDerivedByProcessesContaining(process, pageable);
    }

    public Process addProcessToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope,
            Process process) {
        process.addToSubmissionEnvelope(submissionEnvelope);
        return getProcessRepository().save(process);
    }

    public Page<Process> retrieveAssaysFrom(SubmissionEnvelope submissionEnvelope,
            Pageable pageable) {
        List<Process> assays = findAssays(submissionEnvelope, pageable);
        return new PageImpl<>(assays, pageable, assays.size());
    }

    public Page<Process> retrieveAnalysesFrom(SubmissionEnvelope submissionEnvelope,
            Pageable pageable) {
        List<Process> analyses = findAnalyses(submissionEnvelope, pageable);
        return new PageImpl<>(analyses, pageable, analyses.size());
    }

    private List<Process> findAssays(SubmissionEnvelope submissionEnvelope, Pageable pageable) {
        List<Process> results = new ArrayList<>();
        List<Process> processes = processRepository.findBySubmissionEnvelopesContaining(submissionEnvelope);
        for (Process process : processes) {
            if (!biomaterialRepository.findByInputToProcessesContains(process).isEmpty()) {
                // input to process is a biomaterial
                if (! fileRepository.findByDerivedByProcessesContains(process).isEmpty()) {
                    results.add(process);
                }
            }
        }
        return results;
    }

    private List<Process> findAnalyses(SubmissionEnvelope submissionEnvelope, Pageable pageable) {
        List<Process> results = new ArrayList<>();
        List<Process> processes = processRepository.findBySubmissionEnvelopesContaining(submissionEnvelope);
        for (Process process : processes) {
            if (!fileRepository.findByInputToProcessesContains(process).isEmpty()) {
                // input to process is a biomaterial
                if (! fileRepository.findByDerivedByProcessesContains(process).isEmpty()) {
                    results.add(process);
                }
            }
        }
        return results;
    }
}
