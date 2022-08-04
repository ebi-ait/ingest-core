package org.humancellatlas.ingest.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
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
public class ProtocolControllerTest {
    @Autowired
    private MockMvc webApp;

    @Autowired
    private SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProtocolRepository protocolRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MigrationConfiguration migrationConfiguration;

    @MockBean
    private MessageRouter messageRouter;

    SubmissionEnvelope submissionEnvelope;

    Project project;

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

        uriBuilder = ServletUriComponentsBuilder.fromCurrentContextPath();
    }

    @AfterEach
    void tearDown() {
        submissionEnvelopeRepository.deleteAll();
        projectRepository.deleteAll();
        protocolRepository.deleteAll();
    }

    @Test
    public void newProtocolInSubmissionLinksToSubmissionAndProject() throws Exception {
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
}
