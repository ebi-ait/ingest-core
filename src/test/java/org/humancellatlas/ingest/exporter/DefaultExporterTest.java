package org.humancellatlas.ingest.exporter;

import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.bundle.BundleManifestService;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.web.LinkGenerator;
import org.humancellatlas.ingest.export.ExportState;
import org.humancellatlas.ingest.export.destination.ExportDestination;
import org.humancellatlas.ingest.export.entity.ExportEntityService;
import org.humancellatlas.ingest.export.job.ExportJob;
import org.humancellatlas.ingest.export.job.ExportJobService;
import org.humancellatlas.ingest.export.job.web.ExportJobRequest;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.process.ProcessService;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.humancellatlas.ingest.export.destination.ExportDestinationName.DCP;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DefaultExporterTest {

    @Autowired
    private Exporter exporter;

    @MockBean
    private ProcessService processService;

    @MockBean
    private MessageRouter messageRouter;

    @MockBean
    private MetadataCrudService metadataCrudService;

    @MockBean
    private ExportJobService exportJobService;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private ProcessRepository processRepository;

    @MockBean
    private ExportEntityService exportEntityService;

    @MockBean
    private BundleManifestService bundleManifestService;

    @MockBean
    private BundleManifestRepository bundleManifestRepository;

    @MockBean
    private LinkGenerator linkGenerator;

    SubmissionEnvelope submissionEnvelope;

    Set<String> assayIds;

    @BeforeEach
    void setUp() {
        //given:
        submissionEnvelope = new SubmissionEnvelope();
        assayIds = mockProcessIds(2);

        mockProcessSvcGetProcesses(submissionEnvelope, assayIds);

        //and:
        Set<ExperimentProcess> receivedData = mockSendingManifestThroughMessageRouter();
    }

    @Test
    public void testExportManifests() {
        //when:
        Set<ExperimentProcess> receivedData = mockSendingManifestThroughMessageRouter();

        exporter.exportManifests(submissionEnvelope);

        //then:
        assertAllProcessIdsProcessed(submissionEnvelope, assayIds, receivedData);

        //and:
        verify(messageRouter, times(assayIds.size()))
                .sendManifestForExport(any(ExperimentProcess.class));
    }

    @Test
    public void testExportProcesses() {
        //given:
        mockProcessSave();
        ExportJob newExportJob = mockCreateExportJob();
        Set<ExperimentProcess> receivedData = mockSendingProcessThroughMessageRouter();

        //when:
        exporter.exportProcesses(submissionEnvelope);

        //then:
        assertAllProcessIdsProcessed(submissionEnvelope, assayIds, receivedData);
        assertDcpVersionUpdated(receivedData, newExportJob.getCreatedDate());
        verify(processRepository, times(assayIds.size())).save(any(Process.class));
        verify(messageRouter, times(assayIds.size()))
                .sendExperimentForExport(any(ExperimentProcess.class), any(ExportJob.class), any());
    }

    private ExportJob mockCreateExportJob() {
        JSONObject context = new JSONObject();
        context.put("totalAssayCount", assayIds.size());
        ExportJob newExportJob = ExportJob.builder()
                .status(ExportState.EXPORTING)
                .errors(new ArrayList<>())
                .submission(submissionEnvelope)
                .destination(new ExportDestination(DCP, "v2", null))
                .context(context)
                .build();
        doReturn(newExportJob).when(exportJobService).createExportJob(any(SubmissionEnvelope.class), any(ExportJobRequest.class));
        return newExportJob;
    }

    private void assertAllProcessIdsProcessed(SubmissionEnvelope submissionEnvelope, Set<String> assayIds, Set<ExperimentProcess> receivedData) {
        int expectedCount = 2;
        assertThat(receivedData).hasSize(expectedCount);
        assertUniqueIndexes(receivedData);
        assertCorrectTotalCount(receivedData, expectedCount);
        assertCorrectSubmissionEnvelope(receivedData, submissionEnvelope);
        assertAllProcessesExported(assayIds, receivedData);
    }

    private void mockProcessSave() {
        when(processRepository.save(any(Process.class))).thenAnswer(
                (Answer<Process>) invocation -> {
                    Process process = invocation.getArgument(0);
                    return process;
                }
        );
    }

    private void mockProcessSvcGetProcesses(SubmissionEnvelope submissionEnvelope, Set<String> assayIds) {
        when(processService.getProcesses(any())).thenAnswer(
                (Answer<Stream<Process>>) invocation -> {
                    List<String> ids = invocation.getArgument(0);
                    return ids.stream().map(id -> {
                        Process process = spy(new Process(null));
                        doReturn(id).when(process).getId();
                        process.setSubmissionEnvelope(submissionEnvelope);
                        return process;
                    });
                }
        );

        doReturn(assayIds).when(processService).findAssays(any(SubmissionEnvelope.class));
    }

    private Set<String> mockProcessIds(int max) {
        return IntStream.range(0, max)
                .mapToObj(count -> UUID.randomUUID().toString())
                .collect(Collectors.toSet());
    }

    private Set<ExperimentProcess> mockSendingManifestThroughMessageRouter() {
        final Set<ExperimentProcess> experimentProcess = new HashSet<>();
        Answer<Void> addToSet = invocation -> {
            experimentProcess.add(invocation.getArgument(0));
            return null;
        };
        doAnswer(addToSet).when(messageRouter).sendManifestForExport(any(ExperimentProcess.class));
        return experimentProcess;
    }

    private Set<ExperimentProcess> mockSendingProcessThroughMessageRouter() {
        final Set<ExperimentProcess> experimentProcess = new HashSet<>();
        Answer<Void> addToSet = invocation -> {
            experimentProcess.add(invocation.getArgument(0));
            return null;
        };
        doAnswer(addToSet).when(messageRouter).sendExperimentForExport(any(ExperimentProcess.class), any(ExportJob.class), any());
        return experimentProcess;
    }

    private void assertUniqueIndexes(Set<ExperimentProcess> receivedData) {
        List<Integer> indexes = receivedData.stream()
                .map(ExperimentProcess::getIndex)
                .collect(toList());
        assertThat(indexes).containsOnlyOnce(0, 1);
    }

    private void assertDcpVersionUpdated(Set<ExperimentProcess> receivedData, Instant dcpVersion) {
        receivedData.stream()
                .map(ExperimentProcess::getProcess)
                .forEach(process -> assertThat(process.getDcpVersion()).isEqualTo(dcpVersion));
    }

    private void assertCorrectTotalCount(Set<ExperimentProcess> receivedData, int expectedCount) {
        receivedData.stream().forEach(exporterData -> {
            assertThat(exporterData.getTotalCount()).isEqualTo(expectedCount);
        });
    }

    private void assertCorrectSubmissionEnvelope(Set<ExperimentProcess> receivedData,
                                                 SubmissionEnvelope submissionEnvelope) {
        receivedData.forEach(exporterData -> assertThat(exporterData.getSubmissionEnvelope()).isEqualTo(submissionEnvelope));
    }

    private void assertAllProcessesExported(Set<String> assayIds,
                                            Set<ExperimentProcess> exporterData) {

        List<Process> sentProcesses = exporterData.stream()
                .map(ExperimentProcess::getProcess)
                .collect(toList());

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