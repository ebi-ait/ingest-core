package uk.ac.ebi.subs.ingest.protocol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.ebi.subs.ingest.TestingHelper;
import uk.ac.ebi.subs.ingest.config.MigrationConfiguration;
import uk.ac.ebi.subs.ingest.core.Uuid;
import uk.ac.ebi.subs.ingest.messaging.MessageRouter;
import uk.ac.ebi.subs.ingest.project.*;
import uk.ac.ebi.subs.ingest.state.SubmissionState;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelopeRepository;

@SpringBootTest
@AutoConfigureDataMongo()
@AutoConfigureMockMvc()
@WithMockUser(
    username = "alice",
    roles = {"WRANGLER"})
public class ProtocolControllerTest {
  @Autowired private MockMvc webApp;

  @Autowired private SubmissionEnvelopeRepository submissionEnvelopeRepository;

  @Autowired private ProjectRepository projectRepository;

  @Autowired private ProtocolRepository protocolRepository;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private MigrationConfiguration migrationConfiguration;

  @MockBean private MessageRouter messageRouter;

  SubmissionEnvelope submissionEnvelope;

  Project project;

  UriComponentsBuilder uriBuilder;

  @BeforeEach
  void setUp() {
    submissionEnvelope = new SubmissionEnvelope();
    submissionEnvelope.setUuid(Uuid.newUuid());
    submissionEnvelope.enactStateTransition(SubmissionState.GRAPH_VALID);
    submissionEnvelope = submissionEnvelopeRepository.save(submissionEnvelope);

    project = new Project(new HashMap<String, Object>());
    project.setSubmissionEnvelope(submissionEnvelope);
    project.getSubmissionEnvelopes().add(submissionEnvelope);
    ((Map<String, Object>) project.getContent())
        .put("dataAccess", new ObjectToMapConverter().asMap(new DataAccess(DataAccessTypes.OPEN)));

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
  @WithMockUser()
  public void newProtocolInSubmissionLinksToSubmissionAndProject() throws Exception {
    // when
    webApp
        .perform(
            post("/submissionEnvelopes/{id}/protocols", submissionEnvelope.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\": {}}"))
        .andExpect(status().isAccepted());
    TestingHelper.resetTestingSecurityContext();

    // then
    assertThat(protocolRepository.findAll()).hasSize(1);
    assertThat(protocolRepository.findAllBySubmissionEnvelope(submissionEnvelope)).hasSize(1);
    assertThat(protocolRepository.findByProject(project)).hasSize(1);

    var newProtocol = protocolRepository.findAll().get(0);
    assertThat(newProtocol.getSubmissionEnvelope().getId()).isEqualTo(submissionEnvelope.getId());
    assertThat(newProtocol.getProject().getId()).isEqualTo(project.getId());
  }

  @Test
  public void newProtocolInSubmissionDoesNotFailIfSubmissionHasNoProject() throws Exception {
    // given
    projectRepository.deleteAll();

    // when
    webApp
        .perform(
            post("/submissionEnvelopes/{id}/protocols", submissionEnvelope.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\": {}}"))
        .andExpect(status().isAccepted());
    TestingHelper.resetTestingSecurityContext();

    // then
    assertThat(protocolRepository.findAll()).hasSize(1);
    assertThat(protocolRepository.findAllBySubmissionEnvelope(submissionEnvelope)).hasSize(1);

    var newProtocol = protocolRepository.findAll().get(0);
    assertThat(newProtocol.getSubmissionEnvelope().getId()).isEqualTo(submissionEnvelope.getId());
    assertThat(newProtocol.getProject()).isNull();
  }
}