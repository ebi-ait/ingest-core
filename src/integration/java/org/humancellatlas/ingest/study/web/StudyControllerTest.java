package org.humancellatlas.ingest.study.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.assertj.core.data.MapEntry;
import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.dataset.Dataset;
import org.humancellatlas.ingest.dataset.DatasetRepository;
import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.study.Study;
import org.humancellatlas.ingest.study.StudyEventHandler;
import org.humancellatlas.ingest.study.StudyRepository;
import org.humancellatlas.ingest.study.StudyService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class StudyControllerTest {
  @Autowired private MockMvc webApp;

  @Autowired private StudyRepository studyRepository;

  @Autowired private StudyService studyService;

  @Autowired private DatasetRepository datasetRepository;

  @Autowired private MetadataCrudService metadataCrudService;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private SubmissionEnvelopeRepository submissionEnvelopeRepository;

  @SpyBean private StudyEventHandler studyEventHandler;

  @MockBean private MigrationConfiguration migrationConfiguration;

  SubmissionEnvelope submissionEnvelope;

  @AfterEach
  private void tearDown() {
    studyRepository.deleteAll();
  }

  @Nested
  @Disabled("Test class is currently deactivated - Need to be linked to Submission Envelope")
  class Registration {
    @Test
    @DisplayName("Register Study - Success")
    @WithMockUser
    void registerSuccess() throws Exception {
      doTestRegister(
          "/studies",
          study -> {
            var studyCaptor = ArgumentCaptor.forClass(Study.class);
            verify(studyEventHandler).registeredStudy(studyCaptor.capture());
            Study handledStudy = studyCaptor.getValue();
            assertThat(handledStudy.getId()).isNotNull();
          });
    }

    private void doTestRegister(String registerUrl, Consumer<Study> postCondition)
        throws Exception {
      // given:
      submissionEnvelope = new SubmissionEnvelope();
      submissionEnvelope.setUuid(Uuid.newUuid());
      submissionEnvelope.enactStateTransition(SubmissionState.GRAPH_VALID);
      submissionEnvelope = submissionEnvelopeRepository.save(submissionEnvelope);

      var content = new HashMap<String, Object>();
      content.put("name", "Test Study");
      content.put("submissionEnvelope", submissionEnvelope.getId());

      Study study = new Study(
              "https://dev.schema.morphic.bio/type/0.0.1/project/study",
              "0.0.1",
              "study",
              content);

      // Link study to submission envelope
      study.getSubmissionEnvelopes().add(submissionEnvelope);
      study = studyRepository.save(study);

      studyService.addStudyToSubmissionEnvelope(submissionEnvelope, study);

      // when:
      MvcResult result =
          webApp
              .perform(
                  post(registerUrl)
                      .contentType(APPLICATION_JSON_VALUE)
                      .content("{\"content\": " + objectMapper.writeValueAsString(content) + "}"))
              .andReturn();

      // then:
      MockHttpServletResponse response = result.getResponse();
      assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
      assertThat(response.getContentType()).containsPattern("application/.*json.*");

      // and: verify the registered study content
      Map<String, Object> registeredStudy =
          objectMapper.readValue(response.getContentAsString(), Map.class);
      assertThat(registeredStudy.get("content")).isInstanceOf(Map.class);
      MapEntry<String, String> nameEntry = entry("name", "Test Study");
      assertThat((Map) registeredStudy.get("content")).containsOnly(nameEntry);

      // and: verify the study is stored in the repository
      List<Study> studies = studyRepository.findAll();
      assertThat(studies).hasSize(1);
      Study storedStudy = studies.get(0);
      assertThat((Map) storedStudy.getContent()).containsOnly(nameEntry);

      // and:
      postCondition.accept(storedStudy);
    }
  }

  @Nested
  class Update {
    @Test
    @DisplayName("Update Study - Success")
    void updateSuccess() throws Exception {
      doTestUpdate(
          "/studies/{studyId}",
          study -> {
            var studyCaptor = ArgumentCaptor.forClass(Study.class);
            verify(studyEventHandler).updatedStudy(studyCaptor.capture());
            Study handledStudy = studyCaptor.getValue();
            assertThat(handledStudy.getId()).isEqualTo(study.getId());
          });
    }

    private void doTestUpdate(String patchUrl, Consumer<Study> postCondition) throws Exception {
      // given:
      submissionEnvelope = new SubmissionEnvelope();
      submissionEnvelope.setUuid(Uuid.newUuid());
      submissionEnvelope.enactStateTransition(SubmissionState.GRAPH_VALID);
      submissionEnvelope = submissionEnvelopeRepository.save(submissionEnvelope);

      var content = new HashMap<String, Object>();
      content.put("description", "test");
      Study study =
          new Study(
              "https://dev.schema.morphic.bio/type/0.0.1/project/study", "0.0.1", "study", content);
      study.getSubmissionEnvelopes().add(submissionEnvelope);
      study = studyRepository.save(study);

      studyService.addStudyToSubmissionEnvelope(submissionEnvelope, study);

      // when:
      content.put("description", "test updated");
      MvcResult result =
          webApp
              .perform(
                  patch(patchUrl, study.getId())
                      .contentType(APPLICATION_JSON_VALUE)
                      .content("{\"content\": " + objectMapper.writeValueAsString(content) + "}"))
              .andReturn();

      // expect:
      MockHttpServletResponse response = result.getResponse();
      assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
      assertThat(response.getContentType()).containsPattern("application/.*json.*");

      // and:
      Map<String, Object> updated =
          objectMapper.readValue(response.getContentAsString(), Map.class);
      assertThat(updated.get("described_by"))
          .isEqualTo("https://dev.schema.morphic.bio/type/0.0.1/project/study");
      assertThat(updated.get("schema_version")).isEqualTo("0.0.1");
      assertThat(updated.get("schema_type")).isEqualTo("study");
      assertThat(updated.containsKey("content")).isTrue();
      assertThat(((Map<String, Object>) updated.get("content")).get("description"))
          .isEqualTo("test updated");

      // and:
      study = studyRepository.findById(study.getId()).get();

      assertThat(study.getDescribedBy())
          .isEqualTo("https://dev.schema.morphic.bio/type/0.0.1/project/study");
      assertThat(study.getSchemaVersion()).isEqualTo("0.0.1");
      assertThat(study.getSchemaType()).isEqualTo("study");
      assertThat(((Map<String, Object>) study.getContent()).get("description"))
          .isEqualTo("test updated");

      // and:
      postCondition.accept(study);
    }

    @Test
    @DisplayName("Update Study - Not Found")
    void updateStudyNotFound() throws Exception {
      // given:
      String nonExistentStudyId = "nonExistentId";

      // when:
      MvcResult result =
          webApp
              .perform(
                  patch("/studies/{studyId}", nonExistentStudyId)
                      .contentType(APPLICATION_JSON_VALUE)
                      .content("{\"content\": {\"description\": \"Updated Description\"}}"))
              .andReturn();

      // then:
      MockHttpServletResponse response = result.getResponse();
      assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }
  }

  @Nested
  class Delete {
    @Test
    @DisplayName("Delete Study - Success")
    void deleteSuccess() throws Exception {
      // given:
      String content = "{\"name\": \"delete study\"}";
      Study persistentStudy =
          new Study(
              "https://dev.schema.morphic.bio/type/0.0.1/project/study", "0.0.1", "study", content);
      studyRepository.save(persistentStudy);
      String existingStudyId = persistentStudy.getId();

      // when:
      webApp
          .perform(delete("/studies/{studyId}", existingStudyId))
          .andExpect(status().isNoContent());

      // then:
      assertThat(studyRepository.findById(existingStudyId)).isEmpty();
      // Expect the ResourceNotFoundException when attempting to find the study after deletion
      assertThrows(
          ResourceNotFoundException.class,
          () -> {
            metadataCrudService.findOriginalByUuid(
                String.valueOf(persistentStudy.getUuid()), EntityType.STUDY);
          });
      verify(studyEventHandler).deletedStudy(existingStudyId);
    }

    @Test
    @DisplayName("Delete Study - Not Found")
    void deleteStudyNotFound() throws Exception {
      // given: a non-existent study id
      String nonExistentStudyId = "nonExistentId";

      // when:
      MvcResult result =
          webApp.perform(delete("/studies/{studyId}", nonExistentStudyId)).andReturn();

      // then:
      MockHttpServletResponse response = result.getResponse();
      assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }
  }

  @Nested
  class Link {
    @Test
    @DisplayName("Link dataset to study - Success")
    void listDatasetToStudySuccess() throws Exception {
      // given:
      String studyContent = "{\"name\": \"study\"}";
      Study persistentStudy =
          new Study(
              "https://dev.schema.morphic.bio/type/0.0.1/project/study",
              "0.0.1",
              "study",
              studyContent);
      studyRepository.save(persistentStudy);
      String studyId = persistentStudy.getId();

      String datasetContent = "{\"name\": \"dataset\"}";
      Dataset persistentDataset = new Dataset(datasetContent);
      datasetRepository.save(persistentDataset);
      String datasetId = persistentDataset.getId();

      // when:
      webApp
          .perform(put("/studies/{stud_id}/datasets/{dataset_id}", studyId, datasetId))
          .andExpect(status().isAccepted());
    }
  }
}
