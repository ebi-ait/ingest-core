package org.humancellatlas.ingest.study;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        StudyService.class,
        StudyRepository.class
})
public class StudyServiceTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private StudyService studyService;

    @MockBean
    private MongoTemplate mongoTemplate;

    @MockBean
    private StudyRepository studyRepository;

    @MockBean
    private StudyEventHandler studyEventHandler;

    @MockBean
    private MetadataCrudService metadataCrudService;

    @MockBean
    private MetadataUpdateService metadataUpdateService;

    @BeforeEach
    void setUp() {
        applicationContext.getBeansWithAnnotation(MockBean.class).forEach(Mockito::reset);
        Mockito.reset(metadataCrudService, studyRepository,studyEventHandler);
    }

    @Nested
    class StudyRegistration {

        @Test
        @DisplayName("Register Study - Success")
        void registerSuccess() {
            //given:
            String content = "{\"name\": \"study\"}";
            Study study = new Study(content);

            //and:
            Study persistentStudy = new Study(content);
            doReturn(persistentStudy).when(studyRepository).save(study);

            //when:
            Study result = studyService.register(study);

            //then:
            verify(studyRepository, times(1)).save(study);
            assertThat(result).isEqualTo(persistentStudy);
            verify(studyEventHandler).registeredStudy(persistentStudy);
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
            Study existingStudy = new Study("{\"name\": \"study\"}");

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
            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> studyService.update(nonExistentStudyId, patch)
            );
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
            Study existingStudy = new Study("{\"name\": \"Existing Study Name\"}");
            Study updatedStudy = new Study("{\"name\": \"Updated Study Name\"}");

            // and:
            when(studyRepository.findById(studyId)).thenReturn(Optional.of(existingStudy));

            // when:
            Study result = studyService.replace(studyId, updatedStudy);

            // then:
            verify(studyRepository).findById(studyId);
            verify(studyRepository).save(updatedStudy);  // Verify save is called
            verify(studyEventHandler).updatedStudy(updatedStudy);
            assertThat(result).isEqualTo(updatedStudy);
        }

        @Test
        @DisplayName("Replace Study - Not Found")
        void replaceStudyNotFound() {
            // given:
            String nonExistentStudyId = "nonExistentId";
            Study updatedStudy = new Study("{\"name\": \"Updated Study Name\"}");

            // and:
            when(studyRepository.findById(nonExistentStudyId)).thenReturn(Optional.empty());

            // when, then:
            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> studyService.replace(nonExistentStudyId, updatedStudy)
            );
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
            String content = "{\"name\": \"delete study\"}";
            Study persistentStudy = new Study(content);

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
            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> studyService.delete(nonExistentStudyId)
            );
            assertThat("404 NOT_FOUND").isEqualTo(exception.getMessage());

            verify(metadataCrudService, never()).deleteDocument(any());
            verify(studyEventHandler, never()).deletedStudy(any());
        }
    }
}
