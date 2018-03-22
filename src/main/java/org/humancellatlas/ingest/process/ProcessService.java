package org.humancellatlas.ingest.process;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.jni.Proc;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by rolando on 19/02/2018.
 */
@Service
@RequiredArgsConstructor
@Getter
public class ProcessService {
    private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
    private final @NonNull ProcessRepository processRepository;
    private final @NonNull FileRepository fileRepository;
    private final @NonNull BiomaterialRepository biomaterialRepository;
    private final @NonNull BundleManifestRepository bundleManifestRepository;

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

    public Process addFileToAnalysisProcess(Process analysis, File file) {
        getFileRepository().save(file.addAsDerivedByProcess(analysis));
        return analysis;
    }

    public Process resolveBundleReferencesForProcess(Process analysis, BundleReference bundleReference) {
        for (String bundleUuid : bundleReference.getBundleUuids()) {
            BundleManifest bundleManifest = getBundleManifestRepository().findByBundleUuid(bundleUuid);
            if (bundleManifest != null) {
                getLog().info(String.format("Adding bundle manifest link to process '%s'", analysis.getId()));
                analysis.addInputBundleManifest(bundleManifest);
            }
            else {
                getLog().warn(String.format(
                        "No Bundle Manifest present with bundle UUID '%s' - in future this will cause a critical error",
                        bundleUuid));
            }
        }
        return getProcessRepository().save(analysis);
    }

    private Page<Process> findAssays(SubmissionEnvelope submissionEnvelope, Pageable pageable) {
        Set<Process> results = new LinkedHashSet<>();
        long fileStartTime = System.currentTimeMillis();
        List<File> derivedFiles = fileRepository.findBySubmissionEnvelopesContains(submissionEnvelope);

        long fileEndTime = System.currentTimeMillis();
        float fileQueryTime = ((float)(fileEndTime - fileStartTime)) / 1000;
        String fileQt = new DecimalFormat("#,###.##").format(fileQueryTime);
        getLog().info("Retrieving assays: file query time: {} s", fileQt);
        long allBioStartTime = System.currentTimeMillis();

        for (File derivedFile : derivedFiles) {
            for (Process derivedByProcess : derivedFile.getDerivedByProcesses()) {
                if (!biomaterialRepository.findByInputToProcessesContains(derivedByProcess).isEmpty()) {
                    results.add(derivedByProcess);
                }
            }
        }
        long allBioEndTime = System.currentTimeMillis();
        float allBioQueryTime = ((float)(allBioEndTime - allBioStartTime)) / 1000;
        String allBioQt = new DecimalFormat("#,###.##").format(allBioQueryTime);
        getLog().info("Retrieving assays: biomaterial query time: {} s", allBioQt);
        return results;
    }

    public Collection<Process> findAnalyses(SubmissionEnvelope submissionEnvelope) {
        Set<Process> results = new LinkedHashSet<>();
        List<File> derivedFiles = fileRepository.findBySubmissionEnvelopesContains(submissionEnvelope);

        for (File derivedFile : derivedFiles) {
            for (Process derivedByProcess : derivedFile.getDerivedByProcesses()) {
                if (!fileRepository.findByInputToProcessesContains(derivedByProcess).isEmpty()) {
                    results.add(derivedByProcess);
                }
            }
        }
        return results;
    }
}
