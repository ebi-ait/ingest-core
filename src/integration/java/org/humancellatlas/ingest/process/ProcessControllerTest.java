package org.humancellatlas.ingest.process;

import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.service.ValidationStateChangeService;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.state.ValidationState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

    Protocol protocol;

    Project project;

    Process process;

    @BeforeEach
    void setUp() {
        protocol = new Protocol(UUID.randomUUID());
        protocolRepository.save(protocol);

        project = new Project(UUID.randomUUID());
        projectRepository.save(project);

        // and
        process = new Process(UUID.randomUUID());
        processRepository.save(process);

    }


    @Test
    public void testDeleteProtocolTriggersValidationStateToDraft() throws Exception {
        // given
        process.addProtocol(protocol);
        processRepository.save(process);

        // send delete request
        webApp.perform(delete("/processes/{processId}/protocols/{protocolId}", process.getId(), protocol.getId()))
                .andExpect(status().isNoContent());

        verify(validationStateChangeService, times(1)).changeValidationState(any(), any(), eq(ValidationState.DRAFT));
    }

    @Test
    public void testSaveProtocolTriggersValidationStateToDraft() throws Exception {
        webApp.perform(post("/processes/{processId}/protocols/", process.getId())
                .contentType("text/uri-list")
                .content(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/protocols/" + protocol.getId()))
                .andExpect(status().isNoContent());

        verify(validationStateChangeService, times(1)).changeValidationState(any(), any(), eq(ValidationState.DRAFT));
    }


    @Test
    public void testSaveProjectDoNotTriggerValidationStateToDraft() throws Exception {
        // send post request
        webApp.perform(put("/processes/{processId}/project", process.getId())
                .contentType("text/uri-list")
                .content(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/projects/" + project.getId()))
                .andExpect(status().isNoContent());

        verify(validationStateChangeService, times(0)).changeValidationState(any(), any(), eq(ValidationState.DRAFT));
    }

    @Test
    public void testDeleteProjectDoNotTriggerValidationStateToDraft() throws Exception {
        // send delete request
        webApp.perform(delete("/processes/{processId}/project/{projectId}", process.getId(), project.getId()))
                .andExpect(status().isNoContent());

        verify(validationStateChangeService, times(0)).changeValidationState(any(), any(), eq(ValidationState.DRAFT));
    }


}