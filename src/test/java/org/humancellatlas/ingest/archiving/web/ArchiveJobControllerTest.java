package org.humancellatlas.ingest.archiving.web;

import org.humancellatlas.ingest.archiving.entity.ArchiveJobRepository;
import org.humancellatlas.ingest.archiving.submission.web.ArchiveJobController;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = { ArchiveJobController.class })
@AutoConfigureMockMvc
public class ArchiveJobControllerTest {


    @Autowired
    private ArchiveJobController archiveJobController;

    @MockBean
    private ArchiveJobRepository archiveJobRepository;

    private static final String CREATED_STATUS = "Created";

    @Autowired
    MockMvc mockMvc;

    private UUID uuid;

    @Before
    public void setup() {
        this.uuid = UUID.randomUUID();
        this.archiveJobController = new ArchiveJobController(archiveJobRepository);
    }

    @Test
    public void when_request_archive_job_creation_returns_successful_response() throws Exception {
        this.mockMvc.perform(post("/archiveJobs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"submissionUuid\": this.uuid"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.Uuid", is(this.uuid)))
                .andExpect(jsonPath("$.status", is(CREATED_STATUS)));
    }
}
