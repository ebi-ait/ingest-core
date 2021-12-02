package org.humancellatlas.ingest.biomaterial;

import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.Checksums;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.ValidationStateChangeService;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.web.FileMessage;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
public class BiomaterialControllerTest {

    @MockBean
    ValidationStateChangeService validationStateChangeService;

    @Autowired
    private MockMvc webApp;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private BiomaterialRepository biomaterialRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @MockBean
    private MigrationConfiguration migrationConfiguration;

    @MockBean
    private MessageRouter messageRouter;

    Process process;

    Biomaterial biomaterial;

    @BeforeEach
    void setUp() {
        process = new Process(UUID.randomUUID());
        processRepository.save(process);

        biomaterial = new Biomaterial(null);
    }

    @Test
    public void testDeleteInputToProcessTriggersValidationStateToDraft() throws Exception {
        // given
        biomaterial.addAsInputToProcess(process);
        biomaterialRepository.save(biomaterial);

        // send delete request
        webApp.perform(delete("/biomaterials/{biomaterialId}/inputToProcesses/{processId}", biomaterial.getId(), process.getId()))
                .andExpect(status().isNoContent());

        verify(validationStateChangeService, times(1)).changeValidationState(any(), any(), eq(ValidationState.DRAFT));
    }

    @Test
    public void testSaveInputToProcessTriggersValidationStateToDraft() throws Exception {
        // send post request
        webApp.perform(post("/biomaterials/{biomaterialId}/inputToProcesses/", biomaterial.getId())
                .contentType("text/uri-list")
                .content(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process.getId()))
                .andExpect(status().isNoContent());

        verify(validationStateChangeService, times(1)).changeValidationState(any(), any(), eq(ValidationState.DRAFT));

    }

    @Test
    public void testDeleteDerivedByProcessTriggersValidationStateToDraft() throws Exception {
        // given
        biomaterial.addAsDerivedByProcess(process);
        biomaterialRepository.save(biomaterial);

        // send delete request
        webApp.perform(delete("/biomaterials/{biomaterialId}/derivedByProcesses/{processId}", biomaterial.getId(), process.getId()))
                .andExpect(status().isNoContent());

        verify(validationStateChangeService, times(1)).changeValidationState(any(), any(), eq(ValidationState.DRAFT));
    }

    @Test
    public void testDerivedByProcessTriggersValidationStateToDraft() throws Exception {
        // send post request
        webApp.perform(post("/biomaterials/{biomaterialId}/derivedByProcesses/", biomaterial.getId())
                .contentType("text/uri-list")
                .content(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process.getId()))
                .andExpect(status().isNoContent());

        verify(validationStateChangeService, times(1)).changeValidationState(any(), any(), eq(ValidationState.DRAFT));
    }

}