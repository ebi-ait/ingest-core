package org.humancellatlas.ingest.submission;

import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.export.Exporter;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.patch.PatchRepository;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

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

    @Test
    public void testDeleteSubmission() {
        //given SubmissionEnvelope:
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.setUuid(Uuid.newUuid());

        //given Project
        Project project = new Project(new Object());
        project.setUuid(Uuid.newUuid());
        project.setSubmissionEnvelope(submissionEnvelope);
        project.addToSubmissionEnvelope(submissionEnvelope);
        assertThat(project.getSubmissionEnvelopes()).isNotEmpty();

        //given ProjectRepository
        List<Project> projectList = new ArrayList<>();
        projectList.add(project);
        when(projectRepository.findBySubmissionEnvelope(any(), any()))
                .thenReturn(new PageImpl<>(projectList, Pageable.unpaged(), 1));

        //when
        service.deleteSubmission(submissionEnvelope);

        //then:
        verify(biomaterialRepository).deleteBySubmissionEnvelope(submissionEnvelope);
        verify(processRepository).deleteBySubmissionEnvelope(submissionEnvelope);
        verify(protocolRepository).deleteBySubmissionEnvelope(submissionEnvelope);
        verify(fileRepository).deleteBySubmissionEnvelope(submissionEnvelope);
        verify(bundleManifestRepository).deleteByEnvelopeUuid(submissionEnvelope.getUuid().getUuid().toString());
        verify(patchRepository).deleteBySubmissionEnvelope(submissionEnvelope);
        verify(submissionManifestRepository).deleteBySubmissionEnvelope(submissionEnvelope);

        verify(projectRepository).findBySubmissionEnvelope(submissionEnvelope, Pageable.unpaged());
        assertThat(project.getSubmissionEnvelopes()).isEmpty();
        verify(projectRepository).save(project);
        verify(submissionEnvelopeRepository).delete(submissionEnvelope);
    }
}
