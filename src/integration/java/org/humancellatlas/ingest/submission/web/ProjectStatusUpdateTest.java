package org.humancellatlas.ingest.submission.web;

import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.project.WranglingState;
import org.humancellatlas.ingest.security.Role;
import org.humancellatlas.ingest.state.ValidationState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import static org.humancellatlas.ingest.project.WranglingState.IN_PROGRESS;
import static org.humancellatlas.ingest.project.WranglingState.SUBMITTED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureDataMongo
@AutoConfigureMockMvc
@WithMockUser(username = "test_user", authorities={"WRANGLER"})
public class ProjectStatusUpdateTest {
    @Autowired
    private MockMvc webApp;
    @Autowired
    private ProjectRepository projectRepository;

    // NOTE: Adding MigrationConfiguration as a MockBean is needed as otherwise MigrationConfiguration won't be
    //       initialised. This is very un-elegant and should be fixed.
    @MockBean
    private MigrationConfiguration migrationConfiguration;
    UriComponentsBuilder uriBuilder;

    @BeforeEach
    void setUp() {
        uriBuilder = ServletUriComponentsBuilder.fromCurrentContextPath();
    }

    @Test
    public void test_statusIsInProgress_afterSubmissionCreation() throws Exception {
        // given
        Project project = createProject();

        // when
        String submissionUrl = createSubmission();
        connectSubmissionToProject(project, submissionUrl);

        // then
        assertProjectStatus(project, IN_PROGRESS);
    }

    @Test
    public void test_statusIsSubmitted_afterSubmissionIsExported() throws Exception {
        // given
        Project project = createProject();
        String submissionUrl = createSubmission();
        connectSubmissionToProject(project, submissionUrl);

        // when
        setSubmissionToExported(submissionUrl);

        // then
        assertProjectStatus(project, SUBMITTED);
    }

    @Test
    public void test_deleteSubmissionWorks() throws Exception {
        // given
        Project project = createProject();
        String submissionUrl = createSubmission();
        connectSubmissionToProject(project, submissionUrl);

        // when
        deleteSubmissionFromProject(submissionUrl);
        String submissionUrl2 = createSubmission();
        connectSubmissionToProject(project, submissionUrl2);

        // then
        // no errors
    }

    private void deleteSubmissionFromProject(String submissionUrl) throws Exception {
        webApp.perform(delete(submissionUrl)).andExpect(status().isAccepted());
    }


    private void setSubmissionToExported(String submissionUrl) throws Exception {
        webApp.perform(
                put(submissionUrl + Links.COMMIT_EXPORTED_URL)
        ).andExpect(status().isAccepted());
    }

    private void assertProjectStatus(Project project, WranglingState wranglingState) throws Exception {
        webApp.perform(
                get("/projects/{id}",project.getId())
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.wranglingState")
                                .value(wranglingState.getValue()));
    }

    private void connectSubmissionToProject(Project project, String submissionUrl) throws Exception {
        webApp.perform(
                post("/projects/{id}/submissionEnvelopes", project.getId())
                        .contentType("text/uri-list")
                        .content(submissionUrl)
        ).andExpect(status().isNoContent());
    }

    @Nullable
    private String createSubmission() throws Exception {
        MvcResult mvcResult = webApp.perform(
                        post("/submissionEnvelopes/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                .andExpect(status().isCreated())
                .andReturn();
        String submissionUrl = mvcResult.getResponse().getHeader("Location");
        return submissionUrl;
    }

    @NotNull
    private Project createProject() {
        Project project = new Project(null);
        project.setWranglingState(WranglingState.ELIGIBLE);
        return projectRepository.save(project);
    }
}
