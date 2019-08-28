package org.humancellatlas.ingest.export;

import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.bundle.BundleManifestService;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.web.LinkGenerator;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

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
    private BundleManifestService bundleManifestService;

    @MockBean
    private BundleManifestRepository bundleManifestRepository;

    @MockBean
    private LinkGenerator linkGenerator;

    @Test
    public void testExportBundles() {
        //given:
        Set<String> assayIds = mockProcessIds(2);
        when(processService.getProcesses(any())).thenAnswer(
                (Answer<Stream<Process>>) invocation -> {
                    List<String> ids = invocation.getArgument(0);
                    return ids.stream().map(Process::new);
                }
        );

        doReturn(assayIds).when(processService).findAssays(any(SubmissionEnvelope.class));

        //and:
        Set<String> analysisIds = mockProcessIds(3);
        doReturn(analysisIds).when(processService).findAnalyses(any(SubmissionEnvelope.class));

        //and:
        Set<ExportData> receivedData = mockSendingThroughMessageRouter();

        //when:
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        exporter.exportBundles(submissionEnvelope);

        //then:
        int expectedCount = 5;
        assertThat(receivedData).hasSize(expectedCount);
        assertUniqueIndexes(receivedData);
        assertCorrectTotalCount(receivedData, expectedCount);
        assertCorrectSubmissionEnvelope(receivedData, submissionEnvelope);
        assertAllProcessesExported(assayIds, analysisIds, receivedData);

        //and:
        verify(messageRouter, times(assayIds.size()))
                .sendAssayForExport(any(ExportData.class));
        verify(messageRouter, times(analysisIds.size()))
                .sendAnalysisForExport(any(ExportData.class));
    }

    private Set<String> mockProcessIds(int max) {
        return IntStream.range(0, max)
                        .mapToObj(count -> UUID.randomUUID().toString())
                        .collect(Collectors.toSet());
    }

    private Set<ExportData> mockSendingThroughMessageRouter() {
        final Set<ExportData> exportData = new HashSet<>();
        Answer<Void> addToSet = invocation ->  {
            exportData.add(invocation.getArgument(0));
            return null;
        };
        doAnswer(addToSet).when(messageRouter).sendAssayForExport(any(ExportData.class));
        doAnswer(addToSet).when(messageRouter).sendAnalysisForExport(any(ExportData.class));
        return exportData;
    }

    private void assertUniqueIndexes(Set<ExportData> receivedData) {
        List<Integer> indexes = receivedData.stream()
                .map(ExportData::getIndex)
                .collect(toList());
        assertThat(indexes).containsOnlyOnce(0, 1, 2, 3, 4);
    }

    private void assertCorrectTotalCount(Set<ExportData> receivedData, int expectedCount) {
        receivedData.stream().forEach(exportData -> {
            assertThat(exportData.getTotalCount()).isEqualTo(expectedCount);
        });
    }

    private void assertCorrectSubmissionEnvelope(Set<ExportData> receivedData,
            SubmissionEnvelope submissionEnvelope) {
        receivedData.forEach(exportData -> assertThat(exportData.getSubmissionEnvelope()).isEqualTo(submissionEnvelope));
    }

    private void assertAllProcessesExported(Set<String> assayIds, Set<String> analysisIds,
                                            Set<ExportData> exportData) {

        List<Process> sentProcesses = exportData.stream()
                                                .map(ExportData::getProcess)
                                                .collect(toList());

        assertThat(sentProcesses.stream().map(Process::getId)).containsAll(assayIds);
        assertThat(sentProcesses.stream().map(Process::getId)).containsAll(analysisIds);
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        Exporter defaultExporter() {
            return new DefaultExporter();
        }

    }

}