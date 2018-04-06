package org.humancellatlas.ingest.export;

import org.humancellatlas.ingest.messaging.ExportMessage;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DefaultExporterTest {

    @Autowired
    private Exporter exporter;

    @MockBean
    private ProcessService processService;

    @MockBean
    private MessageRouter messageRouter;

    @Test
    public void testExportBundles() {
        //given:
        List<Process> assays = mockProcesses(2);
        doReturn(assays).when(processService).findAssays(any(SubmissionEnvelope.class));

        //and:
        List<Process> analyses = mockProcesses(3);
        doReturn(analyses).when(processService).findAnalyses(any(SubmissionEnvelope.class));

        //and:
        Set<ExportMessage> receivedMessages = mockSendingThroughMessageRouter();

        //when:
        exporter.exportBundles(new SubmissionEnvelope());

        //then:
        int expectedCount = 5;
        assertThat(receivedMessages).hasSize(expectedCount);
        assertUniqueIndexes(receivedMessages);
        assertCorrectTotalCount(receivedMessages, expectedCount);
        assertAllProcessesExported(assays, analyses, receivedMessages);

        //and:
        verify(messageRouter, times(assays.size()))
                .sendAssayForExport(any(ExportMessage.class));
        verify(messageRouter, times(analyses.size()))
                .sendAnalysisForExport(any(ExportMessage.class));
    }

    private List<Process> mockProcesses(int max) {
        return IntStream.range(0, max)
                .mapToObj(count -> mock(Process.class))
                .collect(toList());
    }

    private Set<ExportMessage> mockSendingThroughMessageRouter() {
        final Set<ExportMessage> exportMessages = new HashSet<>();
        Answer<Void> addToMessages = invocation ->  {
            exportMessages.add(invocation.getArgumentAt(0, ExportMessage.class));
            return null;
        };
        doAnswer(addToMessages).when(messageRouter).sendAssayForExport(any(ExportMessage.class));
        doAnswer(addToMessages).when(messageRouter).sendAnalysisForExport(any(ExportMessage.class));
        return exportMessages;
    }

    private void assertUniqueIndexes(Set<ExportMessage> receivedMessages) {
        List<Integer> indexes = receivedMessages.stream()
                .map(ExportMessage::getIndex)
                .collect(toList());
        assertThat(indexes).containsOnlyOnce(0, 1, 2, 3, 4);
    }

    private void assertCorrectTotalCount(Set<ExportMessage> receivedMessages, int expectedCount) {
        receivedMessages.stream().forEach(message -> {
            assertThat(message.getTotalCount()).isEqualTo(expectedCount);
        });
    }

    private void assertAllProcessesExported(List<Process> assays, List<Process> analyses,
            Set<ExportMessage> receivedMessages) {
        List<Process> sentProcesses = receivedMessages.stream()
                .map(ExportMessage::getProcess)
                .collect(toList());
        assertThat(sentProcesses).containsAll(assays);
        assertThat(sentProcesses).containsAll(analyses);
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        Exporter defaultExporter() {
            return new DefaultExporter();
        }

    }

}