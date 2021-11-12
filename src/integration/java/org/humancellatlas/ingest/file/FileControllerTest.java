package org.humancellatlas.ingest.file;

import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.service.ValidationStateChangeService;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.state.ValidationState;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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

    @Test
    public void testDeleteInputToProcessTriggersValidationStateToDraft() throws Exception {
        // given
        Process process = new Process(UUID.randomUUID());
        processRepository.save(process);

        // and
        File file = new File();
        file.addAsInputToProcess(process);
        fileRepository.save(file);

        // send delete request
        MvcResult result = webApp.perform(delete("/files/{fileId}/inputToProcesses/{processId}", file.getId(), process.getId()))
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());

        verify(validationStateChangeService, times(1)).changeValidationState(any(), any(), eq(ValidationState.DRAFT));
    }

    @Test
    public void testSaveInputToProcessTriggersValidationStateToDraft() throws Exception {
        // given
        Process process = new Process(UUID.randomUUID());
        processRepository.save(process);

        // and
        File file = new File();
        fileRepository.save(file);

        // send post request
        MvcResult result = webApp.perform(post("/files/{fileId}/inputToProcesses/", file.getId())
                .contentType("text/uri-list")
                .content(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process.getId()))
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());

        verify(validationStateChangeService, times(1)).changeValidationState(any(), any(), eq(ValidationState.DRAFT));
    }

    @Test
    public void testDeleteDerivedByProcessTriggersValidationStateToDraft() throws Exception {
        // given
        Process process = new Process(UUID.randomUUID());
        processRepository.save(process);

        // and
        File file = new File();
        file.addAsDerivedByProcess(process);
        fileRepository.save(file);

        // send delete request
        MvcResult result = webApp.perform(delete("/files/{fileId}/derivedByProcesses/{processId}", file.getId(), process.getId()))
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());

        verify(validationStateChangeService, times(1)).changeValidationState(any(), any(), eq(ValidationState.DRAFT));
    }

    @Test
    public void testDerivedByProcessTriggersValidationStateToDraft() throws Exception {
        // given
        Process process = new Process(UUID.randomUUID());
        processRepository.save(process);

        // and
        File file = new File();
        fileRepository.save(file);

        // send post request
        MvcResult result = webApp.perform(post("/files/{fileId}/derivedByProcesses/", file.getId())
                .contentType("text/uri-list")
                .content(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process.getId()))
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());

        verify(validationStateChangeService, times(1)).changeValidationState(any(), any(), eq(ValidationState.DRAFT));
    }
}
