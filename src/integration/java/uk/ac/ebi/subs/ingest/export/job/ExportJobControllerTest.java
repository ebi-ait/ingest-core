package uk.ac.ebi.subs.ingest.export.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.ebi.subs.ingest.export.destination.ExportDestinationName.DCP;

import org.json.simple.JSONObject;
import org.junit.Ignore;
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

import uk.ac.ebi.subs.ingest.config.MigrationConfiguration;
import uk.ac.ebi.subs.ingest.core.Uuid;
import uk.ac.ebi.subs.ingest.export.destination.ExportDestination;
import uk.ac.ebi.subs.ingest.exporter.Exporter;
import uk.ac.ebi.subs.ingest.state.SubmissionState;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelopeRepository;

@SpringBootTest
@AutoConfigureDataMongo()
@AutoConfigureMockMvc()
public class ExportJobControllerTest {
  public static final String STARTED = "STARTED";
  public static final String COMPLETE = "COMPLETE";
  @Autowired private MockMvc webApp;

  @Autowired private SubmissionEnvelopeRepository submissionEnvelopeRepository;

  @Autowired private ExportJobRepository exportJobRepository;

  @Autowired private ExportJobService exportJobService;

  @MockBean private Exporter exporter;

  // Adding MigrationConfiguration as a MockBean is needed as otherwise MigrationConfiguration won't
  // be initialised.
  @MockBean private MigrationConfiguration migrationConfiguration;

  SubmissionEnvelope submissionEnvelope;

  ExportJob exportJob;

  Uuid projectUuid;

  @BeforeEach
  void setUp() {
    submissionEnvelope = new SubmissionEnvelope();
    submissionEnvelope.setUuid(Uuid.newUuid());
    submissionEnvelope.enactStateTransition(SubmissionState.EXPORTING);
    submissionEnvelope = submissionEnvelopeRepository.save(submissionEnvelope);

    projectUuid = Uuid.newUuid();
    var destinationContext = new JSONObject();
    destinationContext.put("projectUuid", projectUuid);

    var exportJobContext = new JSONObject();
    exportJobContext.put("dataFileTransfer", false);
    exportJob =
        ExportJob.builder()
            .id("export-job-id")
            .submission(submissionEnvelope)
            .destination(new ExportDestination(DCP, "v2", destinationContext))
            .context(exportJobContext)
            .build();
    exportJob = exportJobRepository.save(exportJob);
  }

  @AfterEach
  void tearDown() {
    exportJobRepository.deleteAll();
    submissionEnvelopeRepository.deleteAll();
    reset(exporter);
  }

  @Test
  void testDataTransferCallbackOnlyProgressesOnComplete() throws Exception {
    webApp
        .perform(
            // when
            patch("/exportJobs/{id}/context", exportJob.getId())
                .contentType(APPLICATION_JSON_VALUE)
                .content("{\"dataFileTransfer\": \"" + STARTED + "\"}")) // then
        .andExpect(status().isAccepted());
    verify(exporter, never()).generateSpreadsheet(any(ExportJob.class));

    var savedJob = exportJobRepository.findById(exportJob.getId()).orElseThrow();
    assertThat(savedJob.getContext().get("dataFileTransfer")).isEqualTo(STARTED);
  }

  @Ignore
  void testDataTransferCallbackEndpoint() throws Exception {
    webApp
        .perform(
            // when
            patch("/exportJobs/{id}/context", exportJob.getId())
                .contentType(APPLICATION_JSON_VALUE)
                .content("{\"dataFileTransfer\": \"" + COMPLETE + "\"}")) // then
        .andExpect(status().isAccepted());
    var argumentCaptor = ArgumentCaptor.forClass(ExportJob.class);
    verify(exporter).generateSpreadsheet(argumentCaptor.capture());

    var capturedArgument = argumentCaptor.getValue();
    assertThat(capturedArgument.getId()).isEqualTo(exportJob.getId());
    assertThat(capturedArgument.getDestination().getContext().get("projectUuid"))
        .isEqualTo(projectUuid);
    assertThat(capturedArgument.getContext().get("dataFileTransfer")).isEqualTo(COMPLETE);
  }

  @Test
  void testSpreadsheetGenerationCallbackOnlyProgressesOnComplete() throws Exception {
    // given
    exportJob.getContext().put("dataFileTransfer", COMPLETE);
    exportJob = exportJobRepository.save(exportJob);

    webApp
        .perform(
            // when
            patch("/exportJobs/{id}/context", exportJob.getId())
                .contentType(APPLICATION_JSON_VALUE)
                .content("{\"spreadsheetGeneration\": \"" + STARTED + "\"}")) // then
        .andExpect(status().isAccepted());
    verify(exporter, never()).exportMetadata(any(ExportJob.class));

    var savedJob = exportJobRepository.findById(exportJob.getId()).orElseThrow();
    assertThat(savedJob.getContext().get("dataFileTransfer")).isEqualTo(COMPLETE);
    assertThat(savedJob.getContext().get("spreadsheetGeneration")).isEqualTo(STARTED);
  }

  @Ignore
  void testSpreadsheetGenerationCallbackEndpoint() throws Exception {
    // given
    exportJob.getContext().put("dataFileTransfer", COMPLETE);
    exportJob.getContext().put("spreadsheetGeneration", STARTED);
    exportJob = exportJobRepository.save(exportJob);

    webApp
        .perform(
            // when
            patch("/exportJobs/{id}/context", exportJob.getId())
                .contentType(APPLICATION_JSON_VALUE)
                .content("{\"spreadsheetGeneration\": \"" + COMPLETE + "\"}")) // then
        .andExpect(status().isAccepted());
    var argumentCaptor = ArgumentCaptor.forClass(ExportJob.class);
    verify(exporter).exportMetadata(argumentCaptor.capture());

    var capturedArgument = argumentCaptor.getValue();
    assertThat(capturedArgument.getId()).isEqualTo(exportJob.getId());
    assertThat(capturedArgument.getDestination().getContext().get("projectUuid"))
        .isEqualTo(projectUuid);
    assertThat(capturedArgument.getContext().get("dataFileTransfer")).isEqualTo(COMPLETE);
    assertThat(capturedArgument.getContext().get("spreadsheetGeneration")).isEqualTo(COMPLETE);
  }
}
