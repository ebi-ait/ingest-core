package uk.ac.ebi.subs.ingest.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static uk.ac.ebi.subs.ingest.export.destination.ExportDestinationName.DCP;
import static uk.ac.ebi.subs.ingest.messaging.Constants.Exchanges.EXPORTER_EXCHANGE;
import static uk.ac.ebi.subs.ingest.messaging.Constants.Routing.MANIFEST_SUBMITTED;

import java.time.Instant;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ResourceMappings;

import uk.ac.ebi.subs.ingest.config.ConfigurationService;
import uk.ac.ebi.subs.ingest.core.Uuid;
import uk.ac.ebi.subs.ingest.core.web.LinkGenerator;
import uk.ac.ebi.subs.ingest.export.destination.ExportDestination;
import uk.ac.ebi.subs.ingest.export.job.ExportJob;
import uk.ac.ebi.subs.ingest.exporter.ExperimentProcess;
import uk.ac.ebi.subs.ingest.messaging.model.ExportSubmissionMessage;
import uk.ac.ebi.subs.ingest.messaging.model.ManifestMessage;
import uk.ac.ebi.subs.ingest.process.Process;
import uk.ac.ebi.subs.ingest.project.Project;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

@SpringBootTest
public class MessageRouterTest {

  @Autowired private MessageRouter messageRouter;

  @MockBean private MessageSender messageSender;

  @MockBean private ResourceMappings resourceMappings;

  @MockBean private RepositoryRestConfiguration config;

  @MockBean private LinkGenerator linkGenerator;

  @MockBean private ConfigurationService configurationService;

  @Test
  public void testSendManifestForExport() {
    // expect:
    doTestSendForExport(MANIFEST_SUBMITTED);
  }

  @Test
  public void testSendSubmissionForDataExport() {
    // given
    var submissionEnvelope = new SubmissionEnvelope();
    submissionEnvelope.setUuid(Uuid.newUuid());
    var project = new Project(null);
    project.setUuid(Uuid.newUuid());
    project.getSubmissionEnvelopes().add(submissionEnvelope);
    var exportJob = exportJob(submissionEnvelope, project);
    var context = new JSONObject();

    // when
    messageRouter.sendSubmissionForDataExport(exportJob, context);

    // then
    var argumentCaptor = ArgumentCaptor.forClass(ExportSubmissionMessage.class);
    verify(messageSender)
        .queueNewExportMessage(anyString(), anyString(), argumentCaptor.capture(), anyLong());
    verify(linkGenerator).createCallback(any(), anyString());

    var capturedArgument = argumentCaptor.getValue();
    assertThat(capturedArgument.getExportJobId()).isEqualTo(exportJob.getId());
    assertThat(capturedArgument.getSubmissionUuid())
        .isEqualTo(submissionEnvelope.getUuid().getUuid().toString());
    assertThat(capturedArgument.getProjectUuid()).isEqualTo(project.getUuid().getUuid().toString());
    assertThat(capturedArgument.getContext()).isEqualTo(context);
  }

  private ExportJob exportJob(SubmissionEnvelope submissionEnvelope, Project project) {
    var destinationContext = new JSONObject();
    destinationContext.put("projectUuid", project.getUuid().getUuid().toString());

    var exportJobContext = new JSONObject();
    exportJobContext.put("dataFileTransfer", false);
    return ExportJob.builder()
        .id("testExportJobId")
        .submission(submissionEnvelope)
        .destination(new ExportDestination(DCP, "v2", destinationContext))
        .context(exportJobContext)
        .build();
  }

  private void doTestSendForExport(String routingKey) {
    // given:
    String processId = "78bbd9";
    Process process = spy(new Process(null));
    doReturn(processId).when(process).getId();

    Uuid processUuid = Uuid.newUuid();
    process.setUuid(processUuid);
    Instant version = Instant.now();
    process.setDcpVersion(version);

    // and:
    String envelopeId = "87bcf3";
    SubmissionEnvelope submissionEnvelope = spy(new SubmissionEnvelope());
    doReturn(envelopeId).when(submissionEnvelope).getId();
    Uuid envelopeUuid = Uuid.newUuid();
    submissionEnvelope.setUuid(envelopeUuid);

    process.setSubmissionEnvelope(submissionEnvelope);

    // and:
    ExperimentProcess exporterData = new ExperimentProcess(2, 4, process, submissionEnvelope, null);

    // and:
    String callbackLink = "/processes/78bbd9";
    doReturn(callbackLink).when(linkGenerator).createCallback(any(Class.class), anyString());

    // when:
    messageRouter.sendManifestForExport(exporterData);

    // then:
    ArgumentCaptor<ManifestMessage> messageCaptor = ArgumentCaptor.forClass(ManifestMessage.class);
    verify(messageSender)
        .queueNewExportMessage(
            eq(EXPORTER_EXCHANGE), eq(routingKey), messageCaptor.capture(), anyLong());

    // and:
    ManifestMessage submittedMessage = messageCaptor.getValue();
    assertThat(submittedMessage)
        .extracting(
            "documentId",
            "documentUuid",
            "callbackLink",
            "documentType",
            "envelopeId",
            "envelopeUuid",
            "index",
            "total")
        .containsExactly(
            processId,
            processUuid.getUuid().toString(),
            callbackLink,
            process.getClass().getSimpleName(),
            envelopeId,
            envelopeUuid.getUuid().toString(),
            2,
            4);
  }

  @Configuration
  static class TestConfiguration {

    @Bean
    MessageRouter messageRouter() {
      return new MessageRouter();
    }
  }
}
