package org.humancellatlas.ingest.export.job;

import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.export.ExportState;
import org.humancellatlas.ingest.export.destination.ExportDestination;
import org.humancellatlas.ingest.exporter.Exporter;
import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.humancellatlas.ingest.export.destination.ExportDestinationName.DCP;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureDataMongo()
@AutoConfigureMockMvc()
public class ExportJobControllerTest {
    @Autowired
    private MockMvc webApp;

    @Autowired
    private SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @Autowired
    private ExportJobRepository exportJobRepository;

    @Autowired
    private ExportJobService exportJobService;

    @MockBean
    private Exporter exporter;

    // Adding MigrationConfiguration as a MockBean is needed as otherwise MigrationConfiguration won't be initialised.
    @MockBean
    private MigrationConfiguration migrationConfiguration;

    SubmissionEnvelope submissionEnvelope;

    ExportJob exportJob;

    @BeforeEach
    void setUp() {
        submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.setUuid(Uuid.newUuid());
        submissionEnvelope.enactStateTransition(SubmissionState.EXPORTING);
        submissionEnvelope = submissionEnvelopeRepository.save(submissionEnvelope);

        var destinationContext = new JSONObject();
        destinationContext.put("projectUuid", "project-uuid-uuid");

        var exportJobContext = new JSONObject();
        exportJobContext.put("dataFileTransfer", false);
        exportJob = ExportJob.builder()
            .id("export-job-id")
            .submission(submissionEnvelope)
            .destination(new ExportDestination(DCP, "v2", destinationContext))
            .status(ExportState.EXPORTING)
            .createdDate(Instant.now())
            .context(exportJobContext)
            .errors(new ArrayList<>())
            .build();
        exportJob = exportJobRepository.save(exportJob);
    }

    @AfterEach
    void tearDown() {
        exportJobRepository.deleteAll();
        submissionEnvelopeRepository.deleteAll();
    }

    @Test
    void testCallbackEndpoint() throws Exception {
        // given
        String patch_value = "COMPLETE";

        webApp.perform(
                // when
                patch("/exportJobs/{id}/context", exportJob.getId())
                    .contentType(APPLICATION_JSON_VALUE)
                    .content("{\"dataFileTransfer\": \"" + patch_value + "\"}")
            )   // then
            .andExpect(status().isAccepted());
        var argumentCaptor = ArgumentCaptor.forClass(ExportJob.class);
        verify(exporter).exportMetadata(argumentCaptor.capture());

        var capturedArgument = argumentCaptor.getValue();
        assertThat(capturedArgument.getId()).isEqualTo(exportJob.getId());
        assertThat(capturedArgument.getDestination().getContext().get("projectUuid")).isEqualTo("project-uuid-uuid");
        assertThat(capturedArgument.getContext().get("dataFileTransfer")).isEqualTo(patch_value);
    }
}
