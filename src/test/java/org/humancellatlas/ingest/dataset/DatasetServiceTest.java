package org.humancellatlas.ingest.dataset;

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

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {DatasetService.class, DatasetRepository.class})
public class DatasetServiceTest {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DatasetService datasetService;

    @MockBean
    private MongoTemplate mongoTemplate;

    @MockBean
    private DatasetRepository datasetRepository;

    @MockBean
    private DatasetEventHandler datasetEventHandler;

    @MockBean
    private MetadataCrudService metadataCrudService;

    @MockBean
    private MetadataUpdateService metadataUpdateService;

    @BeforeEach
    void setUp() {
        applicationContext.getBeansWithAnnotation(MockBean.class).forEach(Mockito::reset);
        Mockito.reset(metadataCrudService, datasetRepository, datasetEventHandler);
    }

    @Nested
    class DatasetRegistration {

        @Test
        @DisplayName("Register Dataset - Success")
        void registerSuccess() {
            //given:
            String content = "{\"name\": \"dataset\"}";
            Dataset dataset = new Dataset(content);

            //and:
            Dataset persistentDataset = new Dataset(content);
            doReturn(persistentDataset).when(datasetRepository).save(dataset);

            //when:
            Dataset result = datasetService.register(dataset);

            //then:
            verify(datasetRepository, times(1)).save(dataset);
            assertThat(result).isEqualTo(persistentDataset);
            verify(datasetEventHandler).registeredDataset(persistentDataset);
        }

    }

    @Nested
    class DatasetUpdate {
        @Test
        @DisplayName("Update Dataset - Success")
        void updateSuccess() {
            // given:
            String datasetId = "datasetId";
            ObjectNode patch = createUpdatePatch("Updated Dataset Name");
            Dataset existingDataset = new Dataset("{\"name\": \"dataset\"}");

            // and:
            when(datasetRepository.findById(datasetId)).thenReturn(Optional.of(existingDataset));
            when(metadataUpdateService.update(existingDataset, patch)).thenReturn(existingDataset);

            // when:
            Dataset result = datasetService.update(datasetId, patch);

            // then:
            verify(datasetRepository).findById(datasetId);
            verify(metadataUpdateService).update(existingDataset, patch);
            verify(datasetEventHandler).updatedDataset(existingDataset);
            assertThat(result).isEqualTo(existingDataset);
        }

        // Helper method to create an update patch
        private ObjectNode createUpdatePatch(String updatedName) {
            ObjectNode patch = JsonNodeFactory.instance.objectNode();
            patch.put("content", JsonNodeFactory.instance.objectNode().put("name", updatedName));
            return patch;
        }

        @Test
        @DisplayName("Update Dataset - Not Found")
        void updateDatasetNotFound() {
            // given:
            String nonExistentDatasetId = "nonExistentId";
            ObjectNode patch = createUpdatePatch("Updated Dataset Name");

            // and:
            when(datasetRepository.findById(nonExistentDatasetId)).thenReturn(Optional.empty());

            // when, then:
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> datasetService.update(nonExistentDatasetId, patch));
            assertThat(Objects.requireNonNull(exception.getMessage()).contains("404 NOT_FOUND"));

            // verify that other methods are not called
            verify(metadataCrudService, never()).deleteDocument(any());
            verify(datasetEventHandler, never()).deletedDataset(any());
        }
    }

    @Nested
    class DatasetReplace {

        @Test
        @DisplayName("Replace Dataset - Success")
        void replaceSuccess() {
            // given:
            String datasetId = "datasetId";
            Dataset existingDataset = new Dataset("{\"name\": \"Existing Dataset Name\"}");
            Dataset updatedDataset = new Dataset("{\"name\": \"Updated Dataset Name\"}");

            // and:
            when(datasetRepository.findById(datasetId)).thenReturn(Optional.of(existingDataset));

            // when:
            Dataset result = datasetService.replace(datasetId, updatedDataset);

            // then:
            verify(datasetRepository).findById(datasetId);
            verify(datasetRepository).save(updatedDataset);  // Verify save is called
            verify(datasetEventHandler).updatedDataset(updatedDataset);
            assertThat(result).isEqualTo(updatedDataset);
        }

        @Test
        @DisplayName("Replace Dataset - Not Found")
        void replaceDatasetNotFound() {
            // given:
            String nonExistentDatasetId = "nonExistentId";
            Dataset updatedDataset = new Dataset("{\"name\": \"Updated Dataset Name\"}");

            // and:
            when(datasetRepository.findById(nonExistentDatasetId)).thenReturn(Optional.empty());

            // when, then:
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> datasetService.replace(nonExistentDatasetId, updatedDataset));
            assertThat(Objects.requireNonNull(exception.getMessage()).contains("404 NOT_FOUND"));

            // verify that other methods are not called
            verify(datasetRepository, never()).save(any());
            verify(datasetEventHandler, never()).updatedDataset(any());
        }

    }

    @Nested
    class DatasetDeletion {
        @Test
        @DisplayName("Delete Dataset - Success")
        void deleteSuccess() {
            // given:
            String datasteId = "testDeleteId";
            String content = "{\"name\": \"delete dataset\"}";
            Dataset persistentDataset = new Dataset(content);

            // and:
            when(datasetRepository.findById(datasteId)).thenReturn(Optional.of(persistentDataset));

            // when:
            datasetService.delete(datasteId);

            // then:
            verify(metadataCrudService).deleteDocument(persistentDataset);
            verify(datasetEventHandler).deletedDataset(datasteId);

        }

        @Test
        @DisplayName("Delete Dataset - Not Found")
        void deleteDatasetNotFound() {
            // given:
            String nonExistentDatasetId = "nonExistentId";

            // and:
            when(datasetRepository.findById(nonExistentDatasetId)).thenReturn(Optional.empty());

            // when, then:
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> datasetService.delete(nonExistentDatasetId));
            assertThat(Objects.requireNonNull(exception.getMessage()).contains("404 NOT_FOUND"));

            verify(metadataCrudService, never()).deleteDocument(any());
            verify(datasetEventHandler, never()).deletedDataset(any());
        }
    }
}