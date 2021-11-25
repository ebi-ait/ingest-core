package org.humancellatlas.ingest.submission.web;

import lombok.Getter;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class SubmissionLinkMapController {

    @Autowired
    BiomaterialRepository biomaterialRepository;
    @Autowired
    FileRepository fileRepository;
    @Autowired
    ProcessRepository processRepository;
    @Autowired
    ProtocolRepository protocolRepository;


    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/linkingMap", method = RequestMethod.GET)
    @ResponseBody
    public SubmissionLinkingMap getSubmissionLinkMap(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope) {
        return new SubmissionLinkingMap(submissionEnvelope);
    }

    @Getter
    public class SubmissionLinkingMap {
        final Dictionary<String, ProcessLinkingMap> processes = new Hashtable<>();
        final Dictionary<String, UUID> protocols = new Hashtable<>();
        final Dictionary<String, BiomaterialLinkingMap> biomaterials = new Hashtable<>();
        final Dictionary<String, FileLinkingMap> files = new Hashtable<>();

        public SubmissionLinkingMap(SubmissionEnvelope submissionEnvelope){
            processRepository
                .findBySubmissionEnvelope(submissionEnvelope)
                .forEach(process -> this.processes.put(process.getId(), new ProcessLinkingMap(process)));
            biomaterialRepository
                .findBySubmissionEnvelope(submissionEnvelope)
                .forEach(biomaterial -> this.biomaterials.put(biomaterial.getId(), new BiomaterialLinkingMap(biomaterial)));
            fileRepository
                .findBySubmissionEnvelope(submissionEnvelope)
                .forEach(file -> this.files.put(file.getId(), new FileLinkingMap(file)));
        }
    }

    @Getter
    public class ProcessLinkingMap {
        final Collection<String> protocols = new HashSet<>();
        final Collection<String> inputBiomaterials = new HashSet<>();
        final Collection<String> inputFiles = new HashSet<>();

        public ProcessLinkingMap(Process process) {
            process.getProtocols().forEach(protocol -> this.protocols.add(protocol.getId()));
            biomaterialRepository.findByInputToProcessesContains(process).forEach(biomaterial -> this.inputBiomaterials.add(biomaterial.getId()));
            fileRepository.findByInputToProcessesContains(process).forEach(file -> this.inputBiomaterials.add(file.getId()));
        }
    }

    @Getter
    public static class BiomaterialLinkingMap {
        final Collection<String> derivedByProcesses = new HashSet<>();
        final Collection<String> inputToProcesses = new HashSet<>();

        public BiomaterialLinkingMap(Biomaterial biomaterial) {
            biomaterial.getDerivedByProcesses().forEach(process -> this.derivedByProcesses.add(process.getId()));
            biomaterial.getInputToProcesses().forEach(process -> this.inputToProcesses.add(process.getId()));
        }
    }

    @Getter
    public static class FileLinkingMap {
        final Collection<String> derivedByProcesses = new HashSet<>();
        final Collection<String> inputToProcesses = new HashSet<>();

        public FileLinkingMap(File file) {
            file.getDerivedByProcesses().forEach(process -> this.derivedByProcesses.add(process.getId()));
            file.getInputToProcesses().forEach(process -> this.inputToProcesses.add(process.getId()));
        }
    }
}
