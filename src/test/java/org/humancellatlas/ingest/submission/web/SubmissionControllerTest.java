package org.humancellatlas.ingest.submission.web;

import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.exporter.Exporter;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.process.ProcessService;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.protocol.ProtocolService;
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
import static org.humancellatlas.ingest.state.SubmissionState.*;
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
    private ProtocolService protocolService;

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
    @MockBean
    private MessageRouter messageRouter;

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
        verify(submissionEnvelopeService).handleCommitSubmit(submissionEnvelope);
    }

    @Test
    public void testDeleteSubmissionEnvelopeWithoutForce() {
        //given:
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();

        //when:
        HttpEntity<?> response = controller.forceDeleteSubmission(submissionEnvelope, false);

        //then:
        assertThat(response).isNotNull();
        verify(submissionEnvelopeService).deleteSubmission(submissionEnvelope, false);
    }

    @Test
    public void testDeleteSubmissionEnvelopeWithForce() {
        //given:
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();

        //when:
        HttpEntity<?> response = controller.forceDeleteSubmission(submissionEnvelope, true);

        //then:
        assertThat(response).isNotNull();
        verify(submissionEnvelopeService).deleteSubmission(submissionEnvelope, true);
    }

    @Test
    public void testDraftStateTransition() {
        //given:
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.enactStateTransition(GRAPH_VALID);
        assertThat(submissionEnvelope.getSubmissionState()).isEqualTo(GRAPH_VALID);

        //and:
        PersistentEntityResourceAssembler resourceAssembler =
                mock(PersistentEntityResourceAssembler.class);

        // When:
        HttpEntity<?> response = controller.enactDraftEnvelope(submissionEnvelope,
                resourceAssembler);

        //then:
        assertThat(response).isNotNull();
        assertThat(submissionEnvelope.getSubmissionState()).isEqualTo(DRAFT);
    }

    @Test
    public void testHappyValidationPath() {
        //given:
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.enactStateTransition(DRAFT);
        assertThat(submissionEnvelope.getSubmissionState()).isEqualTo(DRAFT);

        //and:
        PersistentEntityResourceAssembler resourceAssembler =
                mock(PersistentEntityResourceAssembler.class);

        // Test metadata validation happy path
        // Metadata validation is triggered when documents are added to the submission
        // so no endpoints fro requesting the state tracker to change the submission state

        // draft -> metadata validating
        HttpEntity<?> response = controller.enactValidatingEnvelope(submissionEnvelope, resourceAssembler);
        assertThat(response).isNotNull();
        assertThat(submissionEnvelope.getSubmissionState()).isEqualTo(METADATA_VALIDATING);

        // metadata validating -> metadata valid
        response = controller.enactValidEnvelope(submissionEnvelope, resourceAssembler);
        assertThat(response).isNotNull();
        assertThat(submissionEnvelope.getSubmissionState()).isEqualTo(METADATA_VALID);


        // Test graph validation happy path
        // endpoints for requesting the state tracker to change the submission state are used here

        // metadata valid -> graph validation requested
        HttpEntity<?> requestResponse = controller.requestGraphValidation(submissionEnvelope, resourceAssembler);
        assertThat(requestResponse).isNotNull();
        verify(submissionEnvelopeService).handleEnvelopeStateUpdateRequest(submissionEnvelope, GRAPH_VALIDATION_REQUESTED);
        HttpEntity<?> enactResponse = controller.enactGraphValidationRequested(submissionEnvelope, resourceAssembler);
        assertThat(enactResponse).isNotNull();
        assertThat(submissionEnvelope.getSubmissionState()).isEqualTo(GRAPH_VALIDATION_REQUESTED);

        // graph validation requested -> graph validating
        requestResponse = controller.requestGraphValidating(submissionEnvelope, resourceAssembler);
        assertThat(requestResponse).isNotNull();
        verify(submissionEnvelopeService).handleEnvelopeStateUpdateRequest(submissionEnvelope, GRAPH_VALIDATING);
        enactResponse = controller.enactGraphValidating(submissionEnvelope, resourceAssembler);
        assertThat(enactResponse).isNotNull();
        assertThat(submissionEnvelope.getSubmissionState()).isEqualTo(GRAPH_VALIDATING);

        // graph validating -> graph valid
        requestResponse = controller.requestGraphValid(submissionEnvelope, resourceAssembler);
        assertThat(requestResponse).isNotNull();
        verify(submissionEnvelopeService).handleEnvelopeStateUpdateRequest(submissionEnvelope, GRAPH_VALID);
        enactResponse = controller.enactGraphValid(submissionEnvelope, resourceAssembler);
        assertThat(enactResponse).isNotNull();
        assertThat(submissionEnvelope.getSubmissionState()).isEqualTo(GRAPH_VALID);
    }

    @Configuration
    static class TestConfiguration {}

}
