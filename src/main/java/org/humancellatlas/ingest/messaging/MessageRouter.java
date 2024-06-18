package org.humancellatlas.ingest.messaging;

import static org.humancellatlas.ingest.messaging.Constants.Exchanges.EXPORTER_EXCHANGE;
import static org.humancellatlas.ingest.messaging.Constants.Routing.*;

import java.net.URI;
import java.util.Map;

import org.humancellatlas.ingest.config.ConfigurationService;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.MetadataDocumentMessageBuilder;
import org.humancellatlas.ingest.core.web.LinkGenerator;
import org.humancellatlas.ingest.export.job.ExportJob;
import org.humancellatlas.ingest.exporter.ExperimentProcess;
import org.humancellatlas.ingest.messaging.model.MetadataDocumentMessage;
import org.humancellatlas.ingest.messaging.model.SubmissionEnvelopeMessage;
import org.humancellatlas.ingest.messaging.model.SubmissionEnvelopeStateUpdateMessage;
import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
public class MessageRouter {

  @Autowired private MessageSender messageSender;

  @Autowired private ConfigurationService configurationService;

  @Autowired private LinkGenerator linkGenerator;

  private final Logger log = LoggerFactory.getLogger(getClass());

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

  /* messages to graph validator */
  public boolean routeGraphValidationMessageFor(SubmissionEnvelope envelope) {
    if (envelope.allowedSubmissionStateTransitions().contains(SubmissionState.GRAPH_VALIDATING)) {
      this.messageSender.queueGraphValidationMessage(
          Constants.Exchanges.VALIDATION_EXCHANGE,
          Constants.Queues.GRAPH_VALIDATION_QUEUE,
          messageFor(envelope),
          System.currentTimeMillis());
      return true;
    }
    return false;
  }

  /* messages to state tracker */
  public boolean routeStateTrackingUpdateMessageFor(MetadataDocument document) {
    // allow projects to be created first before submission envelope
    if (document.getSubmissionEnvelope() != null || document.getType() != EntityType.PROJECT) {
      URI documentUpdateUri =
          UriComponentsBuilder.newInstance()
              .scheme(configurationService.getStateTrackerScheme())
              .host(configurationService.getStateTrackerHost())
              .port(configurationService.getStateTrackerPort())
              .pathSegment(configurationService.getDocumentStatesUpdatePath())
              .build()
              .toUri();

      this.messageSender.queueDocumentStateUpdateMessage(
          documentUpdateUri,
          documentStateUpdateMessage(document),
          document.getUpdateDate().toEpochMilli());
    }
    return true;
  }

  public boolean routeStateTrackingDeleteMessageFor(MetadataDocument document) {
    if (document.getSubmissionEnvelope() != null) {
      URI documentDeleteUri =
          UriComponentsBuilder.newInstance()
              .scheme(configurationService.getStateTrackerScheme())
              .host(configurationService.getStateTrackerHost())
              .port(configurationService.getStateTrackerPort())
              .pathSegment(configurationService.getDocumentStatesUpdatePath())
              .queryParam(configurationService.getDocumentIdParamName(), document.getId())
              .queryParam(
                  configurationService.getEnvelopeIdParamName(),
                  document.getSubmissionEnvelope().getId())
              .build()
              .toUri();
      this.messageSender.queueDocumentStateDeleteMessage(
          documentDeleteUri, document.getUpdateDate().toEpochMilli());
      return true;
    } else {
      log.warn(
          String.format(
              "The metadata document '%s' is not linked to a submission envelope",
              document.getId()));
      return false;
    }
  }

  public boolean routeStateTrackingUpdateMessageForEnvelopeEvent(
      SubmissionEnvelope envelope, SubmissionState state) {
    // TODO: call this when a user requests a state change on an envelope
    this.messageSender.queueStateTrackingMessage(
        Constants.Exchanges.STATE_TRACKING_EXCHANGE,
        Constants.Routing.ENVELOPE_STATE_UPDATE,
        messageFor(envelope, state),
        envelope.getUpdateDate().toEpochMilli());
    return true;
  }

  public boolean routeStateTrackingNewSubmissionEnvelope(SubmissionEnvelope envelope) {
    this.messageSender.queueStateTrackingMessage(
        Constants.Exchanges.STATE_TRACKING_EXCHANGE,
        Constants.Routing.ENVELOPE_CREATE,
        messageFor(envelope),
        envelope.getUpdateDate().toEpochMilli());
    return true;
  }

  /* messages to the exporter */

  public void sendManifestForExport(ExperimentProcess experimentProcess) {
    messageSender.queueNewExportMessage(
        EXPORTER_EXCHANGE,
        MANIFEST_SUBMITTED,
        experimentProcess.toManifestMessage(linkGenerator),
        System.currentTimeMillis());
  }

  public void sendExperimentForExport(
      ExperimentProcess experimentProcess, ExportJob exportJob, Map<String, Object> context) {
    messageSender.queueNewExportMessage(
        EXPORTER_EXCHANGE,
        EXPERIMENT_SUBMITTED,
        experimentProcess.toExportEntityMessage(linkGenerator, exportJob, context),
        System.currentTimeMillis());
  }

  public void sendSubmissionForDataExport(ExportJob exportJob, Map<String, Object> context) {
    messageSender.queueNewExportMessage(
        EXPORTER_EXCHANGE,
        SUBMISSION_SUBMITTED,
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
        EXPORTER_EXCHANGE,
        SPREADSHEET_GENERATION,
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
