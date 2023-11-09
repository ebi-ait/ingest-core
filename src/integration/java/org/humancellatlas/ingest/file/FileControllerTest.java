package org.humancellatlas.ingest.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.humancellatlas.ingest.TestingHelper;
import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.ValidationStateChangeService;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.project.*;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureDataMongo()
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@WithMockUser(username = "alice", roles = {"WRANGLER"})
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
    private ProjectRepository projectRepository;

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

    Project project;

    @BeforeEach
    void setUp() {
        submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.setUuid(Uuid.newUuid());
        submissionEnvelope.enactStateTransition(SubmissionState.GRAPH_VALID);
        submissionEnvelope = submissionEnvelopeRepository.save(submissionEnvelope);

        project = new Project(new HashMap<>());
        project.setUuid(Uuid.newUuid());
        ((Map<String, Object>)project.getContent()).put("dataAccess", new ObjectToMapConverter().asMap(new DataAccess(DataAccessTypes.OPEN)));

        project.setSubmissionEnvelope(submissionEnvelope);
        project.getSubmissionEnvelopes().add(submissionEnvelope);
        project = projectRepository.save(project);

        process1 = processRepository.save(new Process(null));
        process2 = processRepository.save(new Process(null));
        process3 = processRepository.save(new Process(null));

        file = new File(null, "fileName");
        file.setSubmissionEnvelope(submissionEnvelope);
        file = fileRepository.save(file);

        uriBuilder = ServletUriComponentsBuilder.fromCurrentContextPath();
    }

    @AfterEach
    void tearDown() {
        processRepository.deleteAll();
        fileRepository.deleteAll();
        submissionEnvelopeRepository.deleteAll();
    }

    @Test
    public void newFileInSubmissionLinksToSubmissionAndProject() throws Exception {
        //given
        fileRepository.deleteAll();

        // when
        webApp.perform(
            post("/submissionEnvelopes/{id}/files", submissionEnvelope.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\": {}}")
        ).andExpect(status().isAccepted());

        TestingHelper.resetTestingSecurityContext();
        //then
        assertThat(fileRepository.findAll()).hasSize(1);
        assertThat(fileRepository.findAllBySubmissionEnvelope(submissionEnvelope)).hasSize(1);
        assertThat(fileRepository.findByProject(project)).hasSize(1);

        var newFile = fileRepository.findAll().get(0);
        assertThat(newFile.getSubmissionEnvelope().getId()).isEqualTo(submissionEnvelope.getId());
        assertThat(newFile.getProject().getId()).isEqualTo(project.getId());
    }

    @Test
    public void newFileInSubmissionDoesNotFailIfSubmissionHasNoProject() throws Exception {
        //given
        fileRepository.deleteAll();
        projectRepository.deleteAll();

        // when
        webApp.perform(
            post("/submissionEnvelopes/{id}/files", submissionEnvelope.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\": {}}")
        ).andExpect(status().isAccepted());
        TestingHelper.resetTestingSecurityContext();

        //then
        assertThat(fileRepository.findAll()).hasSize(1);
        assertThat(fileRepository.findAllBySubmissionEnvelope(submissionEnvelope)).hasSize(1);

        var newFile = fileRepository.findAll().get(0);
        assertThat(newFile.getSubmissionEnvelope().getId()).isEqualTo(submissionEnvelope.getId());
        assertThat(newFile.getProject()).isNull();
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
        TestingHelper.resetTestingSecurityContext();

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
        TestingHelper.resetTestingSecurityContext();

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
        TestingHelper.resetTestingSecurityContext();

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
        TestingHelper.resetTestingSecurityContext();

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
        TestingHelper.resetTestingSecurityContext();

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

        TestingHelper.resetTestingSecurityContext();
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
        TestingHelper.resetTestingSecurityContext();

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
        TestingHelper.resetTestingSecurityContext();

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
        file.setSubmissionEnvelope(submissionEnvelope);
        file = fileRepository.save(file);

        //when:
        String patch = "{ \"validationJob\": { \"validationReport\": { \"validationState\": \"Valid\" }}}";

        MvcResult result = webApp
                .perform(patch("/files/{id}", file.getId())
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(patch))
                .andReturn();
        TestingHelper.resetTestingSecurityContext();

        //expect:
        MockHttpServletResponse response = result.getResponse();
        assertThat(response.getContentType()).containsPattern("application/.*json.*");

        //and:
        file = fileRepository.findById(file.getId()).get();
        assertThat(file.getValidationJob().getValidationReport().getValidationState()).isEqualTo(ValidationState.VALID);
    }

    @Test
    public void when_new_File_ctor__pass() throws Exception {
        String filePayload = objectMapper.writeValueAsString(new File());
        webApp.perform(
                post("/submissionEnvelopes/{id}/files", submissionEnvelope.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filePayload)
        ).andExpect(status().isAccepted());
    }

    @Test
    public void when_DataFileUuid_is_null__accepted_with_random() throws Exception {
        ObjectNode patch = createPayloadhNoDataFileUuid();
        webApp.perform(
                        post("/submissionEnvelopes/{id}/files", submissionEnvelope.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patch))
                ).andExpect(status().isAccepted())
                .andExpect(jsonPath("$.dataFileUuid").isNotEmpty());
    }

    @Test
    public void when_payload_is_good__pass() throws Exception {
        ObjectNode patch = createValidFilePayload();
        webApp.perform(
                post("/submissionEnvelopes/{id}/files", submissionEnvelope.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch))
        ).andExpect(status().isAccepted());
    }


    private ObjectNode createValidFilePayload() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode newFilePayload = mapper.createObjectNode();

        newFilePayload
                .put("dataFileUuid", UUID.randomUUID().toString())
                .put("fileName", "test-file")
                .put("fileContentType", "text/plain");
        return newFilePayload;
    }

    private ObjectNode createPayloadhNoDataFileUuid() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode newFilePayload = mapper.createObjectNode();

        newFilePayload
                .put("fileName", "test-file")
                .put("fileContentType", "text/plain");
        return newFilePayload;
    }
}
