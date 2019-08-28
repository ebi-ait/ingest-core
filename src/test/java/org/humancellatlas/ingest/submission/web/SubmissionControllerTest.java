package org.humancellatlas.ingest.submission.web;

import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.export.Exporter;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.process.ProcessService;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeService;
import org.humancellatlas.ingest.submission.SubmissionStateMachineService;
import org.humancellatlas.ingest.submissionmanifest.SubmissionManifestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.humancellatlas.ingest.state.SubmissionState.SUBMITTED;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes={ SubmissionController.class })
public class SubmissionControllerTest {

    @Autowired
    private SubmissionController controller;

    @MockBean
    private Exporter exporter;

    @MockBean
    private SubmissionEnvelopeService submissionEnvelopeService;
    @MockBean
    private ProcessService processService;

    @MockBean
    private SubmissionEnvelopeRepository submissionEnvelopeRepository;
    @MockBean
    private FileRepository fileRepository;
    @MockBean
    private ProjectRepository projectRepository;
    @MockBean
    private ProtocolRepository protocolRepository;
    @MockBean
    private BiomaterialRepository biomaterialRepository;
    @MockBean
    private ProcessRepository processRepository;
    @MockBean
    private BundleManifestRepository bundleManifestRepository;
    @MockBean
    private SubmissionManifestRepository submissionManifestRepository;
    @MockBean
    private PagedResourcesAssembler pagedResourcesAssembler;
    @MockBean
    private SubmissionStateMachineService submissionStateMachineService;

    @Test
    public void testEnactSubmitEnvelope() {
        //given:
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        assertThat(submissionEnvelope.getSubmissionState()).isNotEqualTo(SUBMITTED);

        //and:
        PersistentEntityResourceAssembler resourceAssembler =
                mock(PersistentEntityResourceAssembler.class);

        //when:
        HttpEntity<?> response = controller.enactSubmitEnvelope(submissionEnvelope,
                resourceAssembler);

        //then:
        assertThat(response).isNotNull();
        assertThat(submissionEnvelope.getSubmissionState()).isEqualTo(SUBMITTED);
        verify(submissionEnvelopeRepository).save(submissionEnvelope);
        verify(submissionEnvelopeService).handleSubmissionRequest(submissionEnvelope);
    }

    @Configuration
    static class TestConfiguration {}

}
