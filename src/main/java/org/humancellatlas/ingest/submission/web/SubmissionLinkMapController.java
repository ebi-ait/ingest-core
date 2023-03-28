package org.humancellatlas.ingest.submission.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.protocol.Protocol;
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

    @Autowired
    SubmissionLinkMapRepository submissionLinkMapRepository;

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
        final Map<String, ProcessLinkingMap> processes = new Hashtable<>();
        final Map<String, UUID> protocols = new Hashtable<>();
        final Map<String, BiomaterialLinkingMap> biomaterials = new Hashtable<>();
        final Map<String, FileLinkingMap> files = new Hashtable<>();

        public SubmissionLinkingMap(SubmissionEnvelope submissionEnvelope){
            log.info("before processes");
            getProcessLinksUsingAggregation(submissionEnvelope);
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

        private void getProcessLinksUsingAggregation(SubmissionEnvelope submissionEnvelope) {
            submissionLinkMapRepository.findProcessLinkings(submissionEnvelope);
        }

        /**
         * original implementation.\
         * Very slow due to repeated calls to db
         * @param submissionEnvelope
         */
        private void processLinkingByProcess(SubmissionEnvelope submissionEnvelope) {
            processRepository
                .findBySubmissionEnvelope(submissionEnvelope)
                .forEach(process -> this.processes.put(process.getId(), new ProcessLinkingMap().fromProcess(process)));
        }

        /**
         * gets process linking map by traversing from the targets,anmely biomaterials and files.
         * This is inefficient becuae each biomaterial or file causes another call to the db to fetch the related processes.
         * @param submissionEnvelope
         */
        void getProcessLinkingFromTargets(SubmissionEnvelope submissionEnvelope) {
            log.info("processes: before biomaterials");
            biomaterialRepository.findBySubmissionEnvelope(submissionEnvelope)
                    .flatMap(biomaterial -> biomaterial.getInputToProcesses()
                            .stream()
                            .map(process -> Pair.of(biomaterial.getId(), process.getId())))
                            .collect(groupingBy(Pair::getSecond, mapping(Pair::getFirst, toSet())))
                    .forEach((processId, biomaterials)->{
                        this.processes.compute(processId,
                                (_processId, plm)-> {
                                    ProcessLinkingMap processLinkingMap = Optional.ofNullable(plm)
                                            .orElse(new ProcessLinkingMap());
                                    processLinkingMap.inputBiomaterials.addAll(biomaterials);
                                    return processLinkingMap;
                                });
                    });
            log.info("processes: before files");
            fileRepository.findBySubmissionEnvelope(submissionEnvelope)
                    .flatMap(file -> file.getInputToProcesses()
                            .stream()
                            .map(process -> Pair.of(file.getId(), process.getId())))
                    .collect(groupingBy(Pair::getSecond, mapping(Pair::getFirst, toSet())))
                    .forEach((processId, files)->{
                        this.processes.compute(processId,
                                (_processId, plm)-> {
                                    ProcessLinkingMap processLinkingMap = Optional.ofNullable(plm)
                                            .orElse(new ProcessLinkingMap());
                                    processLinkingMap.inputFiles.addAll(files);
                                    return processLinkingMap;
                                });
                    });
            log.info("processes: before protocols");
            // TODO: add index by submissionEnvelope
            processRepository
                    .findBySubmissionEnvelope(submissionEnvelope)
                    .forEach(process->{
                        this.processes.compute(process.getId(),
                                (_processId, plm)-> {
                                    ProcessLinkingMap processLinkingMap = Optional.ofNullable(plm)
                                            .orElse(new ProcessLinkingMap());
                                    processLinkingMap.protocols.addAll(process.getProtocols().stream().map(Protocol::getId).collect(toSet()));
                                    return processLinkingMap;
                                });
                    });

        }

    }

    @Getter
    @AllArgsConstructor
    public class ProcessLinkingMap {
        final Collection<String> protocols = new HashSet<>();
        final Collection<String> inputBiomaterials = new HashSet<>();
        final Collection<String> inputFiles = new HashSet<>();

        ProcessLinkingMap fromProcess(Process process) {
            process.getProtocols().forEach(protocol -> this.protocols.add(protocol.getId()));
            biomaterialRepository.findByInputToProcessesContains(process).forEach(biomaterial -> this.inputBiomaterials.add(biomaterial.getId()));
            fileRepository.findByInputToProcessesContains(process).forEach(file -> this.inputFiles.add(file.getId()));
            return this;
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
