package org.humancellatlas.ingest.exporter;

import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.bundle.BundleManifestService;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.web.LinkGenerator;
import org.humancellatlas.ingest.export.entity.ExportEntityService;
import org.humancellatlas.ingest.export.job.ExportJobService;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.process.ProcessService;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.project.ProjectService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
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

    @Test
    public void testExportManifests() {
        //given:
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        Set<String> assayIds = mockProcessIds(2);
        when(processService.getProcesses(any())).thenAnswer(
                (Answer<Stream<Process>>) invocation -> {
                    List<String> ids = invocation.getArgument(0);
                    return ids.stream().map(id -> {
                            Process process = new Process(id);
                            process.setSubmissionEnvelope(submissionEnvelope);
                            return process;
                    });
                }
        );

        doReturn(assayIds).when(processService).findAssays(any(SubmissionEnvelope.class));

        //and:
        Set<ExperimentProcess> receivedData = mockSendingThroughMessageRouter();

        //when:
        exporter.exportManifests(submissionEnvelope);

        //then:
        int expectedCount = 2;
        assertThat(receivedData).hasSize(expectedCount);
        assertUniqueIndexes(receivedData);
        assertCorrectTotalCount(receivedData, expectedCount);
        assertCorrectSubmissionEnvelope(receivedData, submissionEnvelope);
        assertAllProcessesExported(assayIds, receivedData);

        //and:
        verify(messageRouter, times(assayIds.size()))
                .sendManifestForExport(any(ExperimentProcess.class));
    }

    private Set<String> mockProcessIds(int max) {
        return IntStream.range(0, max)
                        .mapToObj(count -> UUID.randomUUID().toString())
                        .collect(Collectors.toSet());
    }

    private Set<ExperimentProcess> mockSendingThroughMessageRouter() {
        final Set<ExperimentProcess> experimentProcess = new HashSet<>();
        Answer<Void> addToSet = invocation ->  {
            experimentProcess.add(invocation.getArgument(0));
            return null;
        };
        doAnswer(addToSet).when(messageRouter).sendManifestForExport(any(ExperimentProcess.class));
        return experimentProcess;
    }

    private void assertUniqueIndexes(Set<ExperimentProcess> receivedData) {
        List<Integer> indexes = receivedData.stream()
                .map(ExperimentProcess::getIndex)
                .collect(toList());
        assertThat(indexes).containsOnlyOnce(0, 1);
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