package org.humancellatlas.ingest.study;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.humancellatlas.ingest.study.web.StudyController;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {StudyController.class, StudyRepository.class})
@Disabled("Disabled until the morphic profile is active")
public class StudyControllerProfileTest {

  @MockBean private StudyService studyService;

  @MockBean private StudyRepository studyRepository;

  @MockBean private PersistentEntityResourceAssembler assembler;

  @Autowired private StudyController studyController;

  @MockBean private Environment environment;

  @BeforeEach
  void setUp() {
    Mockito.reset(studyService, assembler);
  }

  @Nested
  class StudyUpdate {
    @Test
    @DisplayName("Update Study - True Morphic Profile")
    public void testUpdateStudyMorphicProfile() {
      // given:
      String studyId = "studyId";
      ObjectNode patch = createUpdatePatch("Updated Study Name");
      // Study updatedStudy = new Study("{\"name\": \"study\"}");
      Study newStudy = new Study("Schema URL", "1.0", "Generic", "{\"name\": \"study\"}");
      Study updatedStudy = new Study("Schema URL", "1.0", "Generic", "{\"name\": \"study\"}");

      // and: mock the environment to simulate the "morphic" profile being active
      when(environment.getActiveProfiles()).thenReturn(new String[] {"morphic"});

      // when:
      when(studyService.update(newStudy, patch)).thenReturn(updatedStudy);
      ResponseEntity<Resource<?>> response =
          studyController.updateStudy(newStudy.getId(), patch, assembler);

      // then:
      assertEquals(HttpStatus.OK, response.getStatusCode());
      verify(assembler).toFullResource(updatedStudy);
    }

    @Test
    @DisplayName("Update Study - False Morphic Profile")
    public void testUpdateStudyNonMorphicProfile() {
      // given:
      Study newStudy = new Study("Schema URL", "1.0", "Generic", "{\"name\": \"study\"}");
      ObjectNode patch = createUpdatePatch("Updated Study Name");

      // and: mock the environment to simulate the "morphic" profile being active
      when(environment.getActiveProfiles()).thenReturn(new String[] {"non-morphic"});

      // when:
      ResponseEntity<Resource<?>> response =
          studyController.updateStudy(newStudy.getId(), patch, assembler);

      // then:
      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
      verify(studyService, never()).update(any(), any());
      verify(assembler, never()).toFullResource(any());
    }

    private ObjectNode createUpdatePatch(String updatedName) {
      ObjectNode patch = JsonNodeFactory.instance.objectNode();
      patch.put("content", JsonNodeFactory.instance.objectNode().put("name", updatedName));
      return patch;
    }
  }

  @Nested
  class StudyDeletion {
    @Test
    @DisplayName("Delete Study - True Morphic Profile")
    public void testDeleteStudyMorphicProfile() {
      // given :
      String studyId = "studyId";

      // and: mock the environment to simulate the "morphic" profile being active
      when(environment.getActiveProfiles()).thenReturn(new String[] {"morphic"});

      // when:
      ResponseEntity<Void> response = studyController.deleteStudy(studyId);

      // then:
      assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
      verify(studyService).delete(studyId);
    }

    @Test
    @DisplayName("Delete Study - False Morphic Profile")
    public void testDeleteStudyNonMorphicProfile() {
      // given :
      String studyId = "studyId";

      // and: mock the environment to simulate the "morphic" profile being active
      when(environment.getActiveProfiles()).thenReturn(new String[] {"non-morphic"});

      // when:
      ResponseEntity<Void> response = studyController.deleteStudy(studyId);

      // then:
      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
      verify(studyService, never()).delete(any());
    }
  }
}
