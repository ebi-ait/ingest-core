package org.humancellatlas.ingest.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {MetadataUpdateService.class})
public class MetadataUpdateServiceTest {
    @Autowired
    private MetadataUpdateService service;

    @MockBean
    private MetadataDifferService metadataDifferService;

    @MockBean
    private MetadataCrudService metadataCrudService;

    @MockBean
    private JsonPatcher jsonPatcher;

    @MockBean
    private PatchService patchService;


    @MockBean
    private ValidationStateChangeService validationStateChangeService;

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testUpdateShouldSaveAndReturnUpdatedMetadata() {
        //given:
        ObjectNode content = createProjectMetadataWithTitle("Old Project Title");
        Project project = new Project(content);

        ObjectNode patch = createProjectMetadataWithTitle("New Project Title");

        when(metadataCrudService.save(any())).thenReturn(project);
        when(jsonPatcher.merge(any(ObjectNode.class), any())).thenReturn(project);

        //when:
        Project updatedProject = service.update(project, patch);

        //then:
        assertThat(updatedProject).isEqualTo(project);
        verify(metadataCrudService).save(project);
    }

    @Test
    public void testUpdateShouldSetStateToDraftWhenContentChanged() {
        //given:
        ObjectNode content = createProjectMetadataWithTitle("Old Project Title");
        Project project = new Project(content);

        ObjectNode newContent = createProjectMetadataWithTitle("New Project Title");
        ObjectNode patch = mapper.createObjectNode();
        patch.set("content", newContent);

        when(metadataCrudService.save(any())).thenReturn(project);
        when(jsonPatcher.merge(any(ObjectNode.class), any())).thenReturn(project);

        //when:
        Project updatedProject = service.update(project, patch);

        //then:
        assertThat(updatedProject).isEqualTo(project);
        verify(metadataCrudService).save(project);
        verify(validationStateChangeService).changeValidationState(project.getType(), project.getId(), ValidationState.DRAFT);
    }

    @Test
    public void testUpdateShouldNotSetStateWhenContentIsUnchanged() {
        //given:
        ObjectNode content = createProjectMetadataWithTitle("Old Project Title");
        Project project = new Project(content);

        ObjectNode newContent = createProjectMetadataWithTitle("Old Project Title");
        ObjectNode patch = mapper.createObjectNode();
        patch.set("content", newContent);

        when(metadataCrudService.save(any())).thenReturn(project);
        when(jsonPatcher.merge(any(ObjectNode.class), any())).thenReturn(project);

        //when:
        Project updatedProject = service.update(project, patch);

        //then:
        assertThat(updatedProject).isEqualTo(project);
        verify(metadataCrudService).save(project);
        verify(validationStateChangeService, never()).changeValidationState(project.getType(), project.getId(), ValidationState.DRAFT);
    }

    @Test
    public void testUpdateShouldNotSetStateWhenNoContent() {
        //given:
        ObjectNode content = createProjectMetadataWithTitle("Old Project Title");
        Project project = new Project(content);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode patch = mapper.createObjectNode();
        patch.put("releaseDate", "2021-02-03T04:35:39.641Z");

        when(metadataCrudService.save(any())).thenReturn(project);
        when(jsonPatcher.merge(any(ObjectNode.class), any())).thenReturn(project);

        //when:
        Project updatedProject = service.update(project, patch);

        //then:
        assertThat(updatedProject).isEqualTo(project);
        verify(metadataCrudService).save(project);
        verify(validationStateChangeService, never()).changeValidationState(project.getType(), project.getId(), ValidationState.DRAFT);
    }

    @Test
    public void testUpdateProjectWithSupplementaryFileShouldNotThrowRecursionError() {
        //given:
        ObjectNode content = createProjectMetadataWithTitle("Project with supplementary file");
        Project project = new Project(content);
        File supplementaryFile = new File();
        supplementaryFile.setProject(project);
        project.getSupplementaryFiles().add(supplementaryFile);

        ObjectNode newContent = createProjectMetadataWithTitle("Updated project with supplementary file");
        ObjectNode patch = mapper.createObjectNode();
        patch.set("content", newContent);

        when(metadataCrudService.save(any())).thenReturn(project);
        when(jsonPatcher.merge(any(ObjectNode.class), any())).thenReturn(project);

        //when:
        Project updatedProject = service.update(project, patch);

        //then:
        assertThat(updatedProject).isEqualTo(project);
        verify(metadataCrudService).save(project);
        verify(validationStateChangeService).changeValidationState(project.getType(), project.getId(), ValidationState.DRAFT);
    }

    private ObjectNode createProjectMetadataWithTitle(String title) {
        ObjectNode content = mapper.createObjectNode();
        ObjectNode projectCore0 = content.putObject("project_core");
        projectCore0.put("project_title", title);
        return content;
    }


}
