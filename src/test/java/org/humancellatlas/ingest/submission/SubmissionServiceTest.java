package org.humancellatlas.ingest.submission;

import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.export.Exporter;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.patch.PatchRepository;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.submissionmanifest.SubmissionManifestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes={ SubmissionEnvelopeService.class })
public class SubmissionServiceTest {
    @Autowired
    private SubmissionEnvelopeService service;

    @MockBean
    private MessageRouter messageRouter;

    @MockBean
    private Exporter exporter;

    @MockBean
    private MetadataUpdateService metadataUpdateService;

    @MockBean
    private ExecutorService executorService;

    @MockBean
    private SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @MockBean
    private SubmissionEnvelopeCreateHandler submissionEnvelopeCreateHandler;

    @MockBean
    private SubmissionManifestRepository submissionManifestRepository;

    @MockBean
    private BundleManifestRepository bundleManifestRepository;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private ProcessRepository processRepository;

    @MockBean
    private ProtocolRepository protocolRepository;

    @MockBean
    private FileRepository fileRepository;

    @MockBean
    private BiomaterialRepository biomaterialRepository;

    @MockBean
    private PatchRepository patchRepository;

    @Configuration
    static class TestConfiguration {}

    static class TestProject extends Project {
        Set<File> supplementaryFiles;

        TestProject(Object content) {
            super(content);
            supplementaryFiles = super.getSupplementaryFiles();
        }

        @Override
        public Set<File> getSupplementaryFiles() {
            return supplementaryFiles;
        }

        public void addToSupplementaryFiles(File file) {
            supplementaryFiles.add(file);
        }
    }

    @Test
    public void testDeleteSubmission() {
        //given SubmissionEnvelope
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.setUuid(Uuid.newUuid());

        //given metadata within the SubmissionEnvelope
        Biomaterial testBiomaterial = new Biomaterial(Map.ofEntries(Map.entry("key", UUID.randomUUID())));
        Protocol testProtocol = new Protocol(Map.ofEntries(Map.entry("key", UUID.randomUUID())));
        Process testProcess = new Process(Map.ofEntries(Map.entry("key", UUID.randomUUID())));

        testProcess.setSubmissionEnvelope(submissionEnvelope);
        testProtocol.setSubmissionEnvelope(submissionEnvelope);
        testBiomaterial.setSubmissionEnvelope(submissionEnvelope);

        //given metadata outside the SubmissionEnvelope
        Biomaterial testOutsideBiomaterial = new Biomaterial(Map.ofEntries(Map.entry("key", UUID.randomUUID())));
        File testOutsideFile = new File(Map.ofEntries(Map.entry("key", UUID.randomUUID())));
        Process testOutsideProcess = new Process(Map.ofEntries(Map.entry("key", UUID.randomUUID())));

        //given links to metadata outside of the SubmissionEnvelope
        testOutsideBiomaterial.getInputToProcesses().add(testProcess);
        testOutsideFile.getDerivedByProcesses().add(testProcess);
        testOutsideProcess.getProtocols().add(testProtocol);

        //given File
        File file = new File();
        file.setFileName("testFile.txt");
        file.setSubmissionEnvelope(submissionEnvelope);

        //given Project
        TestProject project = new TestProject(new Object());
        project.setUuid(Uuid.newUuid());
        project.setSubmissionEnvelope(submissionEnvelope);
        project.addToSubmissionEnvelope(submissionEnvelope);
        assertThat(project.getSubmissionEnvelopes()).contains(submissionEnvelope);
        assertThat(project.getSubmissionEnvelope()).isEqualTo(submissionEnvelope);

        //given SupplementaryFile
        project.addToSupplementaryFiles(file);
        assertThat(project.getSupplementaryFiles()).contains(file);

        //given ProjectRepository
        List<Project> projectList = new ArrayList<>();
        projectList.add(project);
        when(projectRepository.findBySubmissionEnvelope(any(), any()))
                .thenReturn(new PageImpl<>(projectList, Pageable.unpaged(), 1));

        //when
        when(processRepository.findBySubmissionEnvelope(submissionEnvelope)).thenReturn(Stream.of(testProcess));
        when(biomaterialRepository.findBySubmissionEnvelope(submissionEnvelope)).thenReturn(Stream.of(testBiomaterial));
        when(protocolRepository.findBySubmissionEnvelope(submissionEnvelope)).thenReturn(Stream.of(testProtocol));

        when(biomaterialRepository.findByInputToProcessesContains(testProcess)).thenReturn(Stream.of(testOutsideBiomaterial));
        when(fileRepository.findByDerivedByProcessesContains(testProcess)).thenReturn(Stream.of(testOutsideFile));
        when(processRepository.findByProtocolsContains(testProtocol)).thenReturn(Stream.of(testOutsideProcess));

        service.deleteSubmission(submissionEnvelope, false);

        //then:
        assertThat(testOutsideBiomaterial.getInputToProcesses()).doesNotContain(testProcess);
        assertThat(testOutsideFile.getDerivedByProcesses()).doesNotContain(testProcess);
        assertThat(testOutsideProcess.getProtocols()).doesNotContain(testProtocol);

        verify(biomaterialRepository).deleteBySubmissionEnvelope(submissionEnvelope);
        verify(processRepository).deleteBySubmissionEnvelope(submissionEnvelope);
        verify(protocolRepository).deleteBySubmissionEnvelope(submissionEnvelope);
        verify(fileRepository).deleteBySubmissionEnvelope(submissionEnvelope);
        verify(bundleManifestRepository).deleteByEnvelopeUuid(submissionEnvelope.getUuid().getUuid().toString());
        verify(patchRepository).deleteBySubmissionEnvelope(submissionEnvelope);
        verify(submissionManifestRepository).deleteBySubmissionEnvelope(submissionEnvelope);

        verify(projectRepository).findBySubmissionEnvelope(submissionEnvelope, Pageable.unpaged());
        assertThat(project.getSubmissionEnvelopes()).isEmpty();
        assertThat(project.getSupplementaryFiles()).isEmpty();
        verify(projectRepository).save(project);
        verify(submissionEnvelopeRepository).delete(submissionEnvelope);
    }
}
