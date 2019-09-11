package org.humancellatlas.ingest.stagingjob.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.humancellatlas.ingest.stagingjob.StagingJob;
import org.humancellatlas.ingest.stagingjob.StagingJobService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.config.PersistentEntityResourceAssemblerArgumentResolver;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {StagingJobController.class})
@EnableWebMvc
@AutoConfigureMockMvc
public class StagingJobControllerTest {

    @MockBean
    private StagingJobService stagingJobService;

    @Autowired
    private MockMvc webApp;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser("johndoe")
    public void createStagingJob() throws Exception {
        //given:
        StagingJob stagingJob = new StagingJob(UUID.randomUUID(), "file_1.json");

        //when:
        String jsonContent = objectMapper.writeValueAsString(stagingJob);
        MvcResult result = webApp
                .perform(post("/stagingJobs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andReturn();

        //then:
        assertThat(result.getResponse().getStatus()).isEqualTo(OK.value());
        verify(stagingJobService).register(any(StagingJob.class));
    }

}
