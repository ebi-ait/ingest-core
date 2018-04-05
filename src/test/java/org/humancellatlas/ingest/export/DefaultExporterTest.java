package org.humancellatlas.ingest.export;

import org.assertj.core.api.Assertions;
import org.humancellatlas.ingest.messaging.ExportMessage;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        final Set<ExportMessage> exportMessages = new HashSet<>();
        Answer<Void> addToMessages = invocation ->  {
            exportMessages.add(invocation.getArgumentAt(0, ExportMessage.class));
            return null;
        };
        doAnswer(addToMessages).when(messageRouter).sendAssayForExport(any(ExportMessage.class));
        doAnswer(addToMessages).when(messageRouter).sendAnalysisForExport(any(ExportMessage.class));

        //when:
        exporter.exportBundles(new SubmissionEnvelope());

        //then:
        assertThat(exportMessages).hasSize(5);
    }

    private List<Process> mockProcesses(int max) {
        return IntStream.range(0, max)
                .mapToObj(count -> mock(Process.class))
                .collect(Collectors.toList());
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        Exporter defaultExporter() {
            return new DefaultExporter();
        }

    }

}