package org.humancellatlas.ingest.archiving.web;

import org.humancellatlas.ingest.archiving.entity.ArchiveJob;
import org.humancellatlas.ingest.archiving.entity.ArchiveJob.ArchiveJobStatus;
import org.humancellatlas.ingest.archiving.entity.ArchiveJobRepository;
import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.*;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @MockBean
    private ArchiveJobRepository archiveJobRepository;

    private UUID uuid;

    private static final String ARCHIVE_JOB_ID = "1";
    private static final String SUBMISSION_UUID = "1234";


    @Test
    public void when_request_archive_job_creation_returns_successful_response() throws Exception {
        final ArchiveJob anArchiveJobById = createAnArchiveJob(ARCHIVE_JOB_ID, SUBMISSION_UUID, ArchiveJobStatus.PENDING);

        given(this.archiveJobRepository.save(any()))
                .willReturn(anArchiveJobById);

        this.mockMvc.perform(post("/archiveJobs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("{\"submissionUuid\": \"%s\"}", this.uuid)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.submissionUuid", is(SUBMISSION_UUID)))
                .andExpect(jsonPath("$.overallStatus", is(ArchiveJobStatus.PENDING.toString())))
                .andExpect(jsonPath("$.createdDate").isNotEmpty())
                .andReturn();
    }

    @Test
    public void when_requesting_non_existing_archiving_job_returns_not_found_response() throws Exception {
        given(this.archiveJobRepository.findById(ARCHIVE_JOB_ID))
                .willReturn(Optional.empty());

        this.mockMvc.perform(get("/archiveJobs/{id}", ARCHIVE_JOB_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void when_existing_archiving_job_in_pending_status_returns_valid_response() throws Exception {
        final ArchiveJob anArchiveJob = createAnArchiveJob(ARCHIVE_JOB_ID, SUBMISSION_UUID, ArchiveJobStatus.PENDING);

        given(this.archiveJobRepository.findById(ARCHIVE_JOB_ID))
                .willReturn(Optional.of(anArchiveJob));

        this.mockMvc.perform(get("/archiveJobs/{id}", ARCHIVE_JOB_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.submissionUuid", is(SUBMISSION_UUID)))
                .andExpect(jsonPath("$.overallStatus", is(ArchiveJobStatus.PENDING.toString())))
                .andExpect(jsonPath("$.createdDate").isNotEmpty())
                .andExpect(jsonPath("$.resultsFromArchives").doesNotExist())
                .andReturn();
    }

    @Test
    public void when_existing_archiving_job_in_completed_status_returns_valid_response() throws Exception {
        final ArchiveJob anArchiveJob = createAnArchiveJob(ARCHIVE_JOB_ID, SUBMISSION_UUID, ArchiveJobStatus.COMPLETED);
        setArchiveResult(anArchiveJob);

        given(this.archiveJobRepository.findById(ARCHIVE_JOB_ID))
                .willReturn(Optional.of(anArchiveJob));

        this.mockMvc.perform(get("/archiveJobs/{id}", ARCHIVE_JOB_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.submissionUuid", is(SUBMISSION_UUID)))
                .andExpect(jsonPath("$.overallStatus", is(ArchiveJobStatus.COMPLETED.toString())))
                .andExpect(jsonPath("$.createdDate").isNotEmpty())
                .andExpect(jsonPath("$.resultsFromArchives").isNotEmpty())
                .andReturn();
    }

    private ArchiveJob createAnArchiveJob(String id, String submissionUuid, ArchiveJob.ArchiveJobStatus status) {
        ArchiveJob archiveJob = new ArchiveJob();
        archiveJob.setId(ARCHIVE_JOB_ID);
        archiveJob.setCreatedDate(Instant.now());
        archiveJob.setOverallStatus(status);
        archiveJob.setSubmissionUuid(submissionUuid);

        return archiveJob;
    }

    private void setArchiveResult(ArchiveJob anArchiveJob) {
        Map<String, Object> resultByArchive = new HashMap<>();
        Map<String, List<Map<String, String>>> experimentsResult = new HashMap<>();
        experimentsResult.put("experiments", List.of(Map.of("accession", "1234"), Map.of("uuid", "1-2-3-4")));
        resultByArchive.put("hca_assays", experimentsResult);
        anArchiveJob.setResultsFromArchives(resultByArchive);
    }
}
