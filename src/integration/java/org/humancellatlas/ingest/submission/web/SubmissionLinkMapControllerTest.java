package org.humancellatlas.ingest.submission.web;

import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureDataMongo()
public class SubmissionLinkMapControllerTest {
    @Autowired
    private SubmissionLinkMapController controller;

    @Autowired
    BiomaterialRepository biomaterialRepository;

    @Autowired
    FileRepository fileRepository;

    @Autowired
    ProcessRepository processRepository;

    @Autowired
    ProtocolRepository protocolRepository;

    @Autowired
    SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @MockBean
    private MigrationConfiguration migrationConfiguration;

    @MockBean
    private MessageRouter messageRouter;

    @AfterEach
    private void tearDown() {
        biomaterialRepository.deleteAll();
        fileRepository.deleteAll();
        processRepository.deleteAll();
        protocolRepository.deleteAll();
        submissionEnvelopeRepository.deleteAll();
    }

    @Test
    public void testSubmissionLinkMap() {
        //given:
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope("link-map-test");

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

        donor.setSubmissionEnvelope(submissionEnvelope);
        specimen.setSubmissionEnvelope(submissionEnvelope);
        cellSuspension.setSubmissionEnvelope(submissionEnvelope);
        donorSpecimen.setSubmissionEnvelope(submissionEnvelope);
        cellSuspensionSequenceFile.setSubmissionEnvelope(submissionEnvelope);
        sequenceFileAnalysisFile.setSubmissionEnvelope(submissionEnvelope);
        collectionProtocol.setSubmissionEnvelope(submissionEnvelope);
        sequencingProtocol.setSubmissionEnvelope(submissionEnvelope);
        analysisProtocol.setSubmissionEnvelope(submissionEnvelope);
        sequencingFile.setSubmissionEnvelope(submissionEnvelope);
        analysisFile.setSubmissionEnvelope(submissionEnvelope);

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
        processRepository.saveAll(List.of(donorSpecimen, cellSuspensionSequenceFile, sequenceFileAnalysisFile));
        fileRepository.saveAll(List.of(sequencingFile, analysisFile));

        //when:
        SubmissionLinkMapController.SubmissionLinkingMap submissionLinkMap = controller.getSubmissionLinkMap(submissionEnvelope);

        //then:
        assertThat(submissionLinkMap).isNotNull();
        assertThat(submissionLinkMap.processes.get(donorSpecimen.getId()).protocols).isEqualTo(new HashSet<>(List.of(collectionProtocol.getId())));
        assertThat(submissionLinkMap.processes.get(donorSpecimen.getId()).inputBiomaterials).isEqualTo(new HashSet<>(List.of(donor.getId())));
        assertThat(submissionLinkMap.processes.get(cellSuspensionSequenceFile.getId()).protocols).isEqualTo(new HashSet<>(List.of(sequencingProtocol.getId())));
        assertThat(submissionLinkMap.processes.get(cellSuspensionSequenceFile.getId()).inputBiomaterials).isEqualTo(new HashSet<>(List.of(cellSuspension.getId())));
        assertThat(submissionLinkMap.processes.get(cellSuspensionSequenceFile.getId()).inputFiles).isEmpty();
        assertThat(submissionLinkMap.processes.get(sequenceFileAnalysisFile.getId()).protocols).isEqualTo(new HashSet<>(List.of(analysisProtocol.getId())));
        assertThat(submissionLinkMap.processes.get(sequenceFileAnalysisFile.getId()).inputBiomaterials).isEmpty();
        assertThat(submissionLinkMap.processes.get(sequenceFileAnalysisFile.getId()).inputFiles).isEqualTo(new HashSet<>(List.of(sequencingFile.getId())));
        assertThat(submissionLinkMap.biomaterials.get(donor.getId()).inputToProcesses).isEqualTo(new HashSet<>(List.of(donorSpecimen.getId())));
        assertThat(submissionLinkMap.biomaterials.get(cellSuspension.getId()).inputToProcesses).isEqualTo(new HashSet<>(List.of(cellSuspensionSequenceFile.getId())));
        assertThat(submissionLinkMap.files.get(sequencingFile.getId()).inputToProcesses).isEqualTo(new HashSet<>(List.of(sequenceFileAnalysisFile.getId())));
    }

}
