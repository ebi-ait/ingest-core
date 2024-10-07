package uk.ac.ebi.subs.ingest.submission.web;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import uk.ac.ebi.subs.ingest.biomaterial.BiomaterialRepository;
import uk.ac.ebi.subs.ingest.core.web.Links;
import uk.ac.ebi.subs.ingest.file.FileRepository;
import uk.ac.ebi.subs.ingest.process.ProcessRepository;
import uk.ac.ebi.subs.ingest.protocol.ProtocolRepository;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

@RestController
public class SubmissionLinkMapController {

  @Autowired BiomaterialRepository biomaterialRepository;
  @Autowired FileRepository fileRepository;
  @Autowired ProcessRepository processRepository;
  @Autowired ProtocolRepository protocolRepository;

  @Autowired SubmissionLinkMapRepository submissionLinkMapRepository;

  @NonNull private final Logger log = LoggerFactory.getLogger(getClass());

  @RequestMapping(
      path = "/submissionEnvelopes/{sub_id}" + Links.SUBMISSION_LINKING_MAP_URL,
      method = RequestMethod.GET)
  @ResponseBody
  public SubmissionLinkingMap getSubmissionLinkMap(
      @PathVariable("sub_id") SubmissionEnvelope submissionEnvelope) {
    return new SubmissionLinkingMap(submissionEnvelope);
  }

  @Getter
  public class SubmissionLinkingMap {
    final Map<String, ProcessLinkingMap> processes = new Hashtable<>();
    final Map<String, UUID> protocols = new Hashtable<>();
    final Map<String, BiomaterialLinkingMap> biomaterials = new Hashtable<>();
    final Map<String, FileLinkingMap> files = new Hashtable<>();

    public SubmissionLinkingMap(SubmissionEnvelope submissionEnvelope) {
      log.info("before processes");
      getProcessLinksUsingAggregation(submissionEnvelope);
      log.info("found {} processes", processes.size());
      log.info("before biomaterials");
      findBiomaterialsLinkedProcessesForSubmission(submissionEnvelope);
      log.info("found {} biomaterials", biomaterials.size());
      log.info("before files");
      findFilesLinkedProcessesForSubmission(submissionEnvelope);
      log.info("found {} files", files.size());
    }

    private void findBiomaterialsLinkedProcessesForSubmission(
        SubmissionEnvelope submissionEnvelope) {
      submissionLinkMapRepository
          .findLinkedProcessesByEntityTypeAndSubmission(submissionEnvelope, "biomaterial")
          .forEach(
              bioMaterialsAndProcesses ->
                  this.biomaterials.compute(
                      bioMaterialsAndProcesses.entityId,
                      (_processId, plm) -> {
                        BiomaterialLinkingMap biomaterialLinkingMap =
                            Optional.ofNullable(plm).orElse(new BiomaterialLinkingMap());
                        biomaterialLinkingMap.inputToProcesses.addAll(
                            bioMaterialsAndProcesses.inputToProcesses);
                        biomaterialLinkingMap.derivedByProcesses.addAll(
                            bioMaterialsAndProcesses.derivedByProcesses);
                        return biomaterialLinkingMap;
                      }));
    }

    private void findFilesLinkedProcessesForSubmission(SubmissionEnvelope submissionEnvelope) {
      submissionLinkMapRepository
          .findLinkedProcessesByEntityTypeAndSubmission(submissionEnvelope, "file")
          .forEach(
              filesAndProcesses ->
                  this.files.compute(
                      filesAndProcesses.entityId,
                      (_processId, plm) -> {
                        FileLinkingMap fileLinkingMap =
                            Optional.ofNullable(plm).orElse(new FileLinkingMap());
                        fileLinkingMap.inputToProcesses.addAll(filesAndProcesses.inputToProcesses);
                        fileLinkingMap.derivedByProcesses.addAll(
                            filesAndProcesses.derivedByProcesses);
                        return fileLinkingMap;
                      }));
    }

    private void getProcessLinksUsingAggregation(SubmissionEnvelope submissionEnvelope) {
      submissionLinkMapRepository
          .findProcessInputBiomaterials(submissionEnvelope)
          .forEach(
              processAndInputBiomaterials ->
                  this.processes.compute(
                      processAndInputBiomaterials.processId,
                      (_processId, plm) -> {
                        ProcessLinkingMap processLinkingMap =
                            Optional.ofNullable(plm).orElse(new ProcessLinkingMap());
                        processLinkingMap.inputBiomaterials.addAll(
                            processAndInputBiomaterials.inputBiomaterials);
                        return processLinkingMap;
                      }));
      submissionLinkMapRepository
          .findProcessInputFiles(submissionEnvelope)
          .forEach(
              (ProcessAndInputFiles processAndInputFiles) ->
                  this.processes.compute(
                      processAndInputFiles.processId,
                      (_processId, plm) -> {
                        ProcessLinkingMap processLinkingMap =
                            Optional.ofNullable(plm).orElse(new ProcessLinkingMap());
                        processLinkingMap.inputFiles.addAll(processAndInputFiles.inputFiles);
                        return processLinkingMap;
                      }));
      log.info("processes: before protocols");
      submissionLinkMapRepository
          .findProcessProtocols(submissionEnvelope)
          .forEach(
              process ->
                  this.processes.compute(
                      process.entityId,
                      (_processId, plm) -> {
                        ProcessLinkingMap processLinkingMap =
                            Optional.ofNullable(plm).orElse(new ProcessLinkingMap());
                        processLinkingMap.protocols.addAll(process.protocols);
                        return processLinkingMap;
                      }));
      log.info("processes: finished protocols");
    }
  }

  @Getter
  @AllArgsConstructor
  public static class ProcessLinkingMap {
    final Collection<String> protocols = new HashSet<>();
    final Collection<String> inputBiomaterials = new HashSet<>();
    final Collection<String> inputFiles = new HashSet<>();
  }

  @Getter
  @NoArgsConstructor
  public static class BiomaterialLinkingMap {
    final Collection<String> derivedByProcesses = new HashSet<>();
    final Collection<String> inputToProcesses = new HashSet<>();
  }

  @Getter
  @NoArgsConstructor
  public static class FileLinkingMap {
    final Collection<String> derivedByProcesses = new HashSet<>();
    final Collection<String> inputToProcesses = new HashSet<>();
  }
}
