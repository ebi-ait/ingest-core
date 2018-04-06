package org.humancellatlas.ingest.messaging;

import org.humancellatlas.ingest.core.LinkGenerator;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.messaging.model.AssaySubmittedMessage;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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
        Process process = new Process("78bbd9");
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        ExportMessage message = new ExportMessage(2, 4, process, submissionEnvelope);

        //and:
        String callbackLink = "/processes/78bbd9";
        doReturn(callbackLink).when(linkGenerator).createCallback(any(MetadataDocument.class));

        //when:
        messageRouter.sendAssayForExport(message);

        //then:
        verify(messageSender).queueNewAssayMessage(anyString(), anyString(),
                any(AssaySubmittedMessage.class));
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        MessageRouter messageRouter() {
            return new MessageRouter();
        }

    }

}
