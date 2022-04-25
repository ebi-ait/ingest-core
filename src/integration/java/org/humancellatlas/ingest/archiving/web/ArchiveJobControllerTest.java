package org.humancellatlas.ingest.archiving.web;

import org.humancellatlas.ingest.archiving.entity.ArchiveJob;
import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureDataMongo()
@AutoConfigureMockMvc
public class ArchiveJobControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private MigrationConfiguration migrationConfiguration;

    private static final ArchiveJob.ArchiveJobStatus PENDING_STATUS = ArchiveJob.ArchiveJobStatus.PENDING;

    private UUID uuid;

    @BeforeEach
    public void setup() {
        this.uuid = UUID.randomUUID();
    }

    @Test
    public void when_request_archive_job_creation_returns_successful_response() throws Exception {
        this.mockMvc.perform(post("/archiveJobs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("{\"submissionUuid\": \"%s\"}", this.uuid)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.submissionUuid", is(this.uuid.toString())))
                .andExpect(jsonPath("$.overallStatus", is(PENDING_STATUS.toString())))
                .andExpect(jsonPath("$.createdDate").isNotEmpty())
                .andReturn();
    }
}
