package org.humancellatlas.ingest.project;

import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
        void testDelete() {
            //given:
            var project = new Project("{\"name\": \"test\"}");

            //when:
            projectService.delete(project);

            //then:
            verify(projectRepository).delete(project);
        }

    }

}
