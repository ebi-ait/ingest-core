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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
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

    @Test
    public void testSubmissionLinkMap() {
        //given:
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope("link-map-test");

        Biomaterial donor = new Biomaterial("donor");
        Biomaterial specimen = new Biomaterial("specimen");
        Biomaterial cellSuspension = new Biomaterial("cellSuspension");

        Process process1 = new Process("donor-specimen-process");
        Process process2 = new Process("cellSuspension-sequenceFile-process");
        Process process3 = new Process("sequenceFile-analysisFile-process");

        Protocol collectionProtocol = new Protocol("collectionProtocol");
        Protocol sequencingProtocol = new Protocol("sequencingProtocol");
        Protocol analysisProtocol = new Protocol("analysisProtocol");

        File sequencingFile = new File("sequenceFile");
        File analysisFile = new File("analysisFile");

        donor.setSubmissionEnvelope(submissionEnvelope);
        specimen.setSubmissionEnvelope(submissionEnvelope);
        cellSuspension.setSubmissionEnvelope(submissionEnvelope);
        process1.setSubmissionEnvelope(submissionEnvelope);
        process2.setSubmissionEnvelope(submissionEnvelope);
        process3.setSubmissionEnvelope(submissionEnvelope);
        collectionProtocol.setSubmissionEnvelope(submissionEnvelope);
        sequencingProtocol.setSubmissionEnvelope(submissionEnvelope);
        analysisProtocol.setSubmissionEnvelope(submissionEnvelope);
        sequencingFile.setSubmissionEnvelope(submissionEnvelope);
        analysisFile.setSubmissionEnvelope(submissionEnvelope);

        specimen.addAsDerivedByProcess(process1);
        sequencingFile.addAsDerivedByProcess(process2);
        analysisFile.addAsDerivedByProcess(process3);

        donor.addAsInputToProcess(process1);
        cellSuspension.addAsInputToProcess(process2);
        sequencingFile.addAsInputToProcess(process3);

        process1.addProtocol(collectionProtocol);
        process2.addProtocol(sequencingProtocol);
        process3.addProtocol(analysisProtocol);

        submissionEnvelopeRepository.save(submissionEnvelope);
        biomaterialRepository.saveAll(List.of(donor, specimen, cellSuspension));
        protocolRepository.saveAll(List.of(collectionProtocol, sequencingProtocol, analysisProtocol));
        processRepository.saveAll(List.of(process1, process2, process3));
        fileRepository.saveAll(List.of(sequencingFile, analysisFile));

        //when:
        SubmissionLinkMapController.SubmissionLinkingMap submissionLinkMap = controller.getSubmissionLinkMap(submissionEnvelope);

        //then:
        assertThat(submissionLinkMap).isNotNull();
        assertThat(submissionLinkMap.processes.get("donor-specimen-process").protocols).isEqualTo(new HashSet<>(Arrays.asList("collectionProtocol")));
        assertThat(submissionLinkMap.processes.get("donor-specimen-process").inputBiomaterials).isEqualTo(new HashSet<>(Arrays.asList("donor")));
        assertThat(submissionLinkMap.processes.get("cellSuspension-sequenceFile-process").protocols).isEqualTo(new HashSet<>(Arrays.asList("sequencingProtocol")));
        assertThat(submissionLinkMap.processes.get("cellSuspension-sequenceFile-process").inputBiomaterials).isEqualTo(new HashSet<>(Arrays.asList("cellSuspension")));
        assertThat(submissionLinkMap.processes.get("cellSuspension-sequenceFile-process").inputFiles).isEmpty();
        assertThat(submissionLinkMap.processes.get("sequenceFile-analysisFile-process").protocols).isEqualTo(new HashSet<>(Arrays.asList("analysisProtocol")));
        assertThat(submissionLinkMap.processes.get("sequenceFile-analysisFile-process").inputBiomaterials).isEmpty();
        assertThat(submissionLinkMap.processes.get("sequenceFile-analysisFile-process").inputFiles).isEqualTo(new HashSet<>(Arrays.asList("sequenceFile")));
        assertThat(submissionLinkMap.biomaterials.get("donor").inputToProcesses).isEqualTo(new HashSet<>(Arrays.asList("donor-specimen-process")));
        assertThat(submissionLinkMap.biomaterials.get("cellSuspension").inputToProcesses).isEqualTo(new HashSet<>(Arrays.asList("cellSuspension-sequenceFile-process")));
        assertThat(submissionLinkMap.files.get("sequenceFile").inputToProcesses).isEqualTo(new HashSet<>(Arrays.asList("sequenceFile-analysisFile-process")));
    }

}
