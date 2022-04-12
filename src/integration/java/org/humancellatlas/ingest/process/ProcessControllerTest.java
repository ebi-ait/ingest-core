package org.humancellatlas.ingest.process;

import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.ValidationStateChangeService;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureDataMongo()
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class ProcessControllerTest {
    @MockBean
    ValidationStateChangeService validationStateChangeService;

    @MockBean
    private MessageRouter messageRouter;

    @MockBean
    private MigrationConfiguration migrationConfiguration;

    @Autowired
    private MockMvc webApp;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private ProtocolRepository protocolRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SubmissionEnvelopeRepository submissionEnvelopeRepository;

    Protocol protocol1;

    Protocol protocol2;

    Protocol protocol3;

    Project project;

    Process process;

    UriComponentsBuilder uriBuilder;

    SubmissionEnvelope submissionEnvelope;

    @BeforeEach
    void setUp() {
        submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.setUuid(Uuid.newUuid());
        submissionEnvelope.enactStateTransition(SubmissionState.GRAPH_VALID);
        submissionEnvelope = submissionEnvelopeRepository.save(submissionEnvelope);

        protocol1 = protocolRepository.save(new Protocol(null));
        protocol2 = protocolRepository.save(new Protocol(null));
        protocol3 = protocolRepository.save(new Protocol(null));

        project = projectRepository.save(new Project(null));

        process = new Process(null);
        process.setSubmissionEnvelope(submissionEnvelope);
        process = processRepository.save(process);

        uriBuilder = ServletUriComponentsBuilder.fromCurrentContextPath();
    }

    @AfterEach
    private void tearDown() {
        submissionEnvelopeRepository.deleteAll();
        processRepository.deleteAll();
        protocolRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    public void testLinkProtocolsToProcessUsingPutMethodWithManyProtocolsInPayload() throws Exception {
        // given
        process.addProtocol(protocol1);
        processRepository.save(process);

        // when
        webApp.perform(put("/processes/{id}/protocols/", process.getId())
                .contentType("text/uri-list")
                .content(uriBuilder.build().toUriString() + "/protocols/" + protocol2.getId()
                    + '\n' + uriBuilder.build().toUriString() + "/protocols/" + protocol3.getId()))
            .andExpect(status().isOk());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(process);
        Process updatedProcess = processRepository.findById(process.getId()).get();
        assertThat(updatedProcess.getProtocols())
            .usingElementComparatorOnFields("id")
            .containsExactly(protocol2, protocol3);
    }

    @Test
    public void testLinkProtocolsToProcessUsingPostMethodWithManyProtocolsInPayload() throws Exception {
        // when
        webApp.perform(post("/processes/{id}/protocols/", process.getId())
                .contentType("text/uri-list")
                .content(uriBuilder.build().toUriString() + "/protocols/" + protocol1.getId()
                    + '\n' + uriBuilder.build().toUriString() + "/protocols/" + protocol2.getId()))
            .andExpect(status().isOk());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(process);
        Process updatedProcess = processRepository.findById(process.getId()).get();
        assertThat(updatedProcess.getProtocols())
            .usingElementComparatorOnFields("id")
            .containsExactly(protocol1, protocol2);
    }

    @Test
    public void testLinkProtocolsToProcessUsingPostMethodWithOneProtocolInPayload() throws Exception {
        // when
        webApp.perform(post("/processes/{processId}/protocols/", process.getId())
                .contentType("text/uri-list")
                .content(uriBuilder.build().toUriString() + "/protocols/" + protocol1.getId()))
            .andExpect(status().isOk());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(process);
        Process updatedProcess = processRepository.findById(process.getId()).get();
        assertThat(updatedProcess.getProtocols())
            .usingElementComparatorOnFields("id")
            .containsExactly(protocol1);
    }

    @Test
    public void testUnlinkProtocolFromProcess() throws Exception {
        // given
        process.addProtocol(protocol1);
        processRepository.save(process);

        // when
        webApp.perform(delete("/processes/{processId}/protocols/{protocolId}", process.getId(), protocol1.getId()))
            .andExpect(status().isNoContent());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(process);

        Process updatedProcess = processRepository.findById(process.getId()).get();
        assertThat(updatedProcess.getProtocols()).doesNotContain(protocol1);
    }

    @Test
    public void testLinkProjectToProcessDoesNotChangeTheirValidationStatesToDraft() throws Exception {
        webApp.perform(put("/processes/{processId}/project", process.getId())
                .contentType("text/uri-list")
                .content(uriBuilder.build().toUriString() + "/projects/" + project.getId()))
            .andExpect(status().isNoContent());

        verify(validationStateChangeService, times(0)).changeValidationState(any(), any(), eq(ValidationState.DRAFT));
    }

    @Test
    public void testUnlinkProjectFromProcessDoesNotChangeTheirValidationStatesToDraft() throws Exception {
        webApp.perform(delete("/processes/{processId}/project/{projectId}", process.getId(), project.getId()))
            .andExpect(status().isNoContent());

        verify(validationStateChangeService, times(0)).changeValidationState(any(), any(), eq(ValidationState.DRAFT));
    }

    private void verifyThatValidationStateChangedToDraftWhenGraphValid(MetadataDocument... values) {
        Arrays.stream(values).forEach(value -> {
            verify(validationStateChangeService, times(1)).changeValidationState(value.getType(), value.getId(), ValidationState.DRAFT);
        });
    }
}