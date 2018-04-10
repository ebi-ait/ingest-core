package org.humancellatlas.ingest.messaging;

import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.web.LinkGenerator;
import org.humancellatlas.ingest.export.ExportData;
import org.humancellatlas.ingest.messaging.model.ExportMessage;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.humancellatlas.ingest.messaging.Constants.Exchanges.ASSAY_EXCHANGE;
import static org.humancellatlas.ingest.messaging.Constants.Routing.ANALYSIS_SUBMITTED;
import static org.humancellatlas.ingest.messaging.Constants.Routing.ASSAY_SUBMITTED;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MessageRouterTest {

    @Autowired
    private MessageRouter messageRouter;

    @MockBean
    private MessageSender messageSender;

    @MockBean
    private ResourceMappings resourceMappings;

    @MockBean
    private RepositoryRestConfiguration config;

    @MockBean
    private LinkGenerator linkGenerator;

    @Test
    public void testSendAssayForExport() {
        //expect:
        doTestSendForExport(ASSAY_SUBMITTED, messageRouter::sendAssayForExport);
    }

    @Test
    public void testSendAnalysisForExport() {
        //expect:
        doTestSendForExport(ANALYSIS_SUBMITTED, messageRouter::sendAnalysisForExport);
    }

    private void doTestSendForExport(String routingKey, Consumer<ExportData> testMethod) {
        //given:
        String processId = "78bbd9";
        Process process = new Process(processId);
        Uuid processUuid = new Uuid();
        process.setUuid(processUuid);

        //and:
        String envelopeId = "87bcf3";
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope(envelopeId);
        Uuid envelopeUuid = new Uuid();
        submissionEnvelope.setUuid(envelopeUuid);

        //and:
        ExportData exportData = new ExportData(2, 4, process, submissionEnvelope);

        //and:
        String callbackLink = "/processes/78bbd9";
        doReturn(callbackLink).when(linkGenerator).createCallback(any(Class.class), anyString());

        //when:
        testMethod.accept(exportData);

        //then:
        ArgumentCaptor<ExportMessage> messageCaptor = ArgumentCaptor.forClass(ExportMessage.class);
        verify(messageSender).queueNewExportMessage(eq(ASSAY_EXCHANGE), eq(routingKey),
                messageCaptor.capture());

        //and:
        ExportMessage submittedMessage = messageCaptor.getValue();
        assertThat(submittedMessage)
                .extracting("documentId", "documentUuid", "callbackLink", "documentType",
                        "envelopeId", "envelopeUuid", "index", "total")
                .containsExactly(processId, processUuid.getUuid().toString(), callbackLink,
                        Process.class.getSimpleName(), envelopeId, envelopeUuid.getUuid().toString(), 2, 4);
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        MessageRouter messageRouter() {
            return new MessageRouter();
        }

    }

}
