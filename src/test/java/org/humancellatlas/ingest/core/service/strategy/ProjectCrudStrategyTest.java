package org.humancellatlas.ingest.core.service.strategy;

import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.core.service.strategy.impl.ProjectCrudStrategy;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ProjectCrudStrategy.class})
public class ProjectCrudStrategyTest {
    @Autowired private ProjectCrudStrategy projectCrudStrategy;

    @MockBean private ProjectRepository projectRepository;
    @MockBean private ProtocolRepository protocolRepository;
    @MockBean private ProcessRepository processRepository;
    @MockBean private FileRepository fileRepository;
    @MockBean private BiomaterialRepository biomaterialRepository;
    @MockBean private MessageRouter messageRouter;

    private Project testProject;

    @BeforeEach
    void setUp() {
        testProject = new Project(null);
    }

    @Test
    public void testRemoveLinksProject() {
        // Given
        Biomaterial biomaterialWithProject = new Biomaterial(null);
        biomaterialWithProject.setProject(testProject);
        biomaterialWithProject.getProjects().add(testProject);
        when(biomaterialRepository.findByProject(testProject)).thenReturn(Stream.of(biomaterialWithProject));
        when(biomaterialRepository.findByProjectsContaining(testProject)).thenReturn(Stream.of(biomaterialWithProject));

        File fileWithProject = new File(null, "fileWithProject");
        fileWithProject.setProject(testProject);
        when(fileRepository.findByProject(testProject)).thenReturn(Stream.of(fileWithProject));

        Process processWithProject = new Process(null);
        processWithProject.setProject(testProject);
        processWithProject.getProjects().add(testProject);
        when(processRepository.findByProject(testProject)).thenReturn(Stream.of(processWithProject));
        when(processRepository.findByProjectsContaining(testProject)).thenReturn(Stream.of(processWithProject));

        Protocol protocolWithProject = new Protocol(null);
        protocolWithProject.setProject(testProject);
        when(protocolRepository.findByProject(testProject)).thenReturn(Stream.of(protocolWithProject));

        // when
        projectCrudStrategy.removeLinksToDocument(testProject);

        //then
        assertThat(biomaterialWithProject.getProject()).isNull();
        assertThat(biomaterialWithProject.getProjects()).isEmpty();
        assertThat(fileWithProject.getProject()).isNull();
        assertThat(processWithProject.getProject()).isNull();
        assertThat(processWithProject.getProjects()).isEmpty();
        assertThat(protocolWithProject.getProject()).isNull();
        verify(biomaterialRepository, times(2)).save(biomaterialWithProject);
        verify(fileRepository).save(fileWithProject);
        verify(processRepository, times(2)).save(processWithProject);
        verify(protocolRepository).save(protocolWithProject);
    }

    @Test
    public void testDeleteProject() {
        //when
        projectCrudStrategy.deleteDocument(testProject);
        //then
        verify(projectRepository).delete(testProject);
    }
}
