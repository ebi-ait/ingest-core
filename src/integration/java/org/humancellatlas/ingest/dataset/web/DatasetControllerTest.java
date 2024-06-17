package org.humancellatlas.ingest.dataset.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.assertj.core.data.MapEntry;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.dataset.Dataset;
import org.humancellatlas.ingest.dataset.DatasetEventHandler;
import org.humancellatlas.ingest.dataset.DatasetRepository;
import org.humancellatlas.ingest.dataset.util.UploadAreaUtil;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@WithMockUser
class DatasetControllerTest {

  @Autowired private MockMvc webApp;

  @Autowired private DatasetRepository repository;

  @Autowired private FileRepository fileRepository;

  @Autowired private BiomaterialRepository biomaterialRepository;

  @Autowired private ProcessRepository processRepository;

  @Autowired private ProtocolRepository protocolRepository;

  @Autowired private MetadataCrudService metadataCrudService;

  @Autowired private ObjectMapper objectMapper;

  @SpyBean private DatasetEventHandler eventHandler;

  @MockBean private UploadAreaUtil uploadAreaUtil;

  @AfterEach
  private void tearDown() {
    repository.deleteAll();
  }

  @Nested
  class Registration {
    @Test
    @DisplayName("Register Dataset - Success")
    @WithMockUser
    void registerSuccess() throws Exception {
      doTestRegister(
          "/datasets",
          dataset -> {
            var datasetArgumentCaptor = ArgumentCaptor.forClass(Dataset.class);
            verify(eventHandler).registeredDataset(datasetArgumentCaptor.capture());
            Dataset handledDataset = datasetArgumentCaptor.getValue();
            assertThat(handledDataset.getId()).isNotNull();
          });
    }

    private void doTestRegister(String registerUrl, Consumer<Dataset> postCondition)
        throws Exception {
      // given:
      var content = new HashMap<String, Object>();
      content.put("name", "Test Dataset");

      doAnswer(invocation -> null).when(uploadAreaUtil).createDataFilesUploadArea(any());

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

      // and: verify the registered dataset content
      Map<String, Object> registeredDataset =
          objectMapper.readValue(response.getContentAsString(), Map.class);
      assertThat(registeredDataset.get("content")).isInstanceOf(Map.class);
      MapEntry<String, String> nameEntry = entry("name", "Test Dataset");
      assertThat((Map) registeredDataset.get("content")).containsOnly(nameEntry);

      // and: verify the dataset is stored in the repository
      List<Dataset> datasets = repository.findAll();
      assertThat(datasets).hasSize(1);
      Dataset storedDataset = datasets.get(0);
      assertThat((Map) storedDataset.getContent()).containsOnly(nameEntry);

      // and:
      postCondition.accept(storedDataset);
    }
  }

  @Nested
  class Update {
    @Test
    @DisplayName("Update Dataset - Success")
    void updateSuccess() throws Exception {
      doTestUpdate(
          "/datasets/{datasetId}",
          dataset -> {
            var datasetCaptor = ArgumentCaptor.forClass(Dataset.class);
            verify(eventHandler).updatedDataset(datasetCaptor.capture());
            Dataset handledDataset = datasetCaptor.getValue();
            assertThat(handledDataset.getId()).isEqualTo(dataset.getId());
          });
    }

