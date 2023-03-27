package org.humancellatlas.ingest.submission.web;

import lombok.Getter;
import lombok.NonNull;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static java.util.stream.Collectors.*;

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

    @NonNull
    private final Logger log = LoggerFactory.getLogger(getClass());

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}"+ Links.SUBMISSION_LINKING_MAP_URL,
            method = RequestMethod.GET)
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
            log.info("before processes");
            oldImplementation(submissionEnvelope);
            log.info("found {} processes", processes.size());
            log.info("before biomaterials");
            biomaterialRepository
                .findBySubmissionEnvelope(submissionEnvelope)
                .forEach(biomaterial -> this.biomaterials.put(biomaterial.getId(), new BiomaterialLinkingMap(biomaterial)));
            log.info("found {} biomaterials", biomaterials.size());
            log.info("before files");
            fileRepository
                .findBySubmissionEnvelope(submissionEnvelope)
                .forEach(file -> this.files.put(file.getId(), new FileLinkingMap(file)));
            log.info("found {} files", files.size());
        }

        private void oldImplementation(SubmissionEnvelope submissionEnvelope) {
            processRepository
                .findBySubmissionEnvelope(submissionEnvelope)
                .forEach(process -> this.processes.put(process.getId(), new ProcessLinkingMap(process)));
        }

        void newImplementation(SubmissionEnvelope submissionEnvelope) {
            /// each is grouped by process
            /// TODO merge by process ID
            biomaterialRepository.findBySubmissionEnvelope(submissionEnvelope)
                    .flatMap(biomaterial->biomaterial.getInputToProcesses()
                                                        .stream()
                                                        .map(process->Pair.of(biomaterial.getId(), process.getId())))
                    .collect(groupingBy(Pair::getSecond, mapping(Pair::getFirst, toSet())));
            fileRepository.findBySubmissionEnvelope(submissionEnvelope)
                    .flatMap(file->file.getInputToProcesses()
                            .stream()
                            .map(process->Pair.of(file.getId(), process.getId())))
                    .collect(groupingBy(Pair::getSecond, mapping(Pair::getFirst, toSet())));
            processRepository
                    .findBySubmissionEnvelope(submissionEnvelope)
                    .map(process -> Pair.of(process.getId(), process.getProtocols()))
                    .collect(groupingBy(Pair::getFirst, mapping(Pair::getSecond, toSet())));


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
            fileRepository.findByInputToProcessesContains(process).forEach(file -> this.inputFiles.add(file.getId()));
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
