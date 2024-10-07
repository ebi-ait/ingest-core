package uk.ac.ebi.subs.ingest.submission.web;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
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

import uk.ac.ebi.subs.ingest.biomaterial.Biomaterial;
import uk.ac.ebi.subs.ingest.biomaterial.BiomaterialRepository;
import uk.ac.ebi.subs.ingest.config.MigrationConfiguration;
import uk.ac.ebi.subs.ingest.core.Uuid;
import uk.ac.ebi.subs.ingest.dataset.Dataset;
import uk.ac.ebi.subs.ingest.dataset.DatasetRepository;
import uk.ac.ebi.subs.ingest.file.File;
import uk.ac.ebi.subs.ingest.file.FileRepository;
import uk.ac.ebi.subs.ingest.messaging.MessageRouter;
import uk.ac.ebi.subs.ingest.process.Process;
import uk.ac.ebi.subs.ingest.process.ProcessRepository;
import uk.ac.ebi.subs.ingest.project.Project;
import uk.ac.ebi.subs.ingest.project.ProjectRepository;
import uk.ac.ebi.subs.ingest.protocol.Protocol;
import uk.ac.ebi.subs.ingest.protocol.ProtocolRepository;
import uk.ac.ebi.subs.ingest.state.SubmissionState;
import uk.ac.ebi.subs.ingest.study.Study;
import uk.ac.ebi.subs.ingest.study.StudyRepository;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelopeRepository;

@SpringBootTest
@AutoConfigureDataMongo()
@AutoConfigureMockMvc()
@WithMockUser
public class SubmissionControllerTest {
  @Autowired private MockMvc webApp;

  @Autowired private SubmissionEnvelopeRepository submissionEnvelopeRepository;

  @Autowired private ProjectRepository projectRepository;

  @Autowired private BiomaterialRepository biomaterialRepository;

  @Autowired private ProcessRepository processRepository;

  @Autowired private ProtocolRepository protocolRepository;

  @Autowired private FileRepository fileRepository;

  @Autowired private StudyRepository studyRepository;

  @Autowired private DatasetRepository datasetRepository;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private MigrationConfiguration migrationConfiguration;

  @MockBean private MessageRouter messageRouter;

  SubmissionEnvelope submissionEnvelope;

  Project project;

  Biomaterial biomaterial;

  Process process;

  Protocol protocol;

  File file;

  Study study;

  Dataset dataset;

  UriComponentsBuilder uriBuilder;

  @BeforeEach
  void setUp() {
    submissionEnvelope = new SubmissionEnvelope();
    submissionEnvelope.setUuid(Uuid.newUuid());
    submissionEnvelope.enactStateTransition(SubmissionState.GRAPH_VALID);
    submissionEnvelope = submissionEnvelopeRepository.save(submissionEnvelope);

    project = new Project(null);
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

    study = new Study(null, null, null, null);
    study.getSubmissionEnvelopes().add(submissionEnvelope);
    study = studyRepository.save(study);

    dataset = new Dataset(null);
    dataset = datasetRepository.save(dataset);

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

  @ParameterizedTest
  @ValueSource(strings = {"biomaterials", "processes", "protocols", "files"})
  public void testAdditionToNonEditableSubmissionThrowsErrorForAllEntityTypes(String endpoint)
      throws Exception {
    // given
    submissionEnvelope.enactStateTransition(SubmissionState.GRAPH_VALID);
    submissionEnvelope = submissionEnvelopeRepository.save(submissionEnvelope);

    // when
    webApp
        .perform(
            post("/submissionEnvelopes/{id}/" + endpoint, submissionEnvelope.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\": {}}"))
        .andExpect(status().isForbidden());
  }

  @ParameterizedTest
  @EnumSource(
      value = SubmissionState.class,
      names = {"EXPORTING", "PROCESSING", "ARCHIVED", "SUBMITTED"})
  public void testAdditionToNonEditableSubmissionThrowsErrorInAllStates(SubmissionState state)
      throws Exception {
    // given
    submissionEnvelope.enactStateTransition(state);
    submissionEnvelope = submissionEnvelopeRepository.save(submissionEnvelope);

    // when
    webApp
        .perform(
            post("/submissionEnvelopes/{id}/biomaterials", submissionEnvelope.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\": {}}"))
        .andExpect(status().isForbidden());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"/submissionEnvelopes/{id}/projects", "/submissionEnvelopes/{id}/relatedProjects"})
  @WithMockUser
  public void testProjectsAreReturnedWhenTheyIncludeTheSubmissionInTheirEnvelopes(String endpoint)
      throws Exception {
    webApp
        .perform(
            // when
            get(endpoint, submissionEnvelope.getId())
                .contentType(MediaType.APPLICATION_JSON)) // then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.projects", hasSize(1)))
        .andExpect(
            jsonPath(
                "$._embedded.projects[0].uuid.uuid", is(project.getUuid().getUuid().toString())));
  }
}