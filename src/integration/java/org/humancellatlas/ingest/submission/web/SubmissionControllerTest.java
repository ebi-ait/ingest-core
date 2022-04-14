package org.humancellatlas.ingest.submission.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.state.SubmissionState;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureDataMongo()
@AutoConfigureMockMvc()
public class SubmissionControllerTest {
    @Autowired
    private MockMvc webApp;

    @Autowired
    private SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private BiomaterialRepository biomaterialRepository;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private ProtocolRepository protocolRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MigrationConfiguration migrationConfiguration;

    @MockBean
    private MessageRouter messageRouter;

    SubmissionEnvelope submissionEnvelope;

    Project project;

    Biomaterial biomaterial;

    Process process;

    Protocol protocol;

    File file;

    UriComponentsBuilder uriBuilder;

    @BeforeEach
    void setUp() {
        submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.setUuid(Uuid.newUuid());
        submissionEnvelope.enactStateTransition(SubmissionState.GRAPH_VALID);
        submissionEnvelope = submissionEnvelopeRepository.save(submissionEnvelope);

        project = new Project(null);
        project.setSubmissionEnvelope(submissionEnvelope);
        project.getSubmissionEnvelopes().add(submissionEnvelope);
        project = projectRepository.save(project);

        biomaterial = new Biomaterial(null);
        biomaterial.setSubmissionEnvelope(submissionEnvelope);
        biomaterial = biomaterialRepository.save(biomaterial);

        process = new Process(null);
        process.setSubmissionEnvelope(submissionEnvelope);
        process = processRepository.save(process);

        protocol = new Protocol(null);
        protocol.setSubmissionEnvelope(submissionEnvelope);
        protocol = protocolRepository.save(protocol);

        file = new File(null, "fileName");
        file.setSubmissionEnvelope(submissionEnvelope);
        file = fileRepository.save(file);

        uriBuilder = ServletUriComponentsBuilder.fromCurrentContextPath();
    }

    @AfterEach
    void tearDown() {
        submissionEnvelopeRepository.deleteAll();
        projectRepository.deleteAll();
        biomaterialRepository.deleteAll();
        processRepository.deleteAll();
        protocolRepository.deleteAll();
        fileRepository.deleteAll();
    }

    @Test
    public void newBiomaterialInSubmissionLinksToSubmissionAndProject() throws Exception {
        //given
        biomaterialRepository.deleteAll();

        // when
        webApp.perform(
            post("/submissionEnvelopes/{id}/biomaterials", submissionEnvelope.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\": {}}")
        ).andExpect(status().isAccepted());

        //then
        assertThat(biomaterialRepository.findAll()).hasSize(1);
        assertThat(biomaterialRepository.findAllBySubmissionEnvelope(submissionEnvelope)).hasSize(1);
        assertThat(biomaterialRepository.findByProject(project)).hasSize(1);
        var newBiomaterial = biomaterialRepository.findAll().get(0);
        assertThat(newBiomaterial.getSubmissionEnvelope()).isNotNull();
        assertThat(newBiomaterial.getProject()).isNotNull();
    }

    @Test
    public void newBiomaterialInSubmissionDoesNotFailIfSubmissionHasNoProject() throws Exception {
        //given
        biomaterialRepository.deleteAll();
        projectRepository.deleteAll();

        // when
        webApp.perform(
            post("/submissionEnvelopes/{id}/biomaterials", submissionEnvelope.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\": {}}")
        ).andExpect(status().isAccepted());

        //then
        assertThat(biomaterialRepository.findAll()).hasSize(1);
        assertThat(biomaterialRepository.findAllBySubmissionEnvelope(submissionEnvelope)).hasSize(1);
        var newBiomaterial = biomaterialRepository.findAll().get(0);
        assertThat(newBiomaterial.getSubmissionEnvelope()).isNotNull();
        assertThat(newBiomaterial.getProject()).isNull();
    }

