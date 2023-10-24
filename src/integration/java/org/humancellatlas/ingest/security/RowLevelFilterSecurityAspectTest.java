package org.humancellatlas.ingest.security;

import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RowLevelFilterSecurityAspectTest {
    @Autowired FileRepository fileRepository;
    @SpyBean
    private RowLevelFilterSecurityAspect rowLevelFilterSecurityAspect;

    @MockBean
    // NOTE: Adding MigrationConfiguration as a MockBean is needed
    // as otherwise MigrationConfiguration won't be initialised.
    private MigrationConfiguration migrationConfiguration;

    @Test
    public void testAdviceOnRepositoryDeclaredMethod() throws Throwable {
        try {
            fileRepository.findByProject(Project.builder().emptyProject().build());
        } catch (Exception e) {
            // ignore exceptions, we are just testing whether the Advice is called
        }

        Mockito.verify(rowLevelFilterSecurityAspect)
                .applyRowLevelSecurity(Mockito.any());
    }

    @Test
    public void testAdviceOnRepositoryInheritedMethod() throws Throwable {
        fileRepository.findAll();

        Mockito.verify(rowLevelFilterSecurityAspect)
                .applyRowLevelSecurity(Mockito.any());
    }
}
