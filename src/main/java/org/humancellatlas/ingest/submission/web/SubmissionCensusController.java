package org.humancellatlas.ingest.submission.web;

import lombok.Getter;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class SubmissionCensusController {

    @Autowired
    BiomaterialRepository biomaterialRepository;
    @Autowired
    FileRepository fileRepository;
    @Autowired
    ProcessRepository processRepository;
    @Autowired
    ProtocolRepository protocolRepository;


    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/census", method = RequestMethod.GET)
    @ResponseBody
    public SubmissionCensus submissionCensus(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope) {
        return new SubmissionCensus(submissionEnvelope);
    }

    @Getter
    public class SubmissionCensus {
        private final UUID uuid;
        private final Dictionary<String, ProcessCensus> processes = new Hashtable<>();
        private final Dictionary<String, UUID> protocols = new Hashtable<>();
        private final Dictionary<String, UUID> biomaterials = new Hashtable<>();
        private final Dictionary<String, UUID> files = new Hashtable<>();

        public SubmissionCensus(SubmissionEnvelope submissionEnvelope){
            this.uuid = submissionEnvelope.getUuid().getUuid();
            processRepository
                .findBySubmissionEnvelope(submissionEnvelope)
                .forEach(process -> this.processes.put(process.getId(), new ProcessCensus(process)));
            protocolRepository
                .findBySubmissionEnvelope(submissionEnvelope)
                .forEach(protocol -> this.protocols.put(protocol.getId(), protocol.getUuid().getUuid()));
            biomaterialRepository
                .findBySubmissionEnvelope(submissionEnvelope)
                .forEach(biomaterial -> this.biomaterials.put(biomaterial.getId(), biomaterial.getUuid().getUuid()));
            fileRepository
                .findBySubmissionEnvelope(submissionEnvelope)
                .forEach(file -> this.files.put(file.getId(), file.getUuid().getUuid()));
        }
    }

    @Getter
    public class ProcessCensus {
        private final UUID uuid;
        private final Collection<String> protocols = new HashSet<>();
        private final Collection<String> inputBiomaterials = new HashSet<>();
        private final Collection<String> derivedBiomaterials = new HashSet<>();
        private final Collection<String> inputFiles = new HashSet<>();
        private final Collection<String> derivedFiles = new HashSet<>();

        public ProcessCensus(Process process) {
            this.uuid = process.getUuid().getUuid();
            process.getProtocols().forEach(protocol -> this.protocols.add(protocol.getId()));
            biomaterialRepository.findByInputToProcessesContains(process).forEach(biomaterial -> this.inputBiomaterials.add(biomaterial.getId()));
            biomaterialRepository.findByDerivedByProcessesContains(process).forEach(biomaterial -> this.derivedBiomaterials.add(biomaterial.getId()));
            fileRepository.findByInputToProcessesContains(process).forEach(file -> this.inputFiles.add(file.getId()));
            fileRepository.findByDerivedByProcessesContains(process).forEach(file -> this.derivedFiles.add(file.getId()));
        }
    }
}
