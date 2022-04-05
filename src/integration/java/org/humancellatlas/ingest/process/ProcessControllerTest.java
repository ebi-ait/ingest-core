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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
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

    Protocol protocol;

    Protocol protocol2;

    Protocol protocol3;

    Project project;

    Process process;

    UriComponentsBuilder uriBuilder;

    SubmissionEnvelope submissionEnvelope;

    @BeforeEach
    void setUp() {
        submissionEnvelope = new SubmissionEnvelope(UUID.randomUUID().toString());
        submissionEnvelope.setUuid(Uuid.newUuid());
        submissionEnvelope.enactStateTransition(SubmissionState.GRAPH_VALID);
        submissionEnvelopeRepository.save(submissionEnvelope);

        protocol = new Protocol();
        protocol2 = new Protocol();
        protocol3 = new Protocol();
        protocolRepository.saveAll(Arrays.asList(protocol, protocol2, protocol3));

        project = new Project(UUID.randomUUID());
        projectRepository.save(project);

        process = new Process();
        process.setSubmissionEnvelope(submissionEnvelope);
        processRepository.save(process);

        uriBuilder = ServletUriComponentsBuilder.fromCurrentContextPath();
    }

    @Test
    public void testLinkProtocolsToProcessUsingPutMethodWithManyProtocolsInPayload() throws Exception {
        // given
        process.addProtocol(protocol);
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
                .content(uriBuilder.build().toUriString() + "/protocols/" + protocol.getId()
                        + '\n' + uriBuilder.build().toUriString() + "/protocols/" + protocol2.getId()))
                .andExpect(status().isOk());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(process);
        Process updatedProcess = processRepository.findById(process.getId()).get();
        assertThat(updatedProcess.getProtocols())
                .usingElementComparatorOnFields("id")
                .containsExactly(protocol, protocol2);
    }

    @Test
    public void testLinkProtocolsToProcessUsingPostMethodWithOneProtocolInPayload() throws Exception {
        // when
        webApp.perform(post("/processes/{processId}/protocols/", process.getId())
                .contentType("text/uri-list")
                .content(uriBuilder.build().toUriString() + "/protocols/" + protocol.getId()))
                .andExpect(status().isOk());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(process);
        Process updatedProcess = processRepository.findById(process.getId()).get();
        assertThat(updatedProcess.getProtocols())
                .usingElementComparatorOnFields("id")
                .containsExactly(protocol);
    }

    @Test
    public void testUnlinkProtocolFromProcess() throws Exception {
        // given
        process.addProtocol(protocol);
        processRepository.save(process);

        // when
        webApp.perform(delete("/processes/{processId}/protocols/{protocolId}", process.getId(), protocol.getId()))
                .andExpect(status().isNoContent());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(process);

        Process updatedProcess = processRepository.findById(process.getId()).get();
        assertThat(updatedProcess.getProtocols()).doesNotContain(protocol);
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