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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureDataMongo()
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

    Process process1;

    Process process2;

    Process process3;

    File file;

    UriComponentsBuilder uriBuilder;

    SubmissionEnvelope submissionEnvelope;

    @BeforeEach
    void setUp() {
        submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.setUuid(Uuid.newUuid());
        submissionEnvelope.enactStateTransition(SubmissionState.GRAPH_VALID);
        submissionEnvelope = submissionEnvelopeRepository.save(submissionEnvelope);

        process1 = processRepository.save(new Process(null));
        process2 = processRepository.save(new Process(null));
        process3 = processRepository.save(new Process(null));

        file = new File(null, "fileName");
        file.setSubmissionEnvelope(submissionEnvelope);
        fileRepository.save(file);

        uriBuilder = ServletUriComponentsBuilder.fromCurrentContextPath();
    }

    @AfterEach
    private void tearDown() {
        processRepository.deleteAll();
        fileRepository.deleteAll();
        submissionEnvelopeRepository.deleteAll();
    }

    @Test
    public void testLinkFileAsInputToProcessesUsingPutMethodWithManyProcessesInPayload() throws Exception {
        file.addAsInputToProcess(process1);
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
                .content(uriBuilder.build().toUriString() + "/processes/" + process1.getId()
                        + '\n' + uriBuilder.build().toUriString() + "/processes/" + process2.getId()))
                .andExpect(status().isOk());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(file);
        File updatedFile = fileRepository.findById(file.getId()).get();
        assertThat(updatedFile.getInputToProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process1, process2);
    }

    @Test
    public void testLinkFileAsInputToProcessesUsingPostMethodWithOneProcessInPayload() throws Exception {
        // when
        webApp.perform(post("/files/{fileId}/inputToProcesses/", file.getId())
                .contentType("text/uri-list")
                .content(uriBuilder.build().toUriString() + "/processes/" + process1.getId()))
                .andExpect(status().isOk());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(file);
        File updatedFile = fileRepository.findById(file.getId()).get();
        assertThat(updatedFile.getInputToProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process1);
    }

    @Test
    public void testLinkFileAsDerivedByProcessesUsingPostMethodWithOneProcessInPayload() throws Exception {
        // when
        webApp.perform(post("/files/{fileId}/derivedByProcesses/", file.getId())
                .contentType("text/uri-list")
                .content(uriBuilder.build().toUriString() + "/processes/" + process1.getId()))
                .andExpect(status().isOk());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(file);
        File updatedFile = fileRepository.findById(file.getId()).get();
        assertThat(updatedFile.getDerivedByProcesses())
                .usingElementComparatorOnFields("id")
                .contains(process1);
    }

    @Test
    public void testLinkFileAsDerivedByProcessesUsingPostMethodWithManyProcessesInPayload() throws Exception {
        // when
        webApp.perform(post("/files/{fileId}/derivedByProcesses/", file.getId())
                .contentType("text/uri-list")
                .content(uriBuilder.build().toUriString() + "/processes/" + process1.getId()
                        + '\n' + uriBuilder.build().toUriString() + "/processes/" + process2.getId()))
                .andExpect(status().isOk());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(file);
        File updatedFile = fileRepository.findById(file.getId()).get();
        assertThat(updatedFile.getDerivedByProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process1, process2);
    }

    @Test
    public void testLinkFileAsDerivedByProcessesUsingPutMethodWithManyProcessesInPayload() throws Exception {
        // given
        file.addAsDerivedByProcess(process1);
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
        file.addAsDerivedByProcess(process1);
        fileRepository.save(file);

        // when
        webApp.perform(delete("/files/{fileId}/derivedByProcesses/{processId}", file.getId(), process1.getId()))
                .andExpect(status().isNoContent());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(file);
        File updatedFile = fileRepository.findById(file.getId()).get();
        assertThat(updatedFile.getDerivedByProcesses()).doesNotContain(process1);
    }

    @Test
    public void testUnlinkFileAsInputToProcesses() throws Exception {
        // given
        file.addAsInputToProcess(process1);
        fileRepository.save(file);

        // when
        webApp.perform(delete("/files/{fileId}/inputToProcesses/{processId}", file.getId(), process1.getId()))
                .andExpect(status().isNoContent());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(file);

        File updatedFile = fileRepository.findById(file.getId()).get();
        assertThat(updatedFile.getInputToProcesses()).doesNotContain(process1);
    }

    private void verifyThatValidationStateChangedToDraftWhenGraphValid(MetadataDocument... values) {
        Arrays.stream(values).forEach(value -> {
            verify(validationStateChangeService, times(1)).changeValidationState(value.getType(), value.getId(), ValidationState.DRAFT);
        });
    }

    @Test
    public void testValidationJobPatch() throws Exception {
        //given:
        File file = new File(null, "test");
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