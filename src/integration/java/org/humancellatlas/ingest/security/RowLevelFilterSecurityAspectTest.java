package org.humancellatlas.ingest.security;

import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectBuilder;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.test.context.support.WithMockUser;
import wiremock.com.fasterxml.jackson.databind.util.Named;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.reset;

@SpringBootTest
@TestInstance(PER_CLASS)
@WithMockUser
class RowLevelFilterSecurityAspectTest {
    @Autowired FileRepository fileRepository;
    @Autowired BiomaterialRepository biomaterialRepository;
    @Autowired ProcessRepository processRepository;
    @Autowired ProtocolRepository protocolRepository;

    @SpyBean
    private RowLevelFilterSecurityAspect rowLevelFilterSecurityAspect;

    @MockBean
    // NOTE: Adding MigrationConfiguration as a MockBean is needed
    // as otherwise MigrationConfiguration won't be initialised.
    private MigrationConfiguration migrationConfiguration;

    @AfterEach
    public void resetSpy() {
        reset(rowLevelFilterSecurityAspect);
    }
    @Test
    public void testAdviceOnRepositoryDeclaredMethod() throws Throwable {
        try {
            fileRepository.findByProject(Project.builder().emptyProject().build());
        } catch (Exception e) {
            // ignore exceptions, we are just testing whether the Advice is called
        }

        Mockito.verify(rowLevelFilterSecurityAspect, atLeast(1))
                .applyRowLevelSecurity(Mockito.any());
    }


    @ParameterizedTest(name = "{index} {1}")
    @MethodSource("repositoryBeans")
    public void testAdviceOnRepositoryInheritedMethod(MongoRepository repository, String metadataType) throws Throwable {
        repository.findAll();

        Mockito.verify(rowLevelFilterSecurityAspect, atLeast(1))
                .applyRowLevelSecurity(Mockito.any());
    }

    private Stream<Arguments> repositoryBeans() {
        return Stream.of(
                Arguments.of(fileRepository, "file"),
                Arguments.of(biomaterialRepository, "biomaterial"),
                Arguments.of(protocolRepository, "protocol"),
                Arguments.of(processRepository, "process")
        );
    }
}
