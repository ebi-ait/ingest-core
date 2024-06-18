package org.humancellatlas.ingest.study;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.dataset.DatasetRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {StudyService.class, StudyRepository.class, DatasetRepository.class})
public class StudyServiceTest {

  @Autowired private ApplicationContext applicationContext;

  @Autowired private StudyService studyService;

  @MockBean private MongoTemplate mongoTemplate;

  @MockBean private StudyRepository studyRepository;

  @MockBean private DatasetRepository datasetRepository;

  @MockBean private StudyEventHandler studyEventHandler;

  @MockBean private MetadataCrudService metadataCrudService;

  @MockBean private MetadataUpdateService metadataUpdateService;

  @BeforeEach
  void setUp() {
    applicationContext.getBeansWithAnnotation(MockBean.class).forEach(Mockito::reset);
    Mockito.reset(metadataCrudService, studyRepository, studyEventHandler);
  }

  @Nested
  class SubmissionEnvelopes {
    Study study1;
    Study study2;
    Set<SubmissionEnvelope> submissionSet1;
    Set<SubmissionEnvelope> submissionSet2;

    @BeforeEach
    void setup() {
      // given
      study1 = spy(new Study("Schema URL", "1.0", "Generic", null));
      doReturn("study1").when(study1).getId();
      study1.setUuid(Uuid.newUuid());

      submissionSet1 = new HashSet<>();
      IntStream.range(0, 3)
          .mapToObj(Integer::toString)
          .forEach(
              id -> {
                var sub = spy(new SubmissionEnvelope());
                doReturn(id).when(sub).getId();
                submissionSet1.add(sub);
              });
      submissionSet1.forEach(study1::addToSubmissionEnvelopes);

      // and:
      study2 = spy(new Study("Schema URL-2", "2.0", "Generic", null));
      doReturn("study2").when(study2).getId();
      study2.setUuid(study1.getUuid());

      submissionSet2 = new HashSet<>();
      IntStream.range(10, 15)
          .mapToObj(Integer::toString)
          .forEach(
              id -> {
                var sub = spy(new SubmissionEnvelope());
                doReturn(id).when(sub).getId();
                submissionSet2.add(sub);
              });
      submissionSet2.forEach(study2::addToSubmissionEnvelopes);
    }

    @Test
    @DisplayName("get all submissions")
    void getFromAllCopiesOfStudies() {
      // given
      when(studyRepository.findByUuid(study1.getUuid())).thenReturn(Stream.of(study1, study2));

      // when:
      var submissionEnvelopes = studyService.getSubmissionEnvelopes(study1);

      // then:
      assertThat(submissionEnvelopes).containsAll(submissionSet1).containsAll(submissionSet2);
    }

    @Test
    @DisplayName("no duplicate submissions")
    void getFromAllCopiesOfStudiesNoDuplicates() {
      // given
      var study3 = spy(new Study("Schema URL", "1.0", "Generic", null));
      doReturn("study3").when(study3).getId();
      study3.setUuid(study1.getUuid());

      submissionSet1.forEach(study3::addToSubmissionEnvelopes);

      var documentIds = new ArrayList<String>();
      submissionSet1.forEach(submission -> documentIds.add(submission.getId()));
      submissionSet2.forEach(submission -> documentIds.add(submission.getId()));

      // and:
      when(studyRepository.findByUuid(study1.getUuid()))
          .thenReturn(Stream.of(study1, study2, study3));

      // when:
      var submissionEnvelopes = studyService.getSubmissionEnvelopes(study1);

      // then:
      var returnDocumentIds = new ArrayList<String>();
      submissionEnvelopes.forEach(submission -> returnDocumentIds.add(submission.getId()));

      assertThat(returnDocumentIds).containsExactlyInAnyOrderElementsOf(documentIds);
    }
  }

  @Nested
  class StudyRegistration {

    @Test
    @DisplayName("Register Study - Success")
    void registerSuccess() {
      // given:
      String content = "{\"name\": \"study\"}";
      Study study = new Study("Schema URL", "1.0", "Generic", "{\"name\": \"study\"}");

      // and:
      Study persistentStudy = new Study("Schema URL", "1.0", "Generic", "{\"name\": \"study\"}");
      doReturn(persistentStudy).when(studyRepository).save(study);

      // when:
      Study result = studyService.register(study);

      // then:
      verify(studyRepository, times(1)).save(study);
      assertThat(result).isEqualTo(persistentStudy);
      verify(studyEventHandler).registeredStudy(persistentStudy);
    }

    @Test
    @DisplayName("Register Study - Ensure Descriptive Fields Are Managed Correctly")
    void shouldHandleDescriptiveFieldsCorrectly() {
      // Initialize studies with descriptive fields
      Study study =
          new Study(
              "Schema URL", "1.0", "Study", "{\"type\": \"study\", \"content\": \"Details\"}");

      // Test retrieval of descriptive fields
      assertEquals("Schema URL", study.getDescribedBy());
      assertEquals("1.0", study.getSchemaVersion());
      assertEquals("Study", study.getSchemaType());

      // Test setting and getting new values
      study.setDescribedBy("Updated Schema URL");
      study.setSchemaVersion("1.1");
      study.setSchemaType("Protocol");

      assertEquals("Updated Schema URL", study.getDescribedBy());
      assertEquals("1.1", study.getSchemaVersion());
      assertEquals("Protocol", study.getSchemaType());
    }
  }

