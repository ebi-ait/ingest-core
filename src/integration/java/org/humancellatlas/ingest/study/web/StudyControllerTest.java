package org.humancellatlas.ingest.study.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.data.MapEntry;
import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.study.Study;
import org.humancellatlas.ingest.study.StudyEventHandler;
import org.humancellatlas.ingest.study.StudyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("morphic")
class StudyControllerTest {

    @Autowired
    private MockMvc webApp;

    @Autowired
    private StudyRepository repository;

    @Autowired
    private MetadataCrudService metadataCrudService;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private StudyEventHandler studyEventHandler;

    @MockBean
    private MigrationConfiguration migrationConfiguration;

    @AfterEach
    private void tearDown() {
        repository.deleteAll();
    }

    @Nested
    class Registration {

        @Test
        @DisplayName("Register Study - Success")
        void registerSuccess() throws Exception {
            doTestRegister("/studies", study -> {
                var studyCaptor = ArgumentCaptor.forClass(Study.class);
                verify(studyEventHandler).registeredStudy(studyCaptor.capture());
                Study handledStudy = studyCaptor.getValue();
                assertThat(handledStudy.getId()).isNotNull();
            });
        }

        @Test
        private void doTestRegister(String registerUrl, Consumer<Study> postCondition) throws Exception {
            // given:
            var content = new HashMap<String, Object>();
            content.put("name", "Test Study");

            // when:
            MvcResult result = webApp
                    .perform(post(registerUrl)
                            .contentType(APPLICATION_JSON_VALUE)
                            .content("{\"content\": " + objectMapper.writeValueAsString(content) + "}"))
                    .andReturn();

            // then:
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
            assertThat(response.getContentType()).containsPattern("application/.*json.*");

            // and: verify the registered study content
            Map<String, Object> registeredStudy = objectMapper.readValue(response.getContentAsString(), Map.class);
            assertThat(registeredStudy.get("content")).isInstanceOf(Map.class);
            MapEntry<String, String> nameEntry = entry("name", "Test Study");
            assertThat((Map) registeredStudy.get("content")).containsOnly(nameEntry);

            // and: verify the study is stored in the repository
            List<Study> studies = repository.findAll();
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
            doTestUpdate("/studies/{studyId}", study -> {
                var studyCaptor = ArgumentCaptor.forClass(Study.class);
                verify(studyEventHandler).updatedStudy(studyCaptor.capture());
                Study handledStudy = studyCaptor.getValue();
                assertThat(handledStudy.getId()).isEqualTo(study.getId());
            });
        }

        private void doTestUpdate(String patchUrl, Consumer<Study> postCondition) throws Exception {
            //given:
            var content = new HashMap<String, Object>();
            content.put("description", "test");
            Study study = new Study(content);
            study = repository.save(study);

            //when:
            content.put("description", "test updated");
            MvcResult result = webApp
                    .perform(patch(patchUrl, study.getId())
                            .contentType(APPLICATION_JSON_VALUE)
                            .content("{\"content\": " + objectMapper.writeValueAsString(content) + "}"))
                    .andReturn();

            //expect:
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
            assertThat(response.getContentType()).containsPattern("application/.*json.*");

            //and:
            //Using Map here because reading directly to Study converts the entire JSON to Study.content.
            Map<String, Object> updated = objectMapper.readValue(response.getContentAsString(), Map.class);
            assertThat(updated.get("content")).isInstanceOf(Map.class);
            MapEntry<String, String> updatedDescription = entry("description", "test updated");
            assertThat((Map) updated.get("content")).containsOnly(updatedDescription);

            //and:
            study = repository.findById(study.getId()).get();
            assertThat((Map) study.getContent()).containsOnly(updatedDescription);

            //and:
            postCondition.accept(study);
        }

        @Test
        @DisplayName("Update Study - Not Found")
        void updateStudyNotFound() throws Exception {
            // given:
            String nonExistentStudyId = "nonExistentId";

            // when:
            MvcResult result = webApp
                    .perform(patch("/studies/{studyId}", nonExistentStudyId)
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
            Study persistentStudy = new Study(content);
            repository.save(persistentStudy);
            String existingStudyId = persistentStudy.getId();

            // when:
            webApp.perform(delete("/studies/{studyId}", existingStudyId))
                    .andExpect(status().isNoContent());

            // then:
            assertThat(repository.findById(existingStudyId)).isEmpty();
            MetadataDocument document = metadataCrudService.findOriginalByUuid(
                    String.valueOf(persistentStudy.getUuid()), EntityType.STUDY);
            assertNull(document);
            verify(studyEventHandler).deletedStudy(existingStudyId);
        }

        @Test
        @DisplayName("Delete Study - Not Found")
        void deleteStudyNotFound() throws Exception {
            // given: a non-existent study id
            String nonExistentStudyId = "nonExistentId";

            // when:
            MvcResult result = webApp.perform(delete("/studies/{studyId}", nonExistentStudyId))
                    .andReturn();

            // then:
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        }
    }

}