package org.humancellatlas.ingest.project;

import org.assertj.core.api.Assertions;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.project.exception.NonEmptyProject;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        ProjectService.class,
        ProjectRepository.class,
        SubmissionEnvelopeRepository.class
})
public class ProjectServiceTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ProjectService projectService;

    @MockBean
    private SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private MetadataCrudService metadataCrudService;

    @MockBean
    private MetadataUpdateService metadataUpdateService;

    @MockBean
    private BundleManifestRepository bundleManifestRepository;

    @BeforeEach
    void setUp() {
        applicationContext.getBeansWithAnnotation(MockBean.class).forEach(Mockito::reset);
    }

    @Nested
    class Deletion {

        @Test
        @DisplayName("success")
        void succeedForEmptyProject() throws Exception {
            //given:
            var project = new Project("{\"name\": \"test\"}");

            //when:
            projectService.delete(project);

            //then:
            verify(projectRepository).delete(project);
        }

        @Test
        @DisplayName("fails for non-empty Project")
        void failForProjectWithSubmissions() {
            //given:
            var project = new Project("test project");

            //and:
            var persistentProject = new Project(null);
            BeanUtils.copyProperties(project, persistentProject);
            IntStream.range(0, 3)
                    .mapToObj(Integer::toString).map(SubmissionEnvelope::new)
                    .forEach(persistentProject::addToSubmissionEnvelopes);
            doReturn(Stream.of(persistentProject))
                    .when(projectRepository).findByUuid(project.getUuid());

            //expect:
            Assertions.assertThatThrownBy(() -> {
                projectService.delete(project);
            }).isInstanceOf(NonEmptyProject.class);
        }

    }

}
