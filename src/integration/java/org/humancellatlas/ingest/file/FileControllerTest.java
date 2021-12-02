package org.humancellatlas.ingest.file;

import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.service.ValidationStateChangeService;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.state.ValidationState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
public class FileControllerTest {
    @MockBean
    ValidationStateChangeService validationStateChangeService;

    @Autowired
    private MockMvc webApp;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private FileRepository fileRepository;

    @MockBean
    private MigrationConfiguration migrationConfiguration;

    @MockBean
    private MessageRouter messageRouter;

    Process process;

    File file = new File();

    @BeforeEach
    void setUp() {
        process = new Process(UUID.randomUUID());
        processRepository.save(process);

        file = new File();
        fileRepository.save(file);
    }

    @Test
    public void testDeleteInputToProcessTriggersValidationStateToDraft() throws Exception {
        // given
        file.addAsInputToProcess(process);
        fileRepository.save(file);

        // send delete request
        webApp.perform(delete("/files/{fileId}/inputToProcesses/{processId}", file.getId(), process.getId()))
                .andExpect(status().isNoContent());

        verify(validationStateChangeService, times(1)).changeValidationState(any(), any(), eq(ValidationState.DRAFT));
    }

    @Test
    public void testSaveInputToProcessTriggersValidationStateToDraft() throws Exception {
        // send post request
        webApp.perform(post("/files/{fileId}/inputToProcesses/", file.getId())
                .contentType("text/uri-list")
                .content(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process.getId()))
                .andExpect(status().isNoContent());

        verify(validationStateChangeService, times(1)).changeValidationState(any(), any(), eq(ValidationState.DRAFT));
    }

    @Test
    public void testDeleteDerivedByProcessTriggersValidationStateToDraft() throws Exception {
        // given
        file.addAsDerivedByProcess(process);
        fileRepository.save(file);

        // send delete request
        webApp.perform(delete("/files/{fileId}/derivedByProcesses/{processId}", file.getId(), process.getId()))
                .andExpect(status().isNoContent());

        verify(validationStateChangeService, times(1)).changeValidationState(any(), any(), eq(ValidationState.DRAFT));
    }

    @Test
    public void testDerivedByProcessTriggersValidationStateToDraft() throws Exception {
        // send post request
        webApp.perform(post("/files/{fileId}/derivedByProcesses/", file.getId())
                .contentType("text/uri-list")
                .content(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process.getId()))
                .andExpect(status().isNoContent());

        verify(validationStateChangeService, times(1)).changeValidationState(any(), any(), eq(ValidationState.DRAFT));
    }
}