package org.humancellatlas.ingest.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.ValidationStateChangeService;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
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
    private SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MigrationConfiguration migrationConfiguration;

    @MockBean
    private MessageRouter messageRouter;

    Process process;

    Process process2;

    Process process3;

    File file;

    UriComponentsBuilder uriBuilder;

    SubmissionEnvelope submissionEnvelope;

    @BeforeEach
    void setUp() {
        submissionEnvelope = new SubmissionEnvelope(UUID.randomUUID().toString());
        submissionEnvelope.setUuid(Uuid.newUuid());
        submissionEnvelope.enactStateTransition(SubmissionState.GRAPH_VALID);
        submissionEnvelopeRepository.save(submissionEnvelope);

        process = new Process();
        process2 = new Process();
        process3 = new Process();
        processRepository.saveAll(Arrays.asList(process, process2, process3));

        file = new File(UUID.randomUUID().toString());
        file.setSubmissionEnvelope(submissionEnvelope);
        fileRepository.save(file);
        uriBuilder = ServletUriComponentsBuilder.fromCurrentContextPath();
    }

    @Test
    public void testLinkFileAsInputToProcessesUsingPutMethodWithManyProcessesInPayload() throws Exception {
        file.addAsInputToProcess(process);
        fileRepository.save(file);

        webApp.perform(put("/files/{fileId}/inputToProcesses/", file.getId())
                .contentType("text/uri-list")
                .content(uriBuilder.build().toUriString() + "/processes/" + process2.getId() + '\n'
                        + uriBuilder.build().toUriString() + "/processes/" + process3.getId()))
                .andExpect(status().isOk());

        verifyThatValidationStateChangedToDraftWhenGraphValid(file);

        File updatedFile = fileRepository.findById(file.getId()).get();
        assertThat(updatedFile.getInputToProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process2, process3);
    }

    @Test
    public void testLinkFileAsInputToMultipleProcessesUsingPostMethodWithManyProcessesInPayload() throws Exception {
        // when
        webApp.perform(post("/files/{fileId}/inputToProcesses/", file.getId())
                .contentType("text/uri-list")
                .content(uriBuilder.build().toUriString() + "/processes/" + process.getId()
                        + '\n' + uriBuilder.build().toUriString() + "/processes/" + process2.getId()))
                .andExpect(status().isOk());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(file);
        File updatedFile = fileRepository.findById(file.getId()).get();
        assertThat(updatedFile.getInputToProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process, process2);
    }

    @Test
    public void testLinkFileAsInputToProcessesUsingPostMethodWithOneProcessInPayload() throws Exception {
        // when
        webApp.perform(post("/files/{fileId}/inputToProcesses/", file.getId())
                .contentType("text/uri-list")
                .content(uriBuilder.build().toUriString() + "/processes/" + process.getId()))
                .andExpect(status().isOk());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(file);
        File updatedFile = fileRepository.findById(file.getId()).get();
        assertThat(updatedFile.getInputToProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process);
    }

    @Test
    public void testLinkFileAsDerivedByProcessesUsingPostMethodWithOneProcessInPayload() throws Exception {
        // when
        webApp.perform(post("/files/{fileId}/derivedByProcesses/", file.getId())
                .contentType("text/uri-list")
                .content(uriBuilder.build().toUriString() + "/processes/" + process.getId()))
                .andExpect(status().isOk());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(file);
        File updatedFile = fileRepository.findById(file.getId()).get();
        assertThat(updatedFile.getDerivedByProcesses())
                .usingElementComparatorOnFields("id")
                .contains(process);
    }

    @Test
    public void testLinkFileAsDerivedByProcessesUsingPostMethodWithManyProcessesInPayload() throws Exception {
        // when
        webApp.perform(post("/files/{fileId}/derivedByProcesses/", file.getId())
                .contentType("text/uri-list")
                .content(uriBuilder.build().toUriString() + "/processes/" + process.getId()
                        + '\n' + uriBuilder.build().toUriString() + "/processes/" + process2.getId()))
                .andExpect(status().isOk());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(file);
        File updatedFile = fileRepository.findById(file.getId()).get();
        assertThat(updatedFile.getDerivedByProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process, process2);
    }

    @Test
    public void testLinkFileAsDerivedByProcessesUsingPutMethodWithManyProcessesInPayload() throws Exception {
        // given
        file.addAsDerivedByProcess(process);
        fileRepository.save(file);

        // when
        webApp.perform(put("/files/{fileId}/derivedByProcesses/", file.getId())
                .contentType("text/uri-list")
                .content(uriBuilder.build().toUriString() + "/processes/" + process2.getId() + '\n'
                        + uriBuilder.build().toUriString() + "/processes/" + process3.getId()))
                .andExpect(status().isOk());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(file);
        File updatedFile = fileRepository.findById(file.getId()).get();
        assertThat(updatedFile.getDerivedByProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process2, process3);
    }

    @Test
    public void testUnlinkFileAsDerivedByProcesses() throws Exception {
        // given
        file.addAsDerivedByProcess(process);
        fileRepository.save(file);

        // when
        webApp.perform(delete("/files/{fileId}/derivedByProcesses/{processId}", file.getId(), process.getId()))
                .andExpect(status().isNoContent());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(file);
        File updatedFile = fileRepository.findById(file.getId()).get();
        assertThat(updatedFile.getDerivedByProcesses()).doesNotContain(process);
    }

    @Test
    public void testUnlinkFileAsInputToProcesses() throws Exception {
        // given
        file.addAsInputToProcess(process);
        fileRepository.save(file);

        // when
        webApp.perform(delete("/files/{fileId}/inputToProcesses/{processId}", file.getId(), process.getId()))
                .andExpect(status().isNoContent());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(file);

        File updatedFile = fileRepository.findById(file.getId()).get();
        assertThat(updatedFile.getInputToProcesses()).doesNotContain(process);
    }

    private void verifyThatValidationStateChangedToDraftWhenGraphValid(MetadataDocument... values) {
        Arrays.stream(values).forEach(value -> {
            verify(validationStateChangeService, times(1)).changeValidationState(value.getType(), value.getId(), ValidationState.DRAFT);
        });
    }

    @Test
    public void testValidationJobPatch() throws Exception {
        // ToDo: This test runs against a real mongo database and can fail if it is not empty.
        //given:
        File file = new File("test");
        file = fileRepository.save(file);

        //when:
        String patch = "{ \"validationJob\": { \"validationReport\": { \"validationState\": \"Valid\" }}}";

        MvcResult result = webApp
                .perform(patch("/files/{id}", file.getId())
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(patch))
                .andReturn();

        //expect:
        MockHttpServletResponse response = result.getResponse();
        assertThat(response.getContentType()).containsPattern("application/.*json.*");

        //and:
        file = fileRepository.findById(file.getId()).get();
        assertThat(file.getValidationJob().getValidationReport().getValidationState()).isEqualTo(ValidationState.VALID);
    }
}