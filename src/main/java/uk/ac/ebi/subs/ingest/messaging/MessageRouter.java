package uk.ac.ebi.subs.ingest.messaging;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.NoArgsConstructor;
import uk.ac.ebi.subs.ingest.core.MetadataDocument;
import uk.ac.ebi.subs.ingest.core.MetadataDocumentMessageBuilder;
import uk.ac.ebi.subs.ingest.core.web.LinkGenerator;
import uk.ac.ebi.subs.ingest.export.job.ExportJob;
import uk.ac.ebi.subs.ingest.exporter.ExperimentProcess;
import uk.ac.ebi.subs.ingest.messaging.model.MetadataDocumentMessage;
import uk.ac.ebi.subs.ingest.messaging.model.SubmissionEnvelopeMessage;
import uk.ac.ebi.subs.ingest.messaging.model.SubmissionEnvelopeStateUpdateMessage;
import uk.ac.ebi.subs.ingest.state.SubmissionState;
import uk.ac.ebi.subs.ingest.state.ValidationState;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelopeMessageBuilder;

@Component
@NoArgsConstructor
public class MessageRouter {
  @Autowired private MessageSender messageSender;
  @Autowired private LinkGenerator linkGenerator;

  /* messages to validator */
  public boolean routeValidationMessageFor(MetadataDocument document) {
    if (document.getValidationState().equals(ValidationState.DRAFT)) {
      this.messageSender.queueValidationMessage(
          Constants.Exchanges.VALIDATION_EXCHANGE,
          Constants.Queues.METADATA_VALIDATION_QUEUE,
          messageFor(document),
          document.getUpdateDate().toEpochMilli());
      return true;
    } else {
      return false;
    }
  }

  /* messages to the exporter */

  public void sendManifestForExport(ExperimentProcess experimentProcess) {
    messageSender.queueNewExportMessage(
        Constants.Exchanges.EXPORTER_EXCHANGE,
        Constants.Routing.MANIFEST_SUBMITTED,
        experimentProcess.toManifestMessage(linkGenerator),
        System.currentTimeMillis());
  }

  public void sendExperimentForExport(
      ExperimentProcess experimentProcess, ExportJob exportJob, Map<String, Object> context) {
    messageSender.queueNewExportMessage(
        Constants.Exchanges.EXPORTER_EXCHANGE,
        Constants.Routing.EXPERIMENT_SUBMITTED,
        experimentProcess.toExportEntityMessage(linkGenerator, exportJob, context),
        System.currentTimeMillis());
  }

  public void sendSubmissionForDataExport(ExportJob exportJob, Map<String, Object> context) {
    messageSender.queueNewExportMessage(
        Constants.Exchanges.EXPORTER_EXCHANGE,
        Constants.Routing.SUBMISSION_SUBMITTED,
        exportJob.toExportSubmissionMessage(linkGenerator, context),
        System.currentTimeMillis());
  }

  /* messages to the upload/staging area manager */

  public boolean routeRequestUploadAreaCredentials(SubmissionEnvelope envelope) {
    this.messageSender.queueUploadManagerMessage(
        Constants.Exchanges.UPLOAD_AREA_EXCHANGE,
        Constants.Routing.UPLOAD_AREA_CREATE,
        messageFor(envelope),
        envelope.getUpdateDate().toEpochMilli());
    return true;
  }

  public boolean routeRequestUploadAreaCleanup(SubmissionEnvelope envelope) {
    this.messageSender.queueUploadManagerMessage(
        Constants.Exchanges.UPLOAD_AREA_EXCHANGE,
        Constants.Routing.UPLOAD_AREA_CLEANUP,
        messageFor(envelope),
        envelope.getUpdateDate().toEpochMilli());
    return true;
  }

  public void sendGenerateSpreadsheet(ExportJob exportJob, Map<String, Object> context) {
    this.messageSender.queueSpreadsheetGenerationMessage(
        Constants.Exchanges.EXPORTER_EXCHANGE,
        Constants.Routing.SPREADSHEET_GENERATION,
        exportJob.toGenerateSubmissionMessage(linkGenerator, context),
        System.currentTimeMillis());
  }

  private MetadataDocumentMessage messageFor(MetadataDocument document) {
    return MetadataDocumentMessageBuilder.using(linkGenerator).messageFor(document).build();
  }

  private SubmissionEnvelopeMessage messageFor(SubmissionEnvelope envelope) {
    return SubmissionEnvelopeMessageBuilder.using(linkGenerator).messageFor(envelope).build();
  }

  private MetadataDocumentMessage documentStateUpdateMessage(MetadataDocument document) {
    if (document.getSubmissionEnvelope() == null) {
      throw new RuntimeException(
          "The metadata document should have a link to a submission envelope.");
    }

    String envelopeId = document.getSubmissionEnvelope().getId();

    return MetadataDocumentMessageBuilder.using(linkGenerator)
        .messageFor(document)
        .withEnvelopeId(envelopeId)
        .withValidationState(document.getValidationState())
        .build();
  }

  private SubmissionEnvelopeStateUpdateMessage messageFor(
      SubmissionEnvelope envelope, SubmissionState state) {
    SubmissionEnvelopeStateUpdateMessage message =
        SubmissionEnvelopeStateUpdateMessage.fromSubmissionEnvelopeMessage(messageFor(envelope));
    message.setRequestedState(state);
    return message;
  }
}