  @Nested
  class StudyUpdate {
    @Test
    @DisplayName("Update Study - Success")
    void updateSuccess() {
      // given:
      String studyId = "studyId";
      ObjectNode patch = createUpdatePatch("Updated Study Name");
      Study existingStudy = new Study("Schema URL", "1.0", "Generic", "{\"name\": \"study\"}");

      // and:
      when(studyRepository.findById(studyId)).thenReturn(Optional.of(existingStudy));
      when(metadataUpdateService.update(existingStudy, patch)).thenReturn(existingStudy);

      // when:
      Study result = studyService.update(studyId, patch);

      // then:
      verify(studyRepository).findById(studyId);
      verify(metadataUpdateService).update(existingStudy, patch);
      verify(studyEventHandler).updatedStudy(existingStudy);
      assertThat(result).isEqualTo(existingStudy);
    }

    // Helper method to create an update patch
    private ObjectNode createUpdatePatch(String updatedName) {
      ObjectNode patch = JsonNodeFactory.instance.objectNode();
      patch.put("content", JsonNodeFactory.instance.objectNode().put("name", updatedName));
      return patch;
    }

    @Test
    @DisplayName("Update Study - Not Found")
    void updateStudyNotFound() {
      // given:
      String nonExistentStudyId = "nonExistentId";
      ObjectNode patch = createUpdatePatch("Updated Study Name");

      // and:
      when(studyRepository.findById(nonExistentStudyId)).thenReturn(Optional.empty());

      // when, then:
      ResponseStatusException exception =
          assertThrows(
              ResponseStatusException.class, () -> studyService.update(nonExistentStudyId, patch));
      assertThat("404 NOT_FOUND").isEqualTo(exception.getMessage());

      // verify that other methods are not called
      verify(metadataCrudService, never()).deleteDocument(any());
      verify(studyEventHandler, never()).deletedStudy(any());
    }
  }

  @Nested
  class StudyReplace {

    @Test
    @DisplayName("Replace Study - Success")
    void replaceSuccess() {
      // given:
      String studyId = "studyId";
      Study existingStudy =
          new Study("ExistingSchema URL", "1.0", "Generic", "{\"name\": \"Existing Study Name\"}");
      Study updatedStudy =
          new Study("UpdatedSchema URL", "1.1", "Specific", "{\"name\": \"Updated Study Name\"}");
      //            Study existingStudy = new Study("{\"name\": \"Existing Study Name\"}");
      //            Study updatedStudy = new Study("{\"name\": \"Updated Study Name\"}");

      // and:
      when(studyRepository.findById(studyId)).thenReturn(Optional.of(existingStudy));

      // when:
      Study result = studyService.replace(studyId, updatedStudy);

      // then:
      verify(studyRepository).findById(studyId);
      verify(studyRepository).save(updatedStudy); // Verify save is called
      verify(studyEventHandler).updatedStudy(updatedStudy);
      assertThat(result).isEqualTo(updatedStudy);
    }

    @Test
    @DisplayName("Replace Study - Not Found")
    void replaceStudyNotFound() {
      // given:
      String nonExistentStudyId = "nonExistentId";
      Study updatedStudy =
          new Study("Schema URL", "1.1", "Specific", "{\"name\": \"Updated Study Name\"}");

      // and:
      when(studyRepository.findById(nonExistentStudyId)).thenReturn(Optional.empty());

      // when, then:
      ResponseStatusException exception =
          assertThrows(
              ResponseStatusException.class,
              () -> studyService.replace(nonExistentStudyId, updatedStudy));
      assertThat("404 NOT_FOUND").isEqualTo(exception.getMessage());

      // verify that other methods are not called
      verify(studyRepository, never()).save(any());
      verify(studyEventHandler, never()).updatedStudy(any());
    }
  }

  @Nested
  class StudyDeletion {
    @Test
    @DisplayName("Delete Study - Success")
    void deleteSuccess() {
      // given:
      String studyId = "testDeleteId";
      // String content = "{\"name\": \"delete study\"}";
      Study persistentStudy =
          new Study("Schema URL", "1.1", "Specific", "{\"name\": \"Updated Study Name\"}");

      // and:
      when(studyRepository.findById(studyId)).thenReturn(Optional.of(persistentStudy));

      // when:
      studyService.delete(studyId);

      // then:
      verify(metadataCrudService).deleteDocument(persistentStudy);
      verify(studyEventHandler).deletedStudy(studyId);
    }

    @Test
    @DisplayName("Delete Study - Not Found")
    void deleteStudyNotFound() {
      // given:
      String nonExistentStudyId = "nonExistentId";

      // and:
      when(studyRepository.findById(nonExistentStudyId)).thenReturn(Optional.empty());

      // when, then:
      ResponseStatusException exception =
          assertThrows(
              ResponseStatusException.class, () -> studyService.delete(nonExistentStudyId));
      assertThat("404 NOT_FOUND").isEqualTo(exception.getMessage());

      verify(metadataCrudService, never()).deleteDocument(any());
      verify(studyEventHandler, never()).deletedStudy(any());
    }
  }
}
