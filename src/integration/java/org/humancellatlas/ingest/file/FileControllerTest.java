package org.humancellatlas.ingest.file;

import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.MetadataDocument;
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

import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    public void testPutLinkFileAsInputToMultipleProcesses() throws Exception {
        file.addAsInputToProcess(process);
        fileRepository.save(file);

        Process process2 = new Process();
        Process process3 = new Process();
        processRepository.save(process2);
        processRepository.save(process3);

        webApp.perform(put("/files/{fileId}/inputToProcesses/", file.getId())
                .contentType("text/uri-list")
                .content(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process2.getId()
                        +'\n'+ ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process3.getId()))
                .andExpect(status().isOk());

        verifyMetadataValidationStateInDraft(file, process, process2, process3);

        File updatedFile = fileRepository.findById(file.getId()).get();
        assertThat(updatedFile.getInputToProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process2, process3);
    }

    @Test
    public void testPostLinkFileAsInputToMultipleProcesses() throws Exception {
        Process process2 = new Process();
        processRepository.save(process2);

        webApp.perform(post("/files/{fileId}/inputToProcesses/", file.getId())
                .contentType("text/uri-list")
                .content(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process.getId()
                        +'\n'+ ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process2.getId()))
                .andExpect(status().isOk());

        verifyMetadataValidationStateInDraft(file, process, process2);

        File updatedFile = fileRepository.findById(file.getId()).get();
        assertThat(updatedFile.getInputToProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process, process2);
    }

    @Test
    public void testPostLinkFileAsInputToOneProcess() throws Exception {
        webApp.perform(post("/files/{fileId}/inputToProcesses/", file.getId())
                .contentType("text/uri-list")
                .content(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process.getId()))
                .andExpect(status().isOk());

        verifyMetadataValidationStateInDraft(file, process);

        File updatedFile = fileRepository.findById(file.getId()).get();
        assertThat(updatedFile.getInputToProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process);
    }

    @Test
    public void testPostLinkFileAsDerivedByOneProcess() throws Exception {
        webApp.perform(post("/files/{fileId}/derivedByProcesses/", file.getId())
                .contentType("text/uri-list")
                .content(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process.getId()))
                .andExpect(status().isOk());

        verifyMetadataValidationStateInDraft(file, process);

        File updatedFile = fileRepository.findById(file.getId()).get();
        assertThat(updatedFile.getDerivedByProcesses())
                .usingElementComparatorOnFields("id")
                .contains(process);
    }

    @Test
    public void testPostLinkFileAsDerivedByMultipleProcesses() throws Exception {
        Process process2 = new Process();
        processRepository.save(process2);

        webApp.perform(post("/files/{fileId}/derivedByProcesses/", file.getId())
                .contentType("text/uri-list")
                .content(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process.getId()
                        +'\n'+ ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process2.getId()))
                .andExpect(status().isOk());

        verifyMetadataValidationStateInDraft(file, process, process2);

        File updatedFile = fileRepository.findById(file.getId()).get();
        assertThat(updatedFile.getDerivedByProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process, process2);
    }

    @Test
    public void testPutLinkFileAsDerivedByMultipleProcesses() throws Exception {
        file.addAsDerivedByProcess(process);
        fileRepository.save(file);

        Process process2 = new Process();
        Process process3 = new Process();
        processRepository.save(process2);
        processRepository.save(process3);

        webApp.perform(put("/files/{fileId}/derivedByProcesses/", file.getId())
                .contentType("text/uri-list")
                .content(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process2.getId()
                        +'\n'+ ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process3.getId()))
                .andExpect(status().isOk());

        verifyMetadataValidationStateInDraft(file, process, process2, process3);

        File updatedFile = fileRepository.findById(file.getId()).get();
        assertThat(updatedFile.getDerivedByProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process2, process3);
    }

    @Test
    public void testUnlinkFileAsDerivedByProcess() throws Exception {
        // given
        file.addAsDerivedByProcess(process);
        fileRepository.save(file);

        // when
        webApp.perform(delete("/files/{fileId}/derivedByProcesses/{processId}", file.getId(), process.getId()))
                .andExpect(status().isNoContent());

        // then
        verifyMetadataValidationStateInDraft(file, process);

        File updatedFile = fileRepository.findById(file.getId()).get();
        assertThat(updatedFile.getDerivedByProcesses()).doesNotContain(process);
    }

    @Test
    public void testUnlinkFileAsInputToProcess() throws Exception {
        // given
        file.addAsInputToProcess(process);
        fileRepository.save(file);

        // when
        webApp.perform(delete("/files/{fileId}/inputToProcesses/{processId}", file.getId(), process.getId()))
                .andExpect(status().isNoContent());

        // then
        verifyMetadataValidationStateInDraft(file, process);

        File updatedFile = fileRepository.findById(file.getId()).get();
        assertThat(updatedFile.getInputToProcesses()).doesNotContain(process);
    }

    private void verifyMetadataValidationStateInDraft(MetadataDocument... values) {
        Arrays.stream(values).forEach(value -> {
            verify(validationStateChangeService, times(1)).changeValidationState(value.getType(), value.getId(), ValidationState.DRAFT);
        });
    }
}