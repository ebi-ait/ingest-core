package org.humancellatlas.ingest.submission.web;

import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {SubmissionLinkMapController.class})
public class SubmissionLinkMapControllerTest {
    @Autowired
    private SubmissionLinkMapController controller;

    @MockBean
    BiomaterialRepository biomaterialRepository;

    @MockBean
    FileRepository fileRepository;

    @MockBean
    ProcessRepository processRepository;

    @MockBean
    ProtocolRepository protocolRepository;


    @Test
    public void testSubmissionLinkMap() {
        //given:
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();

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

        specimen.addAsDerivedByProcess(process1);
        sequencingFile.addAsDerivedByProcess(process2);
        analysisFile.addAsDerivedByProcess(process3);

        donor.addAsInputToProcess(process1);
        cellSuspension.addAsInputToProcess(process2);
        sequencingFile.addAsInputToProcess(process3);

        process1.addProtocol(collectionProtocol);
        process2.addProtocol(sequencingProtocol);
        process3.addProtocol(analysisProtocol);

        when(processRepository.findBySubmissionEnvelope(submissionEnvelope)).thenReturn(Stream.of(process1, process2, process3));
        when(biomaterialRepository.findBySubmissionEnvelope(submissionEnvelope)).thenReturn(Stream.of(donor, specimen, cellSuspension));
        when(fileRepository.findBySubmissionEnvelope(submissionEnvelope)).thenReturn(Stream.of(sequencingFile, analysisFile));

        when(biomaterialRepository.findByInputToProcessesContains(process1)).thenReturn(Stream.of(donor));
        when(biomaterialRepository.findByInputToProcessesContains(process2)).thenReturn(Stream.of(cellSuspension));
        when(biomaterialRepository.findByInputToProcessesContains(process3)).thenReturn(Stream.of());

        when(fileRepository.findByInputToProcessesContains(process1)).thenReturn(Stream.of());
        when(fileRepository.findByInputToProcessesContains(process2)).thenReturn(Stream.of());
        when(fileRepository.findByInputToProcessesContains(process3)).thenReturn(Stream.of(sequencingFile));

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
