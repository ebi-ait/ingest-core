package org.humancellatlas.ingest.export;

import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DefaultExporterTest {

    @Configuration
    static class TestConfiguration {

        @Bean
        Exporter defaultExporter() {
            return new DefaultExporter();
        }

    }

    @Autowired
    private Exporter exporter;

    @MockBean
    private ProcessService processService;

    @MockBean
    private MessageRouter messageRouter;

    @Test
    public void testExportBundles() {
        //given:
        List<Process> assayingProcesses = IntStream.range(0, 2)
                .mapToObj(count -> mock(Process.class))
                .collect(Collectors.toList());
        doReturn(assayingProcesses).when(processService).findAssays(any(SubmissionEnvelope.class));

        //and:
        List<Process> analysisProcesses = IntStream.range(0, 3)
                .mapToObj(count -> mock(Process.class))
                .collect(Collectors.toList());
        doReturn(analysisProcesses)
                .when(processService).findAnalyses(any(SubmissionEnvelope.class));

        //when:
        exporter.exportBundles(new SubmissionEnvelope());

        //then:
        assayingProcesses.forEach(assayingProcess -> {
            verify(messageRouter).sendAssayForExport(assayingProcess);
        });

        //and:
        analysisProcesses.forEach(analysisProcess -> {
            verify(messageRouter).sendAnalysisForExport(analysisProcess);
        });
    }

}