    private void doTestUpdate(String patchUrl, Consumer<Dataset> postCondition) throws Exception {
      // given:
      var content = new HashMap<String, Object>();
      content.put("description", "test");
      Dataset dataset = new Dataset(content);
      dataset = repository.save(dataset);

      // when:
      content.put("description", "test updated");
      MvcResult result =
          webApp
              .perform(
                  patch(patchUrl, dataset.getId())
                      .contentType(APPLICATION_JSON_VALUE)
                      .content("{\"content\": " + objectMapper.writeValueAsString(content) + "}"))
              .andReturn();

      // expect:
      MockHttpServletResponse response = result.getResponse();
      assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
      assertThat(response.getContentType()).containsPattern("application/.*json.*");

      // and:
      // Using Map here because reading directly to dataset converts the entire JSON to dataset
      // content.
      Map<String, Object> updated =
          objectMapper.readValue(response.getContentAsString(), Map.class);
      assertThat(updated.get("content")).isInstanceOf(Map.class);
      MapEntry<String, String> updatedDescription = entry("description", "test updated");
      assertThat((Map) updated.get("content")).containsOnly(updatedDescription);

      // and:
      dataset = repository.findById(dataset.getId()).get();
      assertThat((Map) dataset.getContent()).containsOnly(updatedDescription);

      // and:
      postCondition.accept(dataset);
    }

    @Test
    @DisplayName("Update Dataset - Not Found")
    void updateDatasetNotFound() throws Exception {
      // given:
      var content = new HashMap<String, Object>();
      content.put("description", "nonExistentDataset");

      Dataset nonExistentDataset = new Dataset(content);

      // when:
      MvcResult result =
          webApp
              .perform(
                  patch("/datasets/{datasetId}", nonExistentDataset.getId())
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
    @DisplayName("Delete dataset - Success")
    void deleteSuccess() throws Exception {
      // given:
      String content = "{\"name\": \"delete dataset\"}";
      Dataset persistentDataset = new Dataset(content);
      repository.save(persistentDataset);
      String existingDatasetId = persistentDataset.getId();

      // when:
      webApp
          .perform(delete("/datasets/{datasetId}", existingDatasetId))
          .andExpect(status().isNoContent());

      // then:
      assertThat(repository.findById(existingDatasetId)).isEmpty();
      MetadataDocument document =
          metadataCrudService.findOriginalByUuid(
              String.valueOf(persistentDataset.getUuid()), EntityType.DATASET);
      assertNull(document);
      verify(eventHandler).deletedDataset(existingDatasetId);
    }

    @Test
    @DisplayName("Delete dataset - Not Found")
    void deleteDatasetNotFound() throws Exception {
      // given: a non-existent dataset id
      String nonExistentDatasetId = "nonExistentId";

      // when:
      MvcResult result =
          webApp.perform(delete("/datasets/{datasetId}", nonExistentDatasetId)).andReturn();

      // then:
      MockHttpServletResponse response = result.getResponse();
      assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }
  }

  @Nested
  class Link {
    @Test
    @WithMockUser
    @DisplayName("Link file to dataset - Success")
    void lintFileToDatasetSuccess() throws Exception {
      // given:
      String datasetContent = "{\"name\": \"dataset\"}";
      Dataset persistentDataset = new Dataset(datasetContent);
      repository.save(persistentDataset);
      String datasetId = persistentDataset.getId();

      String fileContent = "{\"name\": \"path\", \"file_type\"}";
      File persistentFile = new File(fileContent, "test.fasta");
      fileRepository.save(persistentFile);
      String fileId = persistentFile.getId();

      // when:
      webApp
          .perform(put("/datasets/{dataset_id}/file/{file_id}", datasetId, fileId))
          .andExpect(status().isAccepted());
    }

    @Test
    @WithMockUser
    @DisplayName("Link biomaterial to dataset - Success")
    void lintBiomaterialToDatasetSuccess() throws Exception {
      // given:
      String datasetContent = "{\"name\": \"dataset\"}";
      Dataset persistentDataset = new Dataset(datasetContent);
      repository.save(persistentDataset);
      String datasetId = persistentDataset.getId();

      String biomaterialContent = "{\"name\": \"test\"}";
      Biomaterial persistentBiomaterial = new Biomaterial(biomaterialContent);
      biomaterialRepository.save(persistentBiomaterial);
      String biomaterialId = persistentBiomaterial.getId();

      // when:
      webApp
          .perform(
              put("/datasets/{dataset_id}/biomaterial/{biomaterial_id}", datasetId, biomaterialId))
          .andExpect(status().isAccepted());
    }
  }
}
