package org.humancellatlas.ingest.messaging;

import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.config.ConfigurationService;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.web.LinkGenerator;
import org.humancellatlas.ingest.export.ExportState;
import org.humancellatlas.ingest.export.job.ExportJob;
import org.humancellatlas.ingest.exporter.ExperimentProcess;
import org.humancellatlas.ingest.messaging.model.ManifestMessage;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ResourceMappings;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.humancellatlas.ingest.messaging.Constants.Exchanges.EXPORTER_EXCHANGE;
import static org.humancellatlas.ingest.messaging.Constants.Routing.EXPERIMENT_SUBMITTED;
import static org.humancellatlas.ingest.messaging.Constants.Routing.MANIFEST_SUBMITTED;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;


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

    @MockBean
    private ConfigurationService configurationService;

    @Test
    public void testSendManifestForExport() {
        //expect:
        doTestSendForExport(MANIFEST_SUBMITTED);
    }


    @Test
    public void testRouteStateTrackingUpdateMessageFor() {
        // given:
        Project project = mock(Project.class);
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope("sub-1");
        doReturn(Instant.now()).when(project).getUpdateDate();
        doReturn(submissionEnvelope).when(project).getSubmissionEnvelope();

        // when:
        messageRouter.routeStateTrackingUpdateMessageFor(project);

        verify(messageSender, times(1)).queueDocumentStateUpdateMessage(any(URI.class), any(), anyLong());
    }

    @Test
    public void testRouteStateTrackingUpdateMessageForBiomaterial() {
        // given:
        Biomaterial project = mock(Biomaterial.class);
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope("sub-1");
        doReturn(Instant.now()).when(project).getUpdateDate();
        doReturn(submissionEnvelope).when(project).getSubmissionEnvelope();

        // when:
        messageRouter.routeStateTrackingUpdateMessageFor(project);

        verify(messageSender, times(1)).queueDocumentStateUpdateMessage(any(URI.class), any(), anyLong());
    }

    @Test
    public void testRouteStateTrackingUpdateMessageForProject() {
        // given:
        Project project = new Project(null);
        // when:
        messageRouter.routeStateTrackingUpdateMessageFor(project);

        verify(messageSender, never()).queueDocumentStateUpdateMessage(any(URI.class), any(), anyLong());
    }

    @Test
    public void testRouteStateTrackingUpdateMessageForBiomaterialWithoutSubmission() {
        // given:
        Biomaterial project = mock(Biomaterial.class);

        // when:
        Exception exception = assertThrows(RuntimeException.class, () -> {
            messageRouter.routeStateTrackingUpdateMessageFor(project);
        });

        verify(messageSender, never()).queueDocumentStateUpdateMessage(any(URI.class), any(), anyLong());
    }

    private void doTestSendForExport(String routingKey) {
        //given:
        String processId = "78bbd9";
        Process process = new Process(processId);
        Uuid processUuid = Uuid.newUuid();
        process.setUuid(processUuid);
        Instant version = Instant.now();
        process.setDcpVersion(version);

        //and:
        String envelopeId = "87bcf3";
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope(envelopeId);
        Uuid envelopeUuid = Uuid.newUuid();
        submissionEnvelope.setUuid(envelopeUuid);

        process.setSubmissionEnvelope(submissionEnvelope);

        //and:
        ExperimentProcess exporterData = new ExperimentProcess(2, 4, process, submissionEnvelope, null);

        //and:
        String callbackLink = "/processes/78bbd9";
        doReturn(callbackLink).when(linkGenerator).createCallback(any(Class.class), anyString());

        //when:
        messageRouter.sendManifestForExport(exporterData);

        //then:
        ArgumentCaptor<ManifestMessage> messageCaptor = ArgumentCaptor.forClass(ManifestMessage.class);
        verify(messageSender).queueNewExportMessage(eq(EXPORTER_EXCHANGE), eq(routingKey),
                messageCaptor.capture(), anyLong());

        //and:
        ManifestMessage submittedMessage = messageCaptor.getValue();
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