    @Test
    public void newProcessInSubmissionLinksToSubmissionAndProject() throws Exception {
        //given
        processRepository.deleteAll();

        // when
        webApp.perform(
            post("/submissionEnvelopes/{id}/processes", submissionEnvelope.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\": {}}")
        ).andExpect(status().isAccepted());

        //then
        assertThat(processRepository.findAll()).hasSize(1);
        assertThat(processRepository.findAllBySubmissionEnvelope(submissionEnvelope)).hasSize(1);
        assertThat(processRepository.findByProject(project)).hasSize(1);
        var newProcess = processRepository.findAll().get(0);
        assertThat(newProcess.getSubmissionEnvelope()).isNotNull();
        assertThat(newProcess.getProject()).isNotNull();
    }

    @Test
    public void newProcessInSubmissionDoesNotFailIfSubmissionHasNoProject() throws Exception {
        //given
        processRepository.deleteAll();
        projectRepository.deleteAll();

        // when
        webApp.perform(
            post("/submissionEnvelopes/{id}/processes", submissionEnvelope.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\": {}}")
        ).andExpect(status().isAccepted());

        //then
        assertThat(processRepository.findAll()).hasSize(1);
        assertThat(processRepository.findAllBySubmissionEnvelope(submissionEnvelope)).hasSize(1);
        var newProcess = processRepository.findAll().get(0);
        assertThat(newProcess.getSubmissionEnvelope()).isNotNull();
        assertThat(newProcess.getProject()).isNull();
    }

    @Test
    public void newProtocolInSubmissionLinksToSubmissionAndProject() throws Exception {
        //given
        protocolRepository.deleteAll();

        // when
        webApp.perform(
            post("/submissionEnvelopes/{id}/protocols", submissionEnvelope.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\": {}}")
        ).andExpect(status().isAccepted());

        //then
        assertThat(protocolRepository.findAll()).hasSize(1);
        assertThat(protocolRepository.findAllBySubmissionEnvelope(submissionEnvelope)).hasSize(1);
        assertThat(protocolRepository.findByProject(project)).hasSize(1);
        var newProtocol = protocolRepository.findAll().get(0);
        assertThat(newProtocol.getSubmissionEnvelope()).isNotNull();
        assertThat(newProtocol.getProject()).isNotNull();
    }

    @Test
    public void newProtocolInSubmissionDoesNotFailIfSubmissionHasNoProject() throws Exception {
        //given
        protocolRepository.deleteAll();
        projectRepository.deleteAll();

        // when
        webApp.perform(
            post("/submissionEnvelopes/{id}/protocols", submissionEnvelope.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\": {}}")
        ).andExpect(status().isAccepted());

        //then
        assertThat(protocolRepository.findAll()).hasSize(1);
        assertThat(protocolRepository.findAllBySubmissionEnvelope(submissionEnvelope)).hasSize(1);
        var newProtocol = protocolRepository.findAll().get(0);
        assertThat(newProtocol.getSubmissionEnvelope()).isNotNull();
        assertThat(newProtocol.getProject()).isNull();
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

        //then
        assertThat(fileRepository.findAll()).hasSize(1);
        assertThat(fileRepository.findAllBySubmissionEnvelope(submissionEnvelope)).hasSize(1);
        assertThat(fileRepository.findByProject(project)).hasSize(1);
        var newFile = fileRepository.findAll().get(0);
        assertThat(newFile.getSubmissionEnvelope()).isNotNull();
        assertThat(newFile.getProject()).isNotNull();
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

        //then
        assertThat(fileRepository.findAll()).hasSize(1);
        assertThat(fileRepository.findAllBySubmissionEnvelope(submissionEnvelope)).hasSize(1);
        var newFile = fileRepository.findAll().get(0);
        assertThat(newFile.getSubmissionEnvelope()).isNotNull();
        assertThat(newFile.getProject()).isNull();
    }
}
