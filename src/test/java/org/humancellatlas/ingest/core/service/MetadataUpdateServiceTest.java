package org.humancellatlas.ingest.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.humancellatlas.ingest.ProjectJson;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.patch.JsonPatcher;
import org.humancellatlas.ingest.patch.PatchService;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.state.ValidationState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.rest.webmvc.mapping.Associations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {MetadataUpdateService.class, JsonPatcher.class, ObjectMapper.class})
public class MetadataUpdateServiceTest {
  @Autowired private MetadataUpdateService service;

  @MockBean private MetadataDifferService metadataDifferService;

  @MockBean private MetadataCrudService metadataCrudService;

  @Autowired private JsonPatcher jsonPatcher;

  @MockBean private PatchService patchService;

  @MockBean private ValidationStateChangeService validationStateChangeService;

  @MockBean PersistentEntities persistentEntities;

  @MockBean private Associations associations;

  @Test
  public void testUpdateShouldSaveAndReturnUpdatedMetadata() {
    // given:
    JsonNode content = ProjectJson.fromTitle("Old Project Title").toObjectNode().get("content");
    Project project = new Project(content);

    ObjectNode patch = ProjectJson.fromTitle("New Project Title").toObjectNode();

    when(metadataCrudService.save(any())).thenReturn(project);

    // when:
    Project updatedProject = service.update(project, patch);

    // then:
    assertThat(updatedProject).isEqualTo(project);
    verify(metadataCrudService).save(project);
  }

  @Test
  public void testUpdateShouldSetStateToDraftWhenContentChanged() {
    // given:
    JsonNode content = ProjectJson.fromTitle("Old Project Title").toObjectNode().get("content");
    Project project = new Project(content);

    ObjectNode patch = ProjectJson.fromTitle("New Project Title").toObjectNode();

    when(metadataCrudService.save(any())).thenReturn(project);

    // when:
    Project updatedProject = service.update(project, patch);

    // then:
    assertThat(updatedProject).isEqualTo(project);
    verify(metadataCrudService).save(project);
    verify(validationStateChangeService)
        .changeValidationState(project.getType(), project.getId(), ValidationState.DRAFT);
  }

  @Test
  public void testUpdateShouldNotSetStateWhenContentIsUnchanged() {
    // given:
    JsonNode content = ProjectJson.fromTitle("Old Project Title").toObjectNode().get("content");
    Project project = new Project(content);

    ObjectNode patch = ProjectJson.fromTitle("Old Project Title").toObjectNode();

    when(metadataCrudService.save(any())).thenReturn(project);

    // when:
    Project updatedProject = service.update(project, patch);

    // then:
    assertThat(updatedProject).isEqualTo(project);
    verify(metadataCrudService).save(project);
    verify(validationStateChangeService, never())
        .changeValidationState(project.getType(), project.getId(), ValidationState.DRAFT);
  }

  @Test
  public void testUpdateShouldNotSetStateWhenNoContent() {
    // given:
    JsonNode content = ProjectJson.fromTitle("Old Project Title").toObjectNode().get("content");
    Project project = new Project(content);

    ObjectMapper mapper = new ObjectMapper();
    ObjectNode patch = mapper.createObjectNode();
    patch.put("isInCatalogue", false);

    when(metadataCrudService.save(any())).thenReturn(project);

    // when:
    Project updatedProject = service.update(project, patch);

    // then:
    assertThat(updatedProject).isEqualTo(project);
    verify(metadataCrudService).save(project);
    verify(validationStateChangeService, never())
        .changeValidationState(project.getType(), project.getId(), ValidationState.DRAFT);
  }

  @Test
  public void testUpdateProjectWithSupplementaryFileShouldNotThrowRecursionError() {
    // given:
    JsonNode content =
        ProjectJson.fromTitle("Project with supplementary file").toObjectNode().get("content");
    Project project = new Project(content);

    File supplementaryFile = new File(null, "fileName");
    supplementaryFile.setProject(project);
    project.getSupplementaryFiles().add(supplementaryFile);

    ObjectNode patch =
        ProjectJson.fromTitle("Updated project with supplementary file").toObjectNode();

    when(metadataCrudService.save(any())).thenReturn(project);

    // when:
    Project updatedProject = service.update(project, patch);

    // then:
    assertThat(updatedProject).isEqualTo(project);
    verify(metadataCrudService).save(project);
    verify(validationStateChangeService)
        .changeValidationState(project.getType(), project.getId(), ValidationState.DRAFT);
  }
}
