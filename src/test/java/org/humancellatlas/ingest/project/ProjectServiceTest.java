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

import java.util.ArrayList;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

    @MockBean
    private ProjectEventHandler projectEventHandler;

    @BeforeEach
    void setUp() {
        applicationContext.getBeansWithAnnotation(MockBean.class).forEach(Mockito::reset);
    }

    @Nested
    class SubmissionEnvelopes {

        @Test
        @DisplayName("get all submissions")
        void getFromAllCopiesOfProjects() {
            //given:
            var project1 = new Project("project");
            var submissionSet1 = IntStream.range(0, 3)
                    .mapToObj(Integer::toString)
                    .map(SubmissionEnvelope::new)
                    .collect(toSet());

            submissionSet1.forEach(submission ->  project1.addToSubmissionEnvelopes(submission));

            //and:
            var project2 = new Project(null);
            BeanUtils.copyProperties(project1, project2);
            var submissionSet2 = IntStream.range(10, 15)
                    .mapToObj(Integer::toString)
                    .map(SubmissionEnvelope::new)
                    .collect(toSet());
            submissionSet2.forEach(submission -> project2.addToSubmissionEnvelopes(submission));

            //and:
            doReturn(Stream.of(project1, project2))
                    .when(projectRepository).findByUuid(project1.getUuid());

            //when:
            var submissionEnvelopes = projectService.getSubmissionEnvelopes(project1);

            //then:
            assertThat(submissionEnvelopes)
                    .containsAll(submissionSet1)
                    .containsAll(submissionSet2);
        }

        @Test
        @DisplayName("no duplicate submissions")
        void getFromAllCopiesOfProjectsNoDuplicates() {
            //given:
            var project1 = new Project("project1");
            var submissionSet1 = IntStream.range(0, 3)
                .mapToObj(Integer::toString)
                .map(SubmissionEnvelope::new)
                .collect(toSet());
            submissionSet1.forEach(project1::addToSubmissionEnvelopes);

            //and:
            var project2 = new Project("Project2");
            BeanUtils.copyProperties(project1, project2);
            var submissionSet2 = IntStream.range(10, 15)
                .mapToObj(Integer::toString)
                .map(SubmissionEnvelope::new)
                .collect(toSet());
            submissionSet2.forEach(project2::addToSubmissionEnvelopes);

            var project3 = new Project(null);
            BeanUtils.copyProperties(project1, project3);
            submissionSet1.forEach(submission -> {
                project3.addToSubmissionEnvelopes(new SubmissionEnvelope(submission.getId()));
            });

            var documentIds = new ArrayList<String>();
            submissionSet1.forEach(submission -> documentIds.add(submission.getId()));
            submissionSet2.forEach(submission -> documentIds.add(submission.getId()));

            //and:
            doReturn(Stream.of(project1, project2, project3))
                .when(projectRepository).findByUuid(project1.getUuid());

            //when:
            var submissionEnvelopes = projectService.getSubmissionEnvelopes(project1);
            var returnDocumentIds = new ArrayList<String>();
            submissionEnvelopes.forEach(submission -> returnDocumentIds.add(submission.getId()));

            //then:
            assertThat(returnDocumentIds)
                .containsExactlyInAnyOrderElementsOf(documentIds);
        }

    }

    @Nested
    class Registration {

        @Test
        @DisplayName("success")
        void succeed() {
            //given:
            var project = new Project("{\"name\": \"project\"}");

            //when:
            projectService.register(project);

            //then:
            verify(projectRepository).save(any(Project.class));
        }

    }

    @Nested
    class Deletion {

        @Test
        @DisplayName("success")
        void succeedForEmptyProject() throws Exception {
            //given:
            var project = new Project("{\"name\": \"test\"}");

            //and:
            var persistentProjects = IntStream.range(0, 3)
                    .mapToObj(number -> new Project(null))
                    .collect(toList());
            persistentProjects.forEach(persistentProject -> {
                BeanUtils.copyProperties(project, persistentProject);
            });
            doReturn(persistentProjects.stream())
                    .when(projectRepository).findByUuid(project.getUuid());

            //when:
            projectService.delete(project);

            //then:
            persistentProjects.forEach(persistentProject -> {
                verify(projectRepository).delete(persistentProject);
            });
        }

        @Test
        @DisplayName("fails for non-empty Project")
        void failForProjectWithSubmissions() {
            //given:
            var project = new Project("test project");

            //and: copy of project with no submissions
            var persistentEmptyProject = new Project(null);
            BeanUtils.copyProperties(project, persistentEmptyProject);

            //and: copy of project with submissions
            var persistentNonEmptyProject = new Project(null);
            BeanUtils.copyProperties(project, persistentNonEmptyProject);
            IntStream.range(0, 3)
                    .mapToObj(Integer::toString).map(SubmissionEnvelope::new)
                    .forEach(persistentNonEmptyProject::addToSubmissionEnvelopes);

            //and:
            doReturn(Stream.of(persistentEmptyProject, persistentNonEmptyProject))
                    .when(projectRepository).findByUuid(project.getUuid());

            //expect:
            Assertions.assertThatThrownBy(() -> {
                projectService.delete(project);
            }).isInstanceOf(NonEmptyProject.class);
        }

    }

}
