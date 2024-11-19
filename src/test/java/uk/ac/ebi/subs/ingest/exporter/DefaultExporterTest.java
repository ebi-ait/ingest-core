package uk.ac.ebi.subs.ingest.exporter;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static uk.ac.ebi.subs.ingest.export.destination.ExportDestinationName.DCP;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.ac.ebi.subs.ingest.bundle.BundleManifestRepository;
import uk.ac.ebi.subs.ingest.bundle.BundleManifestService;
import uk.ac.ebi.subs.ingest.core.Uuid;
import uk.ac.ebi.subs.ingest.core.service.MetadataCrudService;
import uk.ac.ebi.subs.ingest.export.destination.ExportDestination;
import uk.ac.ebi.subs.ingest.export.entity.ExportEntityService;
import uk.ac.ebi.subs.ingest.export.job.ExportJob;
import uk.ac.ebi.subs.ingest.export.job.ExportJobRepository;
import uk.ac.ebi.subs.ingest.export.job.ExportJobService;
import uk.ac.ebi.subs.ingest.export.job.web.ExportJobRequest;
import uk.ac.ebi.subs.ingest.messaging.MessageRouter;
import uk.ac.ebi.subs.ingest.process.Process;
import uk.ac.ebi.subs.ingest.process.ProcessRepository;
import uk.ac.ebi.subs.ingest.process.ProcessService;
import uk.ac.ebi.subs.ingest.project.Project;
import uk.ac.ebi.subs.ingest.project.ProjectRepository;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DefaultExporterTest {

  @Autowired private Exporter exporter;

  @MockBean private ProcessService processService;

  @MockBean private MessageRouter messageRouter;

  @MockBean private MetadataCrudService metadataCrudService;

  @MockBean private ExportJobService exportJobService;

  @MockBean private ExportJobRepository exportJobRepository;

  @MockBean private ProcessRepository processRepository;

  @MockBean private ProjectRepository projectRepository;

  @MockBean private ExportEntityService exportEntityService;

  @MockBean private BundleManifestService bundleManifestService;

  @MockBean private BundleManifestRepository bundleManifestRepository;

  SubmissionEnvelope submissionEnvelope;

  Project project;

  Set<String> assayIds;

  @BeforeEach
  void setUp() {
    // given:
    submissionEnvelope = new SubmissionEnvelope();
    assayIds = mockProcessIds(2);
    project = new Project(null);
    project.setUuid(Uuid.newUuid());
    project.getSubmissionEnvelopes().add(submissionEnvelope);

    mockProcessSvcGetProcesses(submissionEnvelope, assayIds);

    // and:
    Set<ExperimentProcess> receivedData = mockSendingManifestThroughMessageRouter();
  }

  private String projectUuid() {
    return project.getUuid().getUuid().toString();
  }

  @Test
  public void testExportManifests() {
    // when:
    Set<ExperimentProcess> receivedData = mockSendingManifestThroughMessageRouter();

    exporter.exportManifests(submissionEnvelope);

    // then:
    assertAllProcessIdsProcessed(submissionEnvelope, assayIds, receivedData);

    // and:
    verify(messageRouter, times(assayIds.size()))
        .sendManifestForExport(any(ExperimentProcess.class));
  }

  @Test
  public void testExportDataSetsContextsAndCallsMessageRouter() {
    // given
    mockCreateExportJob(projectUuid());

    // when
    exporter.exportData(submissionEnvelope);

    // then
    var insertCaptor = ArgumentCaptor.forClass(ExportJob.class);
    var sendCaptor = ArgumentCaptor.forClass(ExportJob.class);
    verify(exportJobRepository).insert(insertCaptor.capture());
    verify(messageRouter).sendSubmissionForDataExport(sendCaptor.capture(), any());

    assertThat(insertCaptor.getValue().getDestination().getContext().get("projectUuid"))
        .isEqualTo(projectUuid());
    assertThat(insertCaptor.getValue().getContext().get("dataFileTransfer")).isEqualTo(false);
    assertThat(sendCaptor.getValue().getDestination().getContext().get("projectUuid"))
        .isEqualTo(projectUuid());
    assertThat(sendCaptor.getValue().getContext().get("dataFileTransfer")).isEqualTo(false);
  }

  @Test
  public void testExportMetadataFromExportJob() {
    // given:
    mockProcessSave();
    ExportJob newExportJob = mockCreateExportJob(projectUuid());
    Set<ExperimentProcess> receivedData = mockSendingProcessThroughMessageRouter();

    // when:
    exporter.exportMetadata(newExportJob);

    // then:
    assertAllProcessIdsProcessed(submissionEnvelope, assayIds, receivedData);
    assertDcpVersionUpdated(receivedData, newExportJob.getCreatedDate());
    verify(processRepository, times(assayIds.size())).save(any(Process.class));
    verify(messageRouter, times(assayIds.size()))
        .sendExperimentForExport(any(ExperimentProcess.class), any(ExportJob.class), any());
  }

  @Test
  public void testGenerateSpreadsheetFromSubmission() {
    // given
    mockCreateExportJob(projectUuid());

    // when:
    exporter.generateSpreadsheet(submissionEnvelope);

    // then
    var insertCaptor = ArgumentCaptor.forClass(ExportJob.class);
    var saveCaptor = ArgumentCaptor.forClass(ExportJob.class);
    var sendCaptor = ArgumentCaptor.forClass(ExportJob.class);
    verify(exportJobRepository).insert(insertCaptor.capture());
    verify(exportJobRepository).save(saveCaptor.capture());
    verify(messageRouter).sendGenerateSpreadsheet(sendCaptor.capture(), any());

    assertThat(insertCaptor.getValue().getDestination().getContext().get("projectUuid"))
        .isEqualTo(projectUuid());
    assertThat(saveCaptor.getValue().getContext().get("spreadsheetGeneration")).isEqualTo(false);
    assertThat(sendCaptor.getValue().getDestination().getContext().get("projectUuid"))
        .isEqualTo(projectUuid());
    assertThat(sendCaptor.getValue().getContext().get("spreadsheetGeneration")).isEqualTo(false);
  }

  @Test
  public void testGenerateSpreadsheetSetsContextsAndCallsMessageRouter() {
    // given
    ExportJob newExportJob = mockCreateExportJob(projectUuid());

    // when:
    exporter.generateSpreadsheet(newExportJob);

    // then
    var saveCaptor = ArgumentCaptor.forClass(ExportJob.class);
    var sendCaptor = ArgumentCaptor.forClass(ExportJob.class);
    verify(exportJobRepository).save(saveCaptor.capture());
    verify(messageRouter).sendGenerateSpreadsheet(sendCaptor.capture(), any());

    assertThat(saveCaptor.getValue().getContext().get("spreadsheetGeneration")).isEqualTo(false);
    assertThat(sendCaptor.getValue().getDestination().getContext().get("projectUuid"))
        .isEqualTo(projectUuid());
    assertThat(sendCaptor.getValue().getContext().get("spreadsheetGeneration")).isEqualTo(false);
  }

  private ExportJob mockCreateExportJob(String projectUuidUuid) {
    var destinationContext = new JSONObject();
    destinationContext.put("projectUuid", projectUuidUuid);

    var exportJobContext = new JSONObject();
    exportJobContext.put("totalAssayCount", assayIds.size());
    exportJobContext.put("dataFileTransfer", false);
    ExportJob newExportJob =
        ExportJob.builder()
            .submission(submissionEnvelope)
            .destination(new ExportDestination(DCP, "v2", destinationContext))
            .context(exportJobContext)
            .build();
    doReturn(newExportJob)
        .when(exportJobService)
        .createExportJob(any(SubmissionEnvelope.class), any(ExportJobRequest.class));
    doReturn(newExportJob).when(exportJobRepository).insert(any(ExportJob.class));
    doReturn(Stream.of(project))
        .when(projectRepository)
        .findBySubmissionEnvelopesContains(any(SubmissionEnvelope.class));
    return newExportJob;
  }

  private void assertAllProcessIdsProcessed(
      SubmissionEnvelope submissionEnvelope,
      Set<String> assayIds,
      Set<ExperimentProcess> receivedData) {
    int expectedCount = 2;
    assertThat(receivedData).hasSize(expectedCount);
    assertUniqueIndexes(receivedData);
    assertCorrectTotalCount(receivedData, expectedCount);
    assertCorrectSubmissionEnvelope(receivedData, submissionEnvelope);
    assertAllProcessesExported(assayIds, receivedData);
  }

  private void mockProcessSave() {
    when(processRepository.save(any(Process.class)))
        .thenAnswer(
            (Answer<Process>)
                invocation -> {
                  Process process = invocation.getArgument(0);
                  return process;
                });
  }

  private void mockProcessSvcGetProcesses(
      SubmissionEnvelope submissionEnvelope, Set<String> assayIds) {
    when(processService.getProcesses(any()))
        .thenAnswer(
            (Answer<Stream<Process>>)
                invocation -> {
                  List<String> ids = invocation.getArgument(0);
                  return ids.stream()
                      .map(
                          id -> {
                            Process process = spy(new Process(null));
                            doReturn(id).when(process).getId();
                            process.setSubmissionEnvelope(submissionEnvelope);
                            return process;
                          });
                });

    doReturn(assayIds).when(processService).findAssays(any(SubmissionEnvelope.class));
  }

  private Set<String> mockProcessIds(int max) {
    return IntStream.range(0, max)
        .mapToObj(count -> UUID.randomUUID().toString())
        .collect(Collectors.toSet());
  }

  private Set<ExperimentProcess> mockSendingManifestThroughMessageRouter() {
    final Set<ExperimentProcess> experimentProcess = new HashSet<>();
    Answer<Void> addToSet =
        invocation -> {
          experimentProcess.add(invocation.getArgument(0));
          return null;
        };
    doAnswer(addToSet).when(messageRouter).sendManifestForExport(any(ExperimentProcess.class));
    return experimentProcess;
  }

  private Set<ExperimentProcess> mockSendingProcessThroughMessageRouter() {
    final Set<ExperimentProcess> experimentProcess = new HashSet<>();
    Answer<Void> addToSet =
        invocation -> {
          experimentProcess.add(invocation.getArgument(0));
          return null;
        };
    doAnswer(addToSet)
        .when(messageRouter)
        .sendExperimentForExport(any(ExperimentProcess.class), any(ExportJob.class), any());
    return experimentProcess;
  }

  private void assertUniqueIndexes(Set<ExperimentProcess> receivedData) {
    List<Integer> indexes =
        receivedData.stream().map(ExperimentProcess::getIndex).collect(toList());
    assertThat(indexes).containsOnlyOnce(0, 1);
  }

  private void assertDcpVersionUpdated(Set<ExperimentProcess> receivedData, Instant dcpVersion) {
    receivedData.stream()
        .map(ExperimentProcess::getProcess)
        .forEach(process -> assertThat(process.getDcpVersion()).isEqualTo(dcpVersion));
  }

  private void assertCorrectTotalCount(Set<ExperimentProcess> receivedData, int expectedCount) {
    receivedData.stream()
        .forEach(
            exporterData -> {
              assertThat(exporterData.getTotalCount()).isEqualTo(expectedCount);
            });
  }

  private void assertCorrectSubmissionEnvelope(
      Set<ExperimentProcess> receivedData, SubmissionEnvelope submissionEnvelope) {
    receivedData.forEach(
        exporterData ->
            assertThat(exporterData.getSubmissionEnvelope()).isEqualTo(submissionEnvelope));
  }

  private void assertAllProcessesExported(
      Set<String> assayIds, Set<ExperimentProcess> exporterData) {

    List<Process> sentProcesses =
        exporterData.stream().map(ExperimentProcess::getProcess).collect(toList());

    assertThat(sentProcesses.stream().map(Process::getId)).containsAll(assayIds);
  }

  @Configuration
  static class TestConfiguration {

    @Bean
    Exporter defaultExporter() {
      return new DefaultExporter();
    }
  }
}
