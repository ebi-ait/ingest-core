package uk.ac.ebi.subs.ingest.submission.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;

import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.repository.MongoRepository;

import uk.ac.ebi.subs.ingest.biomaterial.Biomaterial;
import uk.ac.ebi.subs.ingest.biomaterial.BiomaterialRepository;
import uk.ac.ebi.subs.ingest.config.MigrationConfiguration;
import uk.ac.ebi.subs.ingest.file.File;
import uk.ac.ebi.subs.ingest.file.FileRepository;
import uk.ac.ebi.subs.ingest.messaging.MessageRouter;
import uk.ac.ebi.subs.ingest.process.Process;
import uk.ac.ebi.subs.ingest.process.ProcessRepository;
import uk.ac.ebi.subs.ingest.protocol.Protocol;
import uk.ac.ebi.subs.ingest.protocol.ProtocolRepository;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelopeRepository;

@Ignore(
    "ignoring because $toString mongo aggregation operator is not supported by the in memory mongo version we use. See dcp-936")
@SpringBootTest
@AutoConfigureDataMongo()
public class SubmissionLinkMapControllerTest {
  @Autowired private SubmissionLinkMapController controller;

  @Autowired BiomaterialRepository biomaterialRepository;

  @Autowired FileRepository fileRepository;

  @Autowired ProcessRepository processRepository;

  @Autowired ProtocolRepository protocolRepository;

  @Autowired SubmissionEnvelopeRepository submissionEnvelopeRepository;

  @MockBean private MigrationConfiguration migrationConfiguration;

  @MockBean private MessageRouter messageRouter;

  @AfterEach
  private void tearDown() {
    List.of(
            biomaterialRepository,
            fileRepository,
            processRepository,
            protocolRepository,
            submissionEnvelopeRepository)
        .forEach(MongoRepository::deleteAll);
  }

  /**
   * @Ignore("ignoring because $toString mongo aggregation operator is not supported by the in
   * memory mongo version we use. See dcp-936")
   */
  public void testSubmissionLinkMap() {
    // given:
    SubmissionEnvelope submissionEnvelope =
        submissionEnvelopeRepository.save(new SubmissionEnvelope());

    Biomaterial donor = biomaterialRepository.save(new Biomaterial(null));
    Biomaterial specimen = biomaterialRepository.save(new Biomaterial(null));
    Biomaterial cellSuspension = biomaterialRepository.save(new Biomaterial(null));

    Process donorSpecimen = processRepository.save(new Process(null));
    Process cellSuspensionSequenceFile = processRepository.save(new Process(null));
    Process sequenceFileAnalysisFile = processRepository.save(new Process(null));

    Protocol collectionProtocol = protocolRepository.save(new Protocol(null));
    Protocol sequencingProtocol = protocolRepository.save(new Protocol(null));
    Protocol analysisProtocol = protocolRepository.save(new Protocol(null));

    File sequencingFile = fileRepository.save(new File(null, "sequenceFile"));
    File analysisFile = fileRepository.save(new File(null, "analysisFile"));

    List.of(
            donor,
            specimen,
            cellSuspension,
            donorSpecimen,
            cellSuspensionSequenceFile,
            sequenceFileAnalysisFile,
            collectionProtocol,
            sequencingProtocol,
            analysisProtocol,
            sequencingFile,
            analysisFile)
        .forEach(entity -> entity.setSubmissionEnvelope(submissionEnvelope));

    specimen.addAsDerivedByProcess(donorSpecimen);
    sequencingFile.addAsDerivedByProcess(cellSuspensionSequenceFile);
    analysisFile.addAsDerivedByProcess(sequenceFileAnalysisFile);

    donor.addAsInputToProcess(donorSpecimen);
    cellSuspension.addAsInputToProcess(cellSuspensionSequenceFile);
    sequencingFile.addAsInputToProcess(sequenceFileAnalysisFile);

    donorSpecimen.addProtocol(collectionProtocol);
    cellSuspensionSequenceFile.addProtocol(sequencingProtocol);
    sequenceFileAnalysisFile.addProtocol(analysisProtocol);

    submissionEnvelopeRepository.save(submissionEnvelope);
    biomaterialRepository.saveAll(List.of(donor, specimen, cellSuspension));
    protocolRepository.saveAll(List.of(collectionProtocol, sequencingProtocol, analysisProtocol));
    processRepository.saveAll(
        List.of(donorSpecimen, cellSuspensionSequenceFile, sequenceFileAnalysisFile));
    fileRepository.saveAll(List.of(sequencingFile, analysisFile));

    // when:
    SubmissionLinkMapController.SubmissionLinkingMap submissionLinkMap =
        controller.getSubmissionLinkMap(submissionEnvelope);

    // then:
    assertThat(submissionLinkMap).isNotNull();
    assertThat(submissionLinkMap.processes.get(donorSpecimen.getId()).protocols)
        .isEqualTo(new HashSet<>(List.of(collectionProtocol.getId())));
    assertThat(submissionLinkMap.processes.get(donorSpecimen.getId()).inputBiomaterials)
        .isEqualTo(new HashSet<>(List.of(donor.getId())));
    assertThat(submissionLinkMap.processes.get(cellSuspensionSequenceFile.getId()).protocols)
        .isEqualTo(new HashSet<>(List.of(sequencingProtocol.getId())));
    assertThat(
            submissionLinkMap.processes.get(cellSuspensionSequenceFile.getId()).inputBiomaterials)
        .isEqualTo(new HashSet<>(List.of(cellSuspension.getId())));
    assertThat(submissionLinkMap.processes.get(cellSuspensionSequenceFile.getId()).inputFiles)
        .isEmpty();
    assertThat(submissionLinkMap.processes.get(sequenceFileAnalysisFile.getId()).protocols)
        .isEqualTo(new HashSet<>(List.of(analysisProtocol.getId())));
    assertThat(submissionLinkMap.processes.get(sequenceFileAnalysisFile.getId()).inputBiomaterials)
        .isEmpty();
    assertThat(submissionLinkMap.processes.get(sequenceFileAnalysisFile.getId()).inputFiles)
        .isEqualTo(new HashSet<>(List.of(sequencingFile.getId())));
    assertThat(submissionLinkMap.biomaterials.get(donor.getId()).inputToProcesses)
        .isEqualTo(new HashSet<>(List.of(donorSpecimen.getId())));
    assertThat(submissionLinkMap.biomaterials.get(cellSuspension.getId()).inputToProcesses)
        .isEqualTo(new HashSet<>(List.of(cellSuspensionSequenceFile.getId())));
    assertThat(submissionLinkMap.files.get(sequencingFile.getId()).inputToProcesses)
        .isEqualTo(new HashSet<>(List.of(sequenceFileAnalysisFile.getId())));
  }
}
