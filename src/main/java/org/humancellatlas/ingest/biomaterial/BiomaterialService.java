package org.humancellatlas.ingest.biomaterial;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by rolando on 19/02/2018.
 */
@Service
@RequiredArgsConstructor
@Getter
public class BiomaterialService {
    private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
    private final @NonNull BiomaterialRepository biomaterialRepository;
    private final @NonNull ProcessRepository processRepository;
    private final @NonNull MetadataUpdateService metadataUpdateService;
    private final @NonNull MetadataCrudService metadataCrudService;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public Biomaterial addBiomaterialToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Biomaterial biomaterial) {
        if (!biomaterial.getIsUpdate()) {
            return metadataCrudService.addToSubmissionEnvelopeAndSave(biomaterial, submissionEnvelope);
        } else {
            return metadataUpdateService.acceptUpdate(biomaterial, submissionEnvelope);
        }
    }

    public void addInputBiomaterial(Biomaterial inputBiomaterial, Process process, Biomaterial biomaterial) {
        if (process != null) {
            processRepository.save(process);
            biomaterial.addAsDerivedByProcess(process);
            inputBiomaterial.addAsInputToProcess(process);
            biomaterialRepository.save(biomaterial);
        } else {
            throw new RuntimeException("There should be a process to link biomaterials");
        }
    }

    public Page<Biomaterial> getInputBiomaterials(Biomaterial biomaterial, Pageable pageable) {
        // Currently, any given biomaterial or file would be the output of a single process, not multiple ones
        // See conversation: https://embl-ebi-ait.slack.com/archives/C016R78CDV1/p1602757614320200
        Page<Biomaterial> inputBiomaterials = new PageImpl<>(Collections.emptyList());
        Set<Process> derivedByProcesses = biomaterial.getDerivedByProcesses();
        if (!derivedByProcesses.isEmpty()) {
            Process derivedByProcess = derivedByProcesses.iterator().next();
            inputBiomaterials = biomaterialRepository.findByInputToProcessesContaining(derivedByProcess, pageable);

        }
        return inputBiomaterials;
    }

    public void deleteInputBiomaterial(Biomaterial biomaterial, Biomaterial biomaterialToDelete) {
        Set<Process> derivedByProcesses = biomaterial.getDerivedByProcesses();

        if (!derivedByProcesses.isEmpty()) {
            Process derivedByProcess = derivedByProcesses.iterator().next();
            Stream<Biomaterial> inputBiomaterials = biomaterialRepository.findByInputToProcessesContains(derivedByProcess);

            List<Biomaterial> inputBiomaterialList = inputBiomaterials.collect(Collectors.toList());
            long inputBiomaterialCount = inputBiomaterialList.size();

            if (inputBiomaterialCount == 0) {
                throw new RuntimeException(
                        String.format("A biomaterial with uuid %s has a process with uuid %s " +
                                        "that is not linked to any biomaterial input",
                                biomaterial.getUuid().toString(), derivedByProcess.getUuid())
                );
            } else {
                inputBiomaterialList.forEach(inputBiomaterial -> {
                    if (inputBiomaterial.equals(biomaterialToDelete)){
                        inputBiomaterial.removeAsInputToProcess(derivedByProcess);
                        biomaterial.removeAsDerivedByProcess(derivedByProcess);
                        biomaterialRepository.save(inputBiomaterial);
                        biomaterialRepository.save(biomaterial);
                    }
                });

                if (inputBiomaterialCount == 1) {
                    processRepository.delete(derivedByProcess);
                }
            }


        }
    }
}
