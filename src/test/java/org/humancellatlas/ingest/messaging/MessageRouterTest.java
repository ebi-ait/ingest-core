package org.humancellatlas.ingest.messaging;

import org.humancellatlas.ingest.core.web.LinkGenerator;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.messaging.model.AssaySubmittedMessage;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.humancellatlas.ingest.messaging.Constants.Exchanges.ASSAY_EXCHANGE;
import static org.humancellatlas.ingest.messaging.Constants.Routing.ASSAY_SUBMITTED;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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
        ExportMessage message = new ExportMessage(2, 4, process, submissionEnvelope);

        //and:
        String callbackLink = "/processes/78bbd9";
        doReturn(callbackLink).when(linkGenerator).createCallback(any(MetadataDocument.class));

        //when:
        messageRouter.sendAssayForExport(message);

        //then:
        ArgumentCaptor<AssaySubmittedMessage> messageCaptor =
                ArgumentCaptor.forClass(AssaySubmittedMessage.class);
        verify(messageSender).queueNewAssayMessage(eq(ASSAY_EXCHANGE), eq(ASSAY_SUBMITTED),
                messageCaptor.capture());

        //and:
        AssaySubmittedMessage submittedMessage = messageCaptor.getValue();
        assertThat(submittedMessage)
                .extracting("documentId", "documentUuid", "documentType", "envelopeId",
                        "envelopeUuid", "assayIndex", "totalAssays")
                .containsExactly(processId, processUuid.toString(), Process.class.getSimpleName(),
                        envelopeId, envelopeUuid.toString(), 2, 4);
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        MessageRouter messageRouter() {
            return new MessageRouter();
        }

    }

}